package ma.m2m.gateway.controller;

import static ma.m2m.gateway.Utils.StringUtils.isNullOrEmpty;
import static ma.m2m.gateway.config.FlagActivation.ACTIVE;
import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.RequestDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.risk.GWRiskAnalysis;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.ControlRiskCmrService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.EmetteurService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.TelecollecteService;
import ma.m2m.gateway.service.TransactionService;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class APIController {

	private static Logger logger = LogManager.getLogger(APIController.class);

	private LocalDateTime dateF;
	private String folder;
	private String file;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;

	private Gson gson;

	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDSS;

	@Value("${key.LINK_SUCCESS}")
	private String link_success;

	@Value("${key.LINK_CCB}")
	private String link_ccb;

	@Value("${key.LINK_FAIL}")
	private String link_fail;

	@Value("${key.LINK_CHALENGE}")
	private String link_chalenge;

	@Value("${key.LINK_INDEX}")
	private String link_index;

	@Value("${key.SWITCH_URL}")
	private String ipSwitch;

	@Value("${key.SWITCH_PORT}")
	private String portSwitch;

	@Value("${key.SECRET}")
	private String secret;

	@Value("${key.USER_TOKEN}")
	private String usernameToken;

	@Value("${key.JWT_TOKEN_VALIDITY}")
	private long jwt_token_validity;

	@Value("${key.ENVIRONEMENT}")
	private String environement;

	@Autowired
	AutorisationService autorisationService;

	@Autowired
	private DemandePaiementService demandePaiementService;

	@Autowired
	HistoAutoGateService histoAutoGateService;

	@Autowired
	TransactionService transactionService;

	@Autowired
	CommercantService commercantService;

	@Autowired
	TelecollecteService telecollecteService;

	@Autowired
	CardtokenService cardtokenService;

	@Autowired
	private ControlRiskCmrService controlRiskCmrService;

	@Autowired
	private EmetteurService emetteurService;

	@Autowired
	CodeReponseService codeReponseService;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd");

	public APIController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		//String file = "API_" + randomWithSplittableRandom;
		// date of folder logs
		dateF = LocalDateTime.now(ZoneId.systemDefault());
		folder = dateF.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	@PostMapping(value = "/napspayment/authorization", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String authorization(@RequestHeader MultiValueMap<String, String> header, @RequestBody String auths,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_AUTH_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start authorization() ************** ");
		System.out.println("*********** Start authorization() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		logger.info("authorization api call start ...");

		Util.writeInFileTransaction(folder, file, "authorization api call start ...");

		Util.writeInFileTransaction(folder, file, "authorization : [" + auths + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(auths);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "authorization 500 malformed json expression " + auths + jserr);
			return getMsgError(folder, file, null, "authorization 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {
			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "authorization 500 malformed header", null);
			}

			else {
				Util.writeInFileTransaction(folder, file, "authorization 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "authorization 500 malformed header " + head_err.getMessage(),
						null);
			}

		}

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", securtoken24, mac_value,
				successURL, failURL, transactiontype, etataut, auth3ds;

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		try {
			// Transaction info
			// securtoken24 = (String) jsonOrequest.get("securtoken24");
			// mac_value = (String) jsonOrequest.get("mac_value");

			capture = (String) jsonOrequest.get("capture");
			currency = (String) jsonOrequest.get("currency");
			orderid = (String) jsonOrequest.get("orderid");
			recurring = (String) jsonOrequest.get("recurring");
			amount = (String) jsonOrequest.get("amount");
			promoCode = (String) jsonOrequest.get("promocode");
			transactionid = (String) jsonOrequest.get("transactionid");
			transactiontype = (String) jsonOrequest.get("transactiontype");

			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackUrl = (String) jsonOrequest.get("callbackurl");
			successURL = (String) jsonOrequest.get("successURL");
			failURL = (String) jsonOrequest.get("failURL");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");
			token = (String) jsonOrequest.get("token");
			expirydate = (String) jsonOrequest.get("expirydate");
			holdername = (String) jsonOrequest.get("holdername");
			cvv = (String) jsonOrequest.get("cvv");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");
			country = (String) jsonOrequest.get("country");
			phone = (String) jsonOrequest.get("phone");
			city = (String) jsonOrequest.get("city");
			state = (String) jsonOrequest.get("state");
			zipcode = (String) jsonOrequest.get("zipcode");
			address = (String) jsonOrequest.get("address");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "authorization 500 malformed json expression " + jerr);
			return getMsgError(folder, file, null, "authorization 500 malformed json expression " + jerr.getMessage(),
					null);
		}
		
		try {
			auth3ds = (String) jsonOrequest.get("auth3ds");
			if(auth3ds.equals("")) {
				auth3ds = "Y";
			}
		} catch(Exception e) {
			auth3ds="Y";
			Util.writeInFileTransaction(folder, file, "authorization 500 malformed json expression auth3ds " + e);
		}
		
		// get cardnumber by token
		if (!token.equals("") && token != null && !token.equals("null")) {
			try {
				CardtokenDto card = cardtokenService.findByIdMerchantAndToken(merchantid, token);
				if (card != null) {
					if (card.getCardNumber() != null) {
						cardnumber = card.getCardNumber();
						if (expirydate.equals("")) {
							String dateExStr = dateFormat.format(card.getExprDate());
							expirydate = dateExStr.substring(2, 4) + dateExStr.substring(5, 7);
						}
					}
				}
			} catch (Exception jerr) {
				Util.writeInFileTransaction(folder, file, "authorization 500 token not found" + jerr);
				return getMsgError(folder, file, jsonOrequest, "authorization 500 token not found", null);
			}
		}

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		Util.writeInFileTransaction(folder, file, "authorization_" + orderid + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"authorization 500 Merchant misconfigured in DB or not existing", "");
		}

		// get demandepaiement id , check if exist

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "authorization 500 Error Already exist in PaiementRequest",
					"16");
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			Util.writeInFileTransaction(folder, file, "authorization 500 Card number length is incorrect orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "authorization 500 Card number length is incorrect", null);
		}

		if (i_card_valid == 2) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Card number  is not valid incorrect luhn check orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"authorization 500 Card number  is not valid incorrect luhn check", null);
		}

		int i_card_type = Util.getCardIss(cardnumber);

		try {

			dmd = new DemandePaiementDto();

			dmd.setComid(merchantid);
			dmd.setCommande(orderid);
			dmd.setDem_pan(cardnumber);
			dmd.setDem_cvv(cvv);
			dmd.setGalid(websiteid);
			dmd.setSuccessURL(successURL);
			dmd.setFailURL(failURL);
			dmd.setType_carte(i_card_type + "");
			if (amount.equals("") || amount == null) {
				amount = "0";
			}
			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			dmd.setMontant(Double.parseDouble(amount));
			dmd.setNom(lname);
			dmd.setPrenom(fname);
			dmd.setEmail(email);
			dmd.setTel(phone);
			dmd.setAddress(address);
			dmd.setCity(city);
			dmd.setCountry(country);
			dmd.setState(state);
			dmd.setPostcode(zipcode);
			// dmd.setDateexpnaps(expirydate);
			dmd.setLangue("E");
			dmd.setEtat_demande("INIT");

			formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			// dmd.setDem_date_time(transactiondate + transactiontime);
			dmd.setDem_date_time(dateFormat.format(new Date()));
			if (recurring.equalsIgnoreCase("Y"))
				dmd.setIs_cof("Y");
			if (recurring.equalsIgnoreCase("N"))
				dmd.setIs_cof("N");

			dmd.setIs_addcard("N");
			dmd.setIs_tokenized("N");
			dmd.setIs_whitelist("N");
			dmd.setIs_withsave("N");

			// generer token
			String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
			dmd.setTokencommande(tokencommande);
			// set transctiontype
			dmd.setTransactiontype(transactiontype);

			dmdSaved = demandePaiementService.save(dmd);
			dmdSaved.setExpery(expirydate);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]"
							+ err1);

			return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during DEMANDE_PAIEMENT insertion",
					null);
		}

		// for test control risk
		GWRiskAnalysis riskAnalysis = new GWRiskAnalysis(folder, file);
		try {
			ControlRiskCmrDto controlRiskCmr = controlRiskCmrService.findByNumCommercant(dmdSaved.getComid());
			List<HistoAutoGateDto> porteurFlowPerDay = null;

			Double globalFlowPerDay = 0.00;
			List<EmetteurDto> listBin = null;

			if (controlRiskCmr != null) {
				/*
				 * --------------------------------- Controle des cartes internationales
				 * -----------------------------------------
				 */
				if (isNullOrEmpty(controlRiskCmr.getAcceptInternational())
						|| (controlRiskCmr.getAcceptInternational() != null && !ACTIVE.getFlag()
								.equalsIgnoreCase(controlRiskCmr.getAcceptInternational().trim()))) {
					String binDebutCarte = cardnumber.substring(0, 9);
					// binDebutCarte = binDebutCarte + "000";
					Util.writeInFileTransaction(folder, file, "controlRiskCmr ici 1");
					listBin = emetteurService.findByBindebut(binDebutCarte);
				}
				// --------------------------------- Controle de flux journalier autorisé par
				// commerçant ----------------------------------
				if (!isNullOrEmpty(controlRiskCmr.getIsGlobalFlowControlActive())
						&& ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getIsGlobalFlowControlActive())) {
					Util.writeInFileTransaction(folder, file, "controlRiskCmr ici 2");
					globalFlowPerDay = histoAutoGateService.getCommercantGlobalFlowPerDay(merchantid);
				}
				// ------------------------- Controle de flux journalier autorisé par client
				// (porteur de carte) ----------------------------
				if ((controlRiskCmr.getFlowCardPerDay() != null && controlRiskCmr.getFlowCardPerDay() > 0)
						|| (controlRiskCmr.getNumberOfTransactionCardPerDay() != null
								&& controlRiskCmr.getNumberOfTransactionCardPerDay() > 0)) {
					Util.writeInFileTransaction(folder, file, "controlRiskCmr ici 3");
					porteurFlowPerDay = histoAutoGateService.getPorteurMerchantFlowPerDay(dmdSaved.getComid(),
							dmdSaved.getDem_pan());
				}
			}
			String msg = riskAnalysis.executeRiskControls(dmdSaved.getComid(), dmdSaved.getMontant(),
					dmdSaved.getDem_pan(), controlRiskCmr, globalFlowPerDay, porteurFlowPerDay, listBin);

			if (!msg.equalsIgnoreCase("OK")) {
				dmdSaved.setEtat_demande("REJET_RISK_CTRL");
				demandePaiementService.save(dmdSaved);
				Util.writeInFileTransaction(folder, file, "authorization 500 " + msg);
				return getMsgError(folder, file, jsonOrequest, "authorization 500 " + msg, null);
			}
			// fin control risk
		} catch (Exception e) {
			dmdSaved.setEtat_demande("REJET_RISK_CTRL");
			demandePaiementService.save(dmdSaved);
			Util.writeInFileTransaction(folder, file,
					"authorization 500 ControlRiskCmr misconfigured in DB or not existing merchantid:["
							+ dmdSaved.getComid() + e);
			return getMsgError(folder, file, jsonOrequest, "authorization 500 Error Opération rejetée: Contrôle risque",
					null);
		}

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();

		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error during  date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during  date formatting", null);
		}

		JSONObject jso = new JSONObject();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		// appel 3DSSecure ***********************************************************

		/**
		 * dans la preprod les tests sans 3DSS on commente l'appel 3DSS et on mj
		 * reponseMPI="Y"
		 */
		Util.writeInFileTransaction(folder, file, "environement : " + environement);
		if (environement.equals("PREPROD")) {
			// threeDsecureResponse = autorisationService.preparerReqThree3DSS(dmdSaved,
			// folder, file);

			threeDsecureResponse.setReponseMPI("Y");
		} else {
			if(auth3ds.equals("N")) {
				Util.writeInFileTransaction(folder, file,"Si auth3ds = N passer sans 3DS ");
				threeDsecureResponse.setReponseMPI("Y");
			} else {
				Util.writeInFileTransaction(folder, file,"Si auth3ds = Y passer avec 3DS ");
				threeDsecureResponse = autorisationService.preparerReqThree3DSS(dmdSaved, folder, file);
			}
		}
		// fin 3DSSecure ***********************************************************

		/*
		 * ------------ DEBUT MPI RESPONSE PARAMS ------------
		 */
		String reponseMPI = "";
		String eci = "";
		String cavv = "";
		String threeDSServerTransID = "";
		String xid = "";
		String errmpi = "";
		String idDemande = String.valueOf(dmdSaved.getIddemande() == null ? "" : dmdSaved.getIddemande());
		String expiry = ""; // YYMM

		if (threeDsecureResponse.getReponseMPI() != null) {
			reponseMPI = threeDsecureResponse.getReponseMPI();
		}
		/*
		 * if (threeDsecureResponse.getIdDemande() != null) { idDemande =
		 * threeDsecureResponse.getIdDemande(); }
		 */
		if (threeDsecureResponse.getThreeDSServerTransID() != null) {
			threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
		}
		if (threeDsecureResponse.getEci() != null) {
			eci = threeDsecureResponse.getEci();
		} else {
			eci = "";
		}
		if (threeDsecureResponse.getCavv() != null) {
			cavv = threeDsecureResponse.getCavv();
		} else {
			cavv = "";
		}
		if (threeDsecureResponse.getErrmpi() != null) {
			errmpi = threeDsecureResponse.getErrmpi();
		} else {
			errmpi = "";
		}
		if (threeDsecureResponse.getExpiry() != null) {
			expiry = threeDsecureResponse.getExpiry();
		} else {
			expiry = "";
		}

		if (idDemande == null || idDemande.equals("")) {
			Util.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
			dmdSaved.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmdSaved);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO idDemande null : " + dmdSaved.toString());
			return getMsgError(folder, file, jsonOrequest, "AUTO INVALIDE DEMANDE MPI_KO", "96");
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			Util.writeInFileTransaction(folder, file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			return getMsgError(folder, file, jsonOrequest, "AUTO INVALIDE DEMANDE NOT FOUND", "96");
		}

		if (reponseMPI.equals("") || reponseMPI == null) {
			dmd.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmd);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			Util.writeInFileTransaction(folder, file, "Response 3DS is null");
			return getMsgError(folder, file, jsonOrequest, "Response 3DS is null", "96");
		}

		if (reponseMPI.equals("Y")) {
			// ********************* Frictionless responseMPI equal Y *********************
			Util.writeInFileTransaction(folder, file,
					"********************* Cas frictionless responseMPI equal Y *********************");
			if (!threeDSServerTransID.equals("")) {
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
			}
			
			// 2024-03-05
			montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, jsonOrequest);

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(merchantname, 19, ' ');
			Util.writeInFileTransaction(folder, file, "merchant_name : [" + merchant_name + "]");

			merchant_city = "MOROCCO        ";
			Util.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

			acq_type = "0000";
			reason_code = "H";
			transaction_condition = "6";
			mesg_type = "0";
			processing_code = "";

			if (transactiontype.equals("0")) {
				processing_code = "0";
			} else if (transactiontype.equals("P")) {
				processing_code = "P";
			} else {
				processing_code = "0";
			}

			// ajout cavv (cavv+eci) xid dans la trame
			String champ_cavv = "";
			xid = threeDSServerTransID;
			if (cavv == null || eci == null) {
				champ_cavv = null;
				Util.writeInFileTransaction(folder, file, "cavv == null || eci == null");
			} else if (cavv != null && eci != null) {
				champ_cavv = cavv + eci;
				Util.writeInFileTransaction(folder, file, "cavv != null && eci != null");
				Util.writeInFileTransaction(folder, file, "champ_cavv : [" + champ_cavv + "]");
			} else {
				Util.writeInFileTransaction(folder, file, "champ_cavv = null");
				champ_cavv = null;
			}

			boolean cvv_present = check_cvv_presence(cvv);
			boolean is_reccuring = is_reccuring_check(recurring);
			boolean is_first_trs = true;
			if (!token.equals("")) {
				cvv_present = true;
			}
			String first_auth = "";
			long lrec_serie = 0;

			// controls
			Util.writeInFileTransaction(folder, file, "Switch processing start ...");

			String tlv = "";
			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

			if (!cvv_present && !is_reccuring) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				return getMsgError(folder, file, jsonOrequest,
						"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
						"82");
			}

			// not reccuring , normal
			if (cvv_present && !is_reccuring) {
				Util.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
				try {

					tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
							.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
							.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
							.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
							.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
							.withField(Tags.tag16, date).withField(Tags.tag17, heure)
							.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
							.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
							.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
							.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
							.withField(Tags.tag168, xid).encode();

					Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
					Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
					Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
					Util.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
					Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
					Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
					Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
					Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
					Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
					Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
					Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
					Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
					Util.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
					Util.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
					Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
					Util.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
					Util.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
					Util.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
					Util.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
					Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
					Util.writeInFileTransaction(folder, file, "tag167_request : [" + champ_cavv + "]");
					Util.writeInFileTransaction(folder, file, "tag168_request : [" + xid + "]");

				} catch (Exception err4) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err4);

					return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during switch tlv buildup",
							"96");
				}

				Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

			}

			// reccuring
			if (is_reccuring) {
				Util.writeInFileTransaction(folder, file, "reccuring");
			}

			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
//			SwitchTCPClient sw = SwitchTCPClient.getInstance();
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {

				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				Util.writeInFileTransaction(folder, file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					return getMsgError(folder, file, jsonOrequest,
							"authorization 500 Error Switch communication s_conn false", "96");
				}

				if (s_conn) {
					Util.writeInFileTransaction(folder, file, "Switch Connected.");
					Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				return getMsgError(folder, file, jsonOrequest,
						"authorization 500 Error Switch communication UnknownHostException", "96");

			} catch (java.net.ConnectException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				return getMsgError(folder, file, jsonOrequest,
						"authorization 500 Error Switch communication ConnectException", "96");
			}

			catch (SocketTimeoutException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file,
						"authorization 500 Error Switch communication SocketTimeoutException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return getMsgError(folder, file, jsonOrequest, "Switch  malfunction  SocketTimeoutException !!!", "96");
			}

			catch (IOException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "authorization 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return getMsgError(folder, file, jsonOrequest, "Switch  malfunction  IOException !!!", "96");
			}

			catch (Exception e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				return getMsgError(folder, file, jsonOrequest,
						"authorization 500 Error Switch communication General Exception", "96");
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				Util.writeInFileTransaction(folder, file, "authorization 500 Error Switch null response" + "switch ip:["
						+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return getMsgError(folder, file, jsonOrequest, "Switch  malfunction resp null!!!", "96");
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				Util.writeInFileTransaction(folder, file, "authorization 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
			}

			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

			TLVParser tlvp = null;

			String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null, tag9_resp = null,
					tag14_resp = null, tag15_resp = null, tag16_resp = null, tag17_resp = null, tag66_resp = null,
					tag18_resp = null, tag19_resp = null, tag23_resp = null, tag20_resp = null, tag21_resp = null,
					tag22_resp = null, tag80_resp = null, tag98_resp = null;

			if (switch_ko == 0) {
				try {
					tlvp = new TLVParser(resp);

					tag0_resp = tlvp.getTag(Tags.tag0);
					tag1_resp = tlvp.getTag(Tags.tag1);
					tag3_resp = tlvp.getTag(Tags.tag3);
					tag8_resp = tlvp.getTag(Tags.tag8);
					tag9_resp = tlvp.getTag(Tags.tag9);
					tag14_resp = tlvp.getTag(Tags.tag14);
					tag15_resp = tlvp.getTag(Tags.tag15);
					tag16_resp = tlvp.getTag(Tags.tag16);
					tag17_resp = tlvp.getTag(Tags.tag17);
					tag66_resp = tlvp.getTag(Tags.tag66); // f1
					tag18_resp = tlvp.getTag(Tags.tag18);
					tag19_resp = tlvp.getTag(Tags.tag19); // f2
					tag23_resp = tlvp.getTag(Tags.tag23);
					tag20_resp = tlvp.getTag(Tags.tag20);
					tag21_resp = tlvp.getTag(Tags.tag21);
					tag22_resp = tlvp.getTag(Tags.tag22);
					tag80_resp = tlvp.getTag(Tags.tag80);
					tag98_resp = tlvp.getTag(Tags.tag98);

				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file, "authorization 500 Error during tlv Switch response parse"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// controle switch
				if (tag1_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag1_resp != null && tag1_resp.length() < 3) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag20_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}
			}
			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			Util.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

			String tag20_resp_verified = "";
			String tag19_res_verified = "";
			String tag66_resp_verified = "";
			tag20_resp_verified = tag20_resp;
			tag19_res_verified = tag19_resp;
			tag66_resp_verified = tag66_resp;
			String s_status, pan_auto = "";

			// SWHistoAutoDto swhist = null;

			if (switch_ko == 1) {
				pan_auto = Util.formatagePan(cardnumber);
				Util.writeInFileTransaction(folder, file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + merchantid);
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;

			Util.writeInFileTransaction(folder, file, "Insert into Histogate...");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

				Util.writeInFileTransaction(folder, file, "get status ...");

				s_status = "";
				try {
					CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
					System.out.println("codeReponseDto : " + codeReponseDto);
					Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
					if (codeReponseDto != null) {
						s_status = codeReponseDto.getRpcLibelle();
					}
				} catch (Exception ee) {
					Util.writeInFileTransaction(folder, file, "authorization 500 Error codeReponseDto null");
					ee.printStackTrace();
				}
				
				websiteid = dmd.getGalid();

				Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

				Util.writeInFileTransaction(folder, file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);
				Util.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");
				
				Util.writeInFileTransaction(folder, file, "websiteid : " + websiteid);

				Date current_date_1 = getDateWithoutTime(curren_date_hist);
				hist.setHatDatdem(current_date_1);

				hist.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
				hist.setHatMontant(Double.parseDouble(amount));
				hist.setHatNumcmr(merchantid);
				hist.setHatCoderep(tag20_resp_verified);
				tag20_resp = tag20_resp_verified;
				hist.setHatDevise(currency);
				hist.setHatBqcmr(acqcode);
				hist.setHatPorteur(pan_auto);
				hist.setHatMtfref1(s_status);
				hist.setHatNomdeandeur(websiteid);
				hist.setHatNautemt(tag19_res_verified); // f2
				tag19_resp = tag19_res_verified;
				if (tag22_resp != null)
					hist.setHatProcode(tag22_resp.charAt(0));
				else
					hist.setHatProcode('6');
				hist.setHatExpdate(expirydate);
				hist.setHatRepondeur(tag21_resp);
				hist.setHatTypmsg("3");
				hist.setHatRrn(tag66_resp_verified); // f1
				tag66_resp_verified = tag66_resp;
				hist.setHatEtat('E');
				if (websiteid.equals("")) {
					hist.setHatCodtpe("1");
				} else {
					hist.setHatCodtpe(websiteid);
				}
				hist.setHatMcc(merc_codeactivite);
				hist.setHatNumCommande(orderid);
				hist.setHatNumdem(new Long(numTransaction));

				if (check_cvv_presence(cvv)) {

					hist.setIs_cvv_verified("Y");
				} else {

					hist.setIs_cvv_verified("N");
				}

				hist.setIs_3ds("N");
				hist.setIs_addcard("N");
				// if (card_destination == 1)
				// hist.setIs_national("Y");
				// else
				// hist.setIs_national("N");
				hist.setIs_whitelist("N");
				hist.setIs_withsave("N");
				hist.setIs_tokenized("N");

				if (recurring.equalsIgnoreCase("Y"))
					hist.setIs_cof("Y");
				if (recurring.equalsIgnoreCase("N"))
					hist.setIs_cof("N");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

				histoAutoGateService.save(hist);
				
				Util.writeInFileTransaction(folder, file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]"
								+ e);
				try {
					Util.writeInFileTransaction(folder, file, "2eme tentative : HistoAutoGate Saving ... ");
					histoAutoGateService.save(hist);
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							"2eme tentative : authorization 500 Error during  insert in histoautogate for given orderid:["
									+ orderid + "]" + ex);
				}
			}

			Util.writeInFileTransaction(folder, file, "HistoAutoGate OK.");

			if (tag20_resp == null) {
				tag20_resp = "";
			}

			if (tag20_resp.equalsIgnoreCase("00"))

			{
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[00]");
				try {
					Util.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE ...");

					dmd.setEtat_demande("SW_PAYE");
					demandePaiementService.save(dmd);
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
									+ orderid + "]" + e);
				}

				Util.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE OK");

				String capture_status = "N";
				int exp_flag = 0;

				if (capture.equalsIgnoreCase("Y")) {
					// Si transactiontype = 0 (payement) on fait la telecollecte automatic
					if (!transactiontype.equalsIgnoreCase("P")) {

						Date current_date = null;
						current_date = new Date();
						Util.writeInFileTransaction(folder, file, "Automatic capture start...");

						Util.writeInFileTransaction(folder, file, "Getting authnumber");

						String authnumber = hist.getHatNautemt();
						Util.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

						Util.writeInFileTransaction(folder, file, "Getting authnumber");
						TransactionDto trs_check = null;

						try {
							trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
						} catch (Exception ee) {

							Util.writeInFileTransaction(folder, file,
									"trs_check trs_check exception e : [" + ee.toString() + "]");
						}

						if (trs_check != null) {
							// do nothing
							Util.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
						} else {

							Util.writeInFileTransaction(folder, file, "inserting into telec start ...");
							try {

								// insert into telec

								TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

								long lidtelc = 0;

								if (n_tlc == null) {
									Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
									Integer idtelc = null;

									TelecollecteDto tlc = null;

									// insert into telec
									idtelc = telecollecteService.getMAX_ID();
									Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);

									lidtelc = idtelc.longValue() + 1;
									tlc = new TelecollecteDto();
									tlc.setTlc_numtlcolcte(lidtelc);

									tlc.setTlc_numtpe(hist.getHatCodtpe());

									tlc.setTlc_datcrtfich(current_date);
									tlc.setTlc_nbrtrans(new Double(1));
									tlc.setTlc_gest("N");

									tlc.setTlc_datremise(current_date);
									tlc.setTlc_numremise(new Double(lidtelc));
									// tlc.setTlc_numfich(new Double(0));
									String tmpattern = "HH:mm";
									SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
									String stm = sftm.format(current_date);
									tlc.setTlc_heuremise(stm);

									tlc.setTlc_codbq(acqcode);
									tlc.setTlc_numcmr(merchantid);
									tlc.setTlc_numtpe(websiteid);
									telecollecteService.save(tlc);

								} else {
									Util.writeInFileTransaction(folder, file, "n_tlc !=null ");

									lidtelc = n_tlc.getTlc_numtlcolcte();
									double nbr_trs = n_tlc.getTlc_nbrtrans();

									nbr_trs = nbr_trs + 1;

									n_tlc.setTlc_nbrtrans(nbr_trs);

									telecollecteService.save(n_tlc);

								}

								// insert into transaction
								TransactionDto trs = new TransactionDto();
								trs.setTrsnumcmr(merchantid);
								trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));

								String frmt_cardnumber = Util.formatagePan(cardnumber);
								trs.setTrs_codporteur(frmt_cardnumber);

								double dmnt = 0;

								dmnt = Double.parseDouble(amount);

								trs.setTrs_montant(dmnt);
								// trs.setTrs_dattrans(new Date());

								current_date = new Date();
								Date current_date_1 = getDateWithoutTime(current_date);
								trs.setTrs_dattrans(current_date_1);

								trs.setTrsnumaut(authnumber);
								trs.setTrs_etat("N");
								trs.setTrs_devise(hist.getHatDevise());
								trs.setTrs_certif("N");
								Integer idtrs = transactionService.getMAX_ID();
								long lidtrs = idtrs.longValue() + 1;
								trs.setTrs_id(lidtrs);
								trs.setTrs_commande(orderid);
								trs.setTrs_procod("0");
								trs.setTrs_groupe(websiteid);
								transactionService.save(trs);

								hist.setHatEtat('T');
								hist.setHatdatetlc(current_date);
								hist.setOperateurtlc("mxplusapi");
								histoAutoGateService.save(hist);

								capture_id = String.format("%040d",
										new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
								Date dt = new Date();
								String dtpattern = "yyyy-MM-dd";
								SimpleDateFormat sfdt = new SimpleDateFormat(dtpattern);
								String sdt = sfdt.format(dt);
								String tmpattern = "HH:mm:ss";
								SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
								String stm = sftm.format(dt);
								Util.writeInFileTransaction(folder, file, "inserting into telec ok");
								capture_status = "Y";

							} catch (Exception e) {
								exp_flag = 1;
								Util.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing" + e);
							}
						}
						if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
							capture_status.equalsIgnoreCase("N");

						Util.writeInFileTransaction(folder, file, "Automatic capture end.");
					}
					// Si transactiontype = P (pre-auto) on fait pas la telecollecte automatic,
					// on le fait dans la confirmation de la pre-auto
				}

			} else {

				Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {

					Util.writeInFileTransaction(folder, file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtat_demande("SW_REJET");
					demandePaiementService.save(dmd);
					// old
					// hist.setHatEtat('A');
					// histoAutoGateService.save(hist);
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
									+ orderid + "]" + e);

					return getMsgError(folder, file, jsonOrequest,
							"authorization 500 Error during  DemandePaiement update SW_REJET", tag20_resp);
				}

				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to SW_REJET OK.");
				// 2024-02-27
				try {
					// get histoauto check if exist
					HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmr(orderid,
							merchantid);
					if (histToAnnulle != null) {
						Util.writeInFileTransaction(folder, file,
								"transaction declinded ==> update HistoAutoGateDto etat to A ...");
						histToAnnulle.setHatEtat('A');
						histoAutoGateService.save(histToAnnulle);
					} else {
						hist.setHatEtat('A');
						histoAutoGateService.save(hist);
					}
				} catch (Exception err2) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during HistoAutoGate findByNumAuthAndNumCommercant orderid:["
									+ orderid + "] and merchantid:[" + merchantid + "]" + err2);
				}
				Util.writeInFileTransaction(folder, file, "update HistoAutoGateDto etat to A OK.");
				// 2024-02-27
			}

			Util.writeInFileTransaction(folder, file, "Generating paymentid...");

			String uuid_paymentid, paymentid = "";
			try {
				uuid_paymentid = String.format("%040d",
						new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
				paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + e);
				return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during  paymentid generation",
						tag20_resp);
			}

			Util.writeInFileTransaction(folder, file, "Generating paymentid OK");
			Util.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

			// JSONObject jso = new JSONObject();

			Util.writeInFileTransaction(folder, file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDem_pan();
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 Error during authdata preparation orderid:[" + orderid + "]" + e);

				return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during authdata preparation",
						tag20_resp);
			}

			// reccurent transaction processing

			// reccurent insert and update

			try {

				// Transaction info
				jso.put("statuscode", coderep);
				jso.put("status", motif);
				jso.put("etataut", "Y");
				jso.put("orderid", orderid);
				jso.put("amount", amount);
				jso.put("transactiondate", date);
				jso.put("transactiontime", heure);
				jso.put("authnumber", authnumber);
				jso.put("paymentid", paymentid);
				jso.put("transactionid", transactionid);

				// Merchant info
				jso.put("merchantid", merchnatidauth);
				jso.put("merchantname", merchantname);
				jso.put("websitename", websiteName);
				jso.put("websiteid", websiteid);

				// Card info
				jso.put("cardnumber", Util.formatCard(cardnumber));

				// Client info
				jso.put("fname", fname);
				jso.put("lname", lname);
				jso.put("email", email);

			} catch (Exception jsouterr) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 Error during jso out processing given authnumber:[" + authnumber + "]"
								+ jsouterr);
				return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during jso out processing",
						tag20_resp);
			}

			System.out.println("autorization api response frictionless :  [" + jso.toString() + "]");
			Util.writeInFileTransaction(folder, file,
					"autorization api response frictionless :  [" + jso.toString() + "]");
			// fin
			// *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// ********************* Cas chalenge responseMPI equal C ou D
			// *********************
			Util.writeInFileTransaction(folder, file, "****** Cas chalenge responseMPI equal C ou D ******");
			try {

				// Transaction info
				// jso.put("statuscode", coderep);
				// jso.put("status", motif);
				jso.put("etataut", "C");
				jso.put("orderid", orderid);
				jso.put("amount", amount);
				jso.put("transactiondate", date);
				jso.put("transactiontime", heure);
				// jso.put("authnumber", authnumber);
				// jso.put("paymentid", paymentid);
				jso.put("transactionid", transactionid);

				// Merchant info
				jso.put("merchantid", merchantid);
				jso.put("merchantname", merchantname);
				jso.put("websitename", websiteName);
				jso.put("websiteid", websiteid);

				// Card info
				jso.put("cardnumber", Util.formatCard(cardnumber));

				// Client info
				jso.put("fname", fname);
				jso.put("lname", lname);
				jso.put("email", email);

				// Link ACS chalenge info
				jso.put("linkacs", link_chalenge + dmd.getTokencommande());

				// insertion htmlCreq dans la demandePaiement
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("SND_TO_ACS");
				demandePaiementService.save(dmd);

				System.out.println("link_chalenge " + link_chalenge + dmd.getTokencommande());
				Util.writeInFileTransaction(folder, file, "link_chalenge " + link_chalenge + dmd.getTokencommande());

				System.out.println("autorization api response chalenge :  [" + jso.toString() + "]");
				Util.writeInFileTransaction(folder, file,
						"autorization api response chalenge :  [" + jso.toString() + "]");
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file, "authorization 500 Error during jso out processing " + ex);

				return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during jso out processing ",
						null);
			}
		} else if (reponseMPI.equals("E")) {
			// ********************* Cas responseMPI equal E
			// *********************
			Util.writeInFileTransaction(folder, file, "****** Cas responseMPI equal E ******");
			Util.writeInFileTransaction(folder, file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
			dmd.setEtat_demande("MPI_DS_ERR");
			dmd.setDem_xid(threeDSServerTransID);
			demandePaiementService.save(dmd);

			// Transaction info
			jso.put("statuscode", "96");
			jso.put("status",
					"La transaction en cours n’a pas abouti (Problème authentification 3DSecure), votre compte ne sera pas débité, merci de contacter votre banque .");
			jso.put("etataut", "N");
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("transactiondate", date);
			jso.put("transactiontime", heure);
			jso.put("transactionid", transactionid);

			// Merchant info
			jso.put("merchantid", merchantid);
			jso.put("merchantname", merchantname);
			jso.put("websitename", websiteName);
			jso.put("websiteid", websiteid);

			// Card info
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// Client info
			jso.put("fname", fname);
			jso.put("lname", lname);
			jso.put("email", email);

			// Link ACS chalenge info :
			jso.put("linkacs", "");

			// System.out.println("link_fail " + link_fail + dmd.getTokencommande());
			// Util.writeInFileTransaction(folder, file, "link_fail " + link_fail +
			// dmd.getTokencommande());

			System.out.println("autorization api response fail :  [" + jso.toString() + "]");
			Util.writeInFileTransaction(folder, file, "autorization api response fail :  [" + jso.toString() + "]");
		} else {
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("MPI_CMR_INEX");
				demandePaiementService.save(dmd);
				// externalContext.redirect("operationErreur.xhtml?Error=".concat("COMMERCANT
				// NON PARAMETRE"));
				return getMsgError(folder, file, jsonOrequest, "COMMERCANT NON PARAMETRE", "15");
			case "BIN NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(folder, file, jsonOrequest, "BIN NON PARAMETREE", "96");
			case "DIRECTORY SERVER":
				Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(folder, file, jsonOrequest, "MPI_DS_ERR", "96");
			case "CARTE ERRONEE":
				Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(folder, file, jsonOrequest, "CARTE ERRONEE", "96");
			case "CARTE NON ENROLEE":
				Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(folder, file, jsonOrequest, "CARTE NON ENROLLE", "96");
			}
		}
		Util.writeInFileTransaction(folder, file, "*********** Fin authorization() ************** ");
		System.out.println("*********** Fin authorization() ************** ");
		return jso.toString();

	}

	@PostMapping(value = "/napspayment/linkpayment", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String getLink(@RequestHeader MultiValueMap<String, String> header, @RequestBody String linkP,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_LINK_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start getLink() ************** ");
		System.out.println("*********** Start getLink() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "getLink api call start ...");
		Util.writeInFileTransaction(folder, file, "getLink : [" + linkP + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(linkP);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "getLink 500 malformed json expression " + linkP + jserr);
			return getMsgError(folder, file, null, "getLink 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file,
						"getLink 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "getLink 500 malformed header", null);
			} else {
				Util.writeInFileTransaction(folder, file, "getLink 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "getLink 500 malformed header " + head_err.getMessage(), null);
			}
		}

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;

		String orderid, amount, merchantid, merchantname, websiteName, websiteid, recurring, country, phone, city,
				state, zipcode, address, expirydate, transactiondate, transactiontime, callbackUrl, fname, lname,
				email = "", securtoken24, mac_value, successURL, failURL, idDemande, id_client = "", token = "";
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			amount = (String) jsonOrequest.get("amount");
			recurring = (String) jsonOrequest.get("recurring");
			// securtoken24 = (String) jsonOrequest.get("securtoken24");
			// mac_value = (String) jsonOrequest.get("mac_value");

			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			// websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackUrl = (String) jsonOrequest.get("callbackurl");
			successURL = (String) jsonOrequest.get("successURL");
			failURL = (String) jsonOrequest.get("failURL");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");
			country = (String) jsonOrequest.get("country");
			phone = (String) jsonOrequest.get("phone");
			city = (String) jsonOrequest.get("city");
			state = (String) jsonOrequest.get("state");
			zipcode = (String) jsonOrequest.get("zipcode");
			address = (String) jsonOrequest.get("address");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "getLink 500 malformed json expression " + linkP + jerr);
			return getMsgError(folder, file, null, "getLink 500 malformed json expression " + jerr.getMessage(), null);
		}
		try {
			id_client = (String) jsonOrequest.get("id_client");
		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "getLink 500 malformed json expression " + linkP + jerr);
		}
		try {
			token = (String) jsonOrequest.get("token");
		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "getLink 500 malformed json expression " + linkP + jerr);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null && check_dmd.getEtat_demande().equals("SW_PAYE")) {
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Error Already exist in PaiementRequest", "16");
		}

		String url = "", status = "", statuscode = "";

		try {
			String tokencommande = "";
			if (check_dmd != null) {
				// generer token
				tokencommande = Util.genTokenCom(check_dmd.getCommande(), check_dmd.getComid());
				// url = link_success + check_dmd.getTokencommande();
				// statuscode = "00";
				// status = "OK";
				url = "";
				statuscode = "17";
				status = "PaiementRequest Already exist orderid:[" + orderid + "]";
				idDemande = String.valueOf(check_dmd.getIddemande());
			} else {
				dmd = new DemandePaiementDto();

				dmd.setComid(merchantid);
				dmd.setCommande(orderid);
				dmd.setId_client(id_client);
				dmd.setToken(token);
				dmd.setGalid(websiteid);
				dmd.setSuccessURL(successURL);
				dmd.setFailURL(failURL);
				dmd.setCallbackURL(callbackUrl);
				if (amount.equals("") || amount == null) {
					amount = "0";
				}
				if (amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				dmd.setMontant(Double.parseDouble(amount));
				if(lname.length() > 25) {
					lname = lname.substring(0, 25);
				}
				dmd.setNom(lname);
				if(fname.length() > 20) {
					fname = fname.substring(0, 20);
				}
				dmd.setPrenom(fname);
				dmd.setEmail(email);
				dmd.setTel(phone);
				dmd.setAddress(address);
				dmd.setCity(city);
				dmd.setCountry(country);
				dmd.setState(state);
				dmd.setPostcode(zipcode);
				dmd.setLangue("E");
				dmd.setEtat_demande("INIT");

				formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
				formatter_2 = new SimpleDateFormat("HH:mm:ss");
				trsdate = new Date();
				transactiondate = formatter_1.format(trsdate);
				transactiontime = formatter_2.format(trsdate);
				// dmd.setDem_date_time(transactiondate + transactiontime);
				dmd.setDem_date_time(dateFormat.format(new Date()));

				if (!id_client.equalsIgnoreCase("") || !token.equalsIgnoreCase("")) {
					dmd.setIs_cof("Y");
				} else {
					dmd.setIs_cof("N");
				}
				dmd.setIs_addcard("N");
				dmd.setIs_tokenized("N");
				dmd.setIs_whitelist("N");
				dmd.setIs_withsave("N");

				// generer token
				tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
				dmd.setTokencommande(tokencommande);

				dmdSaved = demandePaiementService.save(dmd);

				url = link_success + dmdSaved.getTokencommande();
				statuscode = "00";
				status = "OK";
				idDemande = String.valueOf(dmdSaved.getIddemande());
			}

		} catch (Exception err1) {
			url = "";
			statuscode = "";
			status = "KO";
			idDemande = "";
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Error during DEMANDE_PAIEMENT insertion", null);
		}

		JSONObject jso = new JSONObject();

		try {
			// Transaction info
			jso.put("statuscode", statuscode);
			jso.put("status", status);
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("idDemande", idDemande);
			jso.put("url", url);

			// Merchant info
			jso.put("merchantid", merchantid);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			Util.writeInFileTransaction(folder, file,
					"getLink 500 Error during jso out processing given orderid:[" + orderid + "]" + err8);

			return getMsgError(folder, file, jsonOrequest, "getLink 500 Error during jso out processing", null);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin getLink() ************** ");
		System.out.println("*********** Fin getLink() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/createtocken24", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String generateToken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String token24,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_TOKEN24_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start generateToken() ************** ");
		System.out.println("*********** Start generateToken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		// BasicConfigurator.configure();
		Util.writeInFileTransaction(folder, file, "token24 api call start ...");
		Util.writeInFileTransaction(folder, file, "token24 : [" + token24 + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(token24);
		} catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "token24 500 malformed json expression " + token24 + jserr);
			return getMsgError(folder, file, null, "token24 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		// check the header
		try {

			Util.writeInFileTransaction(folder, file, "token24 api header check ...");

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "token24 500 malformed header", null);
			} else {
				Util.writeInFileTransaction(folder, file, "token24 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "token24 500 malformed header " + head_err.getMessage(), null);
			}
		}

		String cx_user, cx_password, institution_id, cx_reason, error_msg, error_code, mac_value = "";
		try {
			// Merchant info
			cx_user = (String) jsonOrequest.get("cx_user");
			cx_password = (String) jsonOrequest.get("cx_password");
			cx_reason = (String) jsonOrequest.get("cx_reason");
			mac_value = (String) jsonOrequest.get("mac_value");
			institution_id = (String) jsonOrequest.get("institution_id");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "token24 500 malformed json expression " + token24 + jerr);
			return getMsgError(folder, file, null, "token24 500 malformed json expression " + jerr.getMessage(), null);
		}

		// pour tester la generation du tocken
		JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
		error_code = "";
		JSONObject jso = new JSONObject();
		JSONObject jsoVerified = new JSONObject();
		String token = "";

		if (mac_value.equals("")) {
			Util.writeInFileTransaction(folder, file, "the token generation failed, mac_value is empty");

			jsoVerified.put("error_msg", "the token generation failed, mac_value is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			Util.writeInFileTransaction(folder, file, "jsoVerified : " + jsoVerified.toString());
			System.out.println("jsoVerified : " + jsoVerified.toString());

			Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");

			return jsoVerified.toString();
		}
		if (cx_user.equals("")) {
			Util.writeInFileTransaction(folder, file, "the token generation failed, cx_user is empty");

			jsoVerified.put("error_msg", "the token generation failed, cx_user is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			Util.writeInFileTransaction(folder, file, "jsoVerified : " + jsoVerified.toString());
			System.out.println("jsoVerified : " + jsoVerified.toString());

			Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");

			return jsoVerified.toString();
		}
		if (cx_password.equals("")) {
			Util.writeInFileTransaction(folder, file, "the token generation failed, cx_password is empty");

			jsoVerified.put("error_msg", "the token generation failed, cx_password is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			Util.writeInFileTransaction(folder, file, "jsoVerified : " + jsoVerified.toString());
			System.out.println("jsoVerified : " + jsoVerified.toString());

			Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");

			return jsoVerified.toString();
		}
		if (institution_id.equals("")) {
			Util.writeInFileTransaction(folder, file, "the token generation failed, institution_id is empty");

			jsoVerified.put("error_msg", "the token generation failed, institution_id is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			Util.writeInFileTransaction(folder, file, "jsoVerified : " + jsoVerified.toString());
			System.out.println("jsoVerified : " + jsoVerified.toString());

			Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");

			return jsoVerified.toString();
		}

		try {

			// generate by user and secretkey shared with client
			// token = jwtTokenUtil.generateToken(cx_user, cx_password);

			// generate by user , secretkey and jwt_token_validity configured in app
			// properties
			token = jwtTokenUtil.generateToken(usernameToken, secret, jwt_token_validity);

			// verification expiration token
			jso = verifieToken(token, file);

			if (jso != null && !jso.get("statuscode").equals("00")) {
				jsoVerified.put("error_msg", "the token generation failed");
				jsoVerified.put("error_code", jso.get("statuscode"));
				jsoVerified.put("securtoken_24", "");
				jsoVerified.put("cx_user", cx_user);
				jsoVerified.put("mac_value", mac_value);
				jsoVerified.put("institution_id", institution_id);
				Util.writeInFileTransaction(folder, file, "jsoVerified : " + jsoVerified.toString());
				System.out.println("jsoVerified : " + jsoVerified.toString());
				Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");
				System.out.println("*********** Fin generateToken() ************** ");
				return jsoVerified.toString();
			} else {
				error_msg = "the token successfully generated";
				error_code = "00";
			}

		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file, "the token generation failed");
			error_msg = "the token generation failed";
			error_code = "17";
		}

		jsoVerified.put("error_msg", error_msg);
		jsoVerified.put("error_code", error_code);
		jsoVerified.put("securtoken_24", token);
		jsoVerified.put("cx_user", cx_user);
		jsoVerified.put("mac_value", mac_value);

		Util.writeInFileTransaction(folder, file, "json res : [" + jsoVerified.toString() + "]");
		System.out.println("json res : [" + jsoVerified.toString() + "]");

		// fin
		System.out.println("*********** Fin generateToken() ************** ");
		Util.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");

		return jsoVerified.toString();
	}

	@RequestMapping(value = "/napspayment/chalenge/token/{token}", method = RequestMethod.GET)
	public String chalengeapi(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CHALENGE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start chalengeapi ************** ");
		System.out.println("*********** Start chalengeapi ************** ");

		String page = "chalenge";

		Util.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto current_dem = demandePaiementService.findByTokencommande(token);
		String msgRefus = "Une erreur est survenue, merci de réessayer plus tard";

		if (current_dem != null) {
			Util.writeInFileTransaction(folder, file, "current_dem is exist OK");
			System.out.println("current_dem is exist OK");
			if (current_dem.getEtat_demande().equals("SW_PAYE") || current_dem.getEtat_demande().equals("PAYE")) {
				msgRefus = "La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else if (current_dem.getEtat_demande().equals("SW_REJET")) {
				msgRefus = "La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer .";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else {
				page = "chalenge";

				// String htmlCreq = "<form action='https://acs2.bankofafrica.ma:443/lacs2'
				// method='post'enctype='application/x-www-form-urlencoded'><input
				// type='hidden'name='creq'value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjllZjUwNjk3LWRiMTctNGZmMy04MDYzLTc0ZTAwMTk0N2I4YiIsCiAgImFjc1RyYW5zSUQiOiAiZjM2ZDA3ZWQtZGJhOS00ZTkzLWE2OGMtMzNmYjAyMDgxZDVmIiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0='/></form>";
				// current_dem.setCreq(htmlCreq);
				if (current_dem.getCreq().equals("")) {
					msgRefus = "La transaction en cours n’a pas abouti (Le lien de chalence acs est null), votre compte ne sera pas débité, merci de réessayer .";
					current_dem.setMsgRefus(msgRefus);
					Util.writeInFileTransaction(folder, file, "Le lien de chalence acs est null !!!");

					model.addAttribute("demandeDto", current_dem);
					page = "error";
				} else {
					System.out.println("current_dem htmlCreq : " + current_dem.getCreq());
					Util.writeInFileTransaction(folder, file, "current_dem htmlCreq : " + current_dem.getCreq());

					model.addAttribute("demandeDto", current_dem);
				}

			}
		} else {
			DemandePaiementDto demande = new DemandePaiementDto();
			msgRefus = "Votre commande est introuvable ";
			demande.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demande);
			Util.writeInFileTransaction(folder, file, "current_dem not found ");
			System.out.println("current_dem null ");
			page = "error";
		}

		System.out.println("return to " + page + ".html");

		Util.writeInFileTransaction(folder, file, "*********** Fin chalengeapi ************** ");
		System.out.println("*********** Fin chalengeapi ************** ");

		return page;
	}

	@PostMapping(value = "/napspayment/status", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String status(@RequestHeader MultiValueMap<String, String> header, @RequestBody String status,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_STATUS_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start status() ************** ");
		System.out.println("*********** Start status() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		// BasicConfigurator.configure();
		Util.writeInFileTransaction(folder, file, "status api call start ...");
		Util.writeInFileTransaction(folder, file, "status : [" + status + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(status);
		} catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "status 500 malformed json expression " + status + jserr);
			return getMsgError(folder, file, null, "status 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		// check the header
		try {

			Util.writeInFileTransaction(folder, file, "status api header check ...");

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "500 malformed header " + header.toString() + head_err);
				return getMsgError(folder, file, null, "status 500 malformed header", null);
			} else {
				Util.writeInFileTransaction(folder, file, "status 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "status 500 malformed header " + head_err.getMessage(), null);
			}
		}

		String orderid, authnumber, paymentid, amount, transactionid, merchantid = "", securtoken24, mac_value;
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			authnumber = (String) jsonOrequest.get("authnumber");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

			// Merchant info
			merchantid = (String) jsonOrequest.get("merchantid");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "status 500 malformed json expression " + status + jerr);
			return getMsgError(folder, file, null, "status 500 malformed json expression " + jerr.getMessage(), null);
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		Util.writeInFileTransaction(folder, file, "status_" + orderid + timeStamp);

		String status_ = "Uknown";
		String statuscode_ = "06";

		DemandePaiementDto current_dmd = null;

		String dcurrent_dmd, dtpattern, tmpattern, respcode, s_respcode = "";
		SimpleDateFormat sfdt, sftm = null;
		Date datdem, datetlc = null;
		Character E = '\0';

		JSONObject jso = new JSONObject();
		// verification expiration token
		jso = verifieToken(securtoken24, file);
		if (!jso.get("statuscode").equals("00")) {
			Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
			System.out.println("jso : " + jso.toString());
			Util.writeInFileTransaction(folder, file, "*********** Fin status() ************** ");
			System.out.println("*********** Fin status() ************** ");
			return jso.toString();
		}

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"status 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);
			return getMsgError(folder, file, jsonOrequest, "status 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			Util.writeInFileTransaction(folder, file, "status 500 PaiementRequest not found orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			return getMsgError(folder, file, jsonOrequest, "status 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		if (authnumber.length() < 1) {
			try {

				// get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmr(orderid, merchantid);

			} catch (Exception err2) {
				Util.writeInFileTransaction(folder, file,
						"status 500 Error during HistoAutoGate findByNumAuthAndNumCommercant orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err2);
				return getMsgError(folder, file, jsonOrequest, "status 500 Error during find HistoAutoGate", null);
			}
		} else {

			try {
				// get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
						merchantid);

			} catch (Exception err2) {
				Util.writeInFileTransaction(folder, file,
						"Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:[" + orderid
								+ "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]"
								+ err2);
				return getMsgError(folder, file, jsonOrequest, "status 500 Error during find HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			String dmd_etat = "";
			if (current_dmd.getEtat_demande() != null) {
				dmd_etat = current_dmd.getEtat_demande();
			}
			if (dmd_etat.equalsIgnoreCase("PAYE")) {
				Util.writeInFileTransaction(folder, file,
						"Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
								+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
								+ merchantid + "]");
				return getMsgError(folder, file, jsonOrequest, "status 500 Inconsitence HistoAutoGate not found", null);
			} else {

				E = 'X';

			}
		}

		char pr = '\0';

		try {

			dcurrent_dmd = current_dmd.getDem_date_time();
			dtpattern = "yyyy-MM-dd";
			sfdt = new SimpleDateFormat(dtpattern);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);

			datdem = current_hist.getHatDatdem();
			datetlc = current_hist.getHatdatetlc();
			E = current_hist.getHatEtat();
			pr = current_hist.getHatProcode();
			respcode = current_hist.getHatCoderep();
			// s_respcode = histservice.getLib("RPC_LIBELLE", "CODEREPONSE", "RPC_CODE='" +
			// respcode + "'");
			s_respcode = "";
		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file, "status 500 Error during status processing for given authnumber"
					+ " :[" + authnumber + "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(folder, file, jsonOrequest, "status 500 Error during status processing", null);
		}

		String sdt = "", stm = "", sdt1 = "", stm1 = "", sdt0 = "", stm0 = "";

		String rep_auto = "";
		if (current_hist != null)
			rep_auto = current_hist.getHatCoderep();

		if (E == 'E')
			if (rep_auto.equalsIgnoreCase("00")) {
				status_ = "Paid";
				statuscode_ = "00";

				String spr = pr + "";
				if (spr.equalsIgnoreCase("4")) {

					status_ = "Refunded";
					statuscode_ = "07";
				}

			}

		if (E == 'E')
			if (!rep_auto.equalsIgnoreCase("00")) {
				status_ = "Declinded";
				statuscode_ = "03";

				String spr = pr + "";
				if (spr.equalsIgnoreCase("4")) {

					status_ = "Refund_Declined";
					statuscode_ = "08";
				}

			}

		if (E == 'X') {

			status_ = "Declinded";
			statuscode_ = "03";
		}

		if (E == 'A') {
			status_ = "Canceled";
			statuscode_ = "04";
		}

		String trs_state = "";
		TransactionDto trs_check = null;

		if (E == 'T') {
			try {

				trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);

			} catch (Exception err4) {
				Util.writeInFileTransaction(folder, file,
						"status 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber" + " :["
								+ authnumber + "] and merchantid:[" + merchantid + "]" + err4);

				return getMsgError(folder, file, jsonOrequest, "status 500 Error during Transaction", null);
			}
		}

		if (E == 'T')
			if (trs_check != null) {

				status_ = "Captured";
				statuscode_ = "01";

			}

		if (E == 'T')
			if (trs_check != null)
				trs_state = trs_check.getTrs_etat();

		if (E == 'T')
			if (trs_check != null)
				if (trs_state == null) {

					status_ = "Uknown";
					statuscode_ = "06";

				}

		if (E == 'T')
			if (trs_check != null)
				if (trs_state.equalsIgnoreCase("N")) {

					status_ = "Captured";
					statuscode_ = "01";
				}

		if (E == 'T')
			if (trs_check != null)
				if (trs_state.equalsIgnoreCase("E")) {

					status_ = "Settled";
					statuscode_ = "02";
				}

		String dmd_status = current_dmd.getEtat_demande();
		if (dmd_status.equalsIgnoreCase("R")) {
			status_ = "Refunded";
			statuscode_ = "07";

		}

		try {

			jso.put("status", status_);
			// jso.put("status", "Declined");
			// jso.put("status", "Captured");
			// jso.put("status", "Settled");
			// jso.put("status", "NotExist");
			// jso.put("status", "Canceled");
			jso.put("statuscode", statuscode_);
			// jso.put("statuscode", "01");
			// jso.put("statuscode", "02");
			// jso.put("statuscode", "03");
			// jso.put("statuscode", "04");
			// jso.put("statuscode", "05");

			jso.put("authstatuscode", respcode);
			jso.put("authstatus", s_respcode);

			if (dcurrent_dmd == null) {
				jso.put("transactiondate", "");
				jso.put("transactiontime", "");

			} else {

				if (dcurrent_dmd.length() < 16) {

					jso.put("transactiondate", "");
					jso.put("transactiontime", "");
				} else {

					SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						Date date_dmd = parser.parse(dcurrent_dmd);
						sdt0 = sfdt.format(date_dmd);
						stm0 = sftm.format(date_dmd);
						jso.put("transactiondate", sdt0);
						jso.put("transactiontime", stm0);

					} catch (ParseException e) {
						jso.put("transactiondate", "");
						jso.put("transactiontime", "");
					}

				}

			}

			if (datdem != null) {

				sdt = sfdt.format(datdem);
				stm = sftm.format(datdem);

				jso.put("authdate", sdt);
				jso.put("authtime", stm);

			} else {
				jso.put("authdate", "");
				jso.put("authtime", "");

			}

			if (datetlc != null) {

				sdt1 = sfdt.format(datetlc);
				stm1 = sftm.format(datetlc);

				jso.put("capturedate", sdt1);
				jso.put("capturetime", stm1);

			} else {
				jso.put("capturedate", "");
				jso.put("capturetime", "");

			}

			jso.put("settlementdate", "");
			jso.put("settlementtime", "");
			jso.put("authnumber", authnumber);
			jso.put("transactionid", transactionid);
			jso.put("merchantid", merchantid);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception err3) {
			err3.printStackTrace();
			Util.writeInFileTransaction(folder, file, "status 500 Error during jso out processing for given authnumber "
					+ " :[" + authnumber + "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(folder, file, jsonOrequest, "status 500 Error during jso out processing", null);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin status() ************** ");
		System.out.println("*********** Fin status() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/capture", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String capture(@RequestHeader MultiValueMap<String, String> header, @RequestBody String capture,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CAPTURE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start capture() ************** ");
		System.out.println("*********** Start capture() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "capture api call start ...");
		Util.writeInFileTransaction(folder, file, "capture : [" + capture + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(capture);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "capture 500 malformed json expression " + capture + jserr);
			return getMsgError(folder, file, null, "capture 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file,
						"capture 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "capture 500 malformed header", null);
			} else {
				Util.writeInFileTransaction(folder, file, "capture 500 malformed header " + head_err);
				return getMsgError(folder, file, null, "capture 500 malformed header " + head_err.getMessage(), null);
			}
		}

		String orderid, paymentid, amount, authnumber, transactionid, merchantid, merchantname, websiteName, websiteid,
				callbackUrl, cardnumber, fname, lname, email = "", securtoken24, mac_value;

		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			authnumber = (String) jsonOrequest.get("authnumber");
			transactionid = (String) jsonOrequest.get("transactionid");
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

			// Merchant info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackUrl = (String) jsonOrequest.get("callbackurl");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "capture 500 malformed json expression " + capture + jerr);
			return getMsgError(folder, file, null, "capture 500 malformed json expression " + jerr.getMessage(), null);
		}

		JSONObject jso = new JSONObject();
		// verification expiration token
		jso = verifieToken(securtoken24, file);
		if (!jso.get("statuscode").equals("00")) {
			Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
			System.out.println("jso : " + jso.toString());
			Util.writeInFileTransaction(folder, file, "*********** Fin capture() ************** ");
			System.out.println("*********** Fin capture() ************** ");
			return jso.toString();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		Util.writeInFileTransaction(folder, file, "capture_" + orderid + timeStamp);
		// get demandepaiement id , check if exist

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);
			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			Util.writeInFileTransaction(folder, file, "captue 500 PaiementRequest not found for orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			return getMsgError(folder, file, jsonOrequest, "captue 500 PaiementRequest not found", null);
		}

		// get histoauto check if exist

		HistoAutoGateDto current_hist = null;

		try {

			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err2);
			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during HistoAutoGate", null);
		}

		if (current_hist == null) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "capture 500 Inconsitence HistoAutoGate not found", null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "capture 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "capture 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "capture 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		// check if already telecollected

		TransactionDto trs_check = null;

		try {

			trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);

		} catch (Exception err4) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber:["
							+ authnumber + "] and merchantid:[" + merchantid + "]" + err4);

			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during Transaction", null);
		}

		if (trs_check != null) {
			Util.writeInFileTransaction(folder, file, "capture 500 Transaction already captured  for given "
					+ "authnumber:[" + authnumber + "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "500 Transaction already captured", null);
		}

		TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);
		Date current_date = null;
		current_date = new Date();
		long lidtelc = 0;

		if (n_tlc == null) {
			Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
			Integer idtelc = null;

			TelecollecteDto tlc = null;

			try {
				// insert into telec
				// idtelc = tlcservice.getMAX_ID("TELECOLLECTE", "TLC_NUMTLCOLCTE");
				idtelc = telecollecteService.getMAX_ID();
				Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);

				lidtelc = idtelc.longValue() + 1;
				tlc = new TelecollecteDto();
				tlc.setTlc_numtlcolcte(lidtelc);
				tlc.setTlc_numtpe(current_hist.getHatCodtpe());

				tlc.setTlc_datcrtfich(current_date);
				tlc.setTlc_nbrtrans(new Double(1));
				tlc.setTlc_gest("N");

				tlc.setTlc_datremise(current_date);
				tlc.setTlc_numremise(new Double(lidtelc));
				// tlc.setTlc_numfich(new Double(0));
				String tmpattern = "HH:mm";
				SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
				String stm = sftm.format(current_date);
				tlc.setTlc_heuremise(stm);

				tlc.setTlc_codbq(acqcode);
				tlc.setTlc_numcmr(merchantid);
				tlc.setTlc_numtpe(websiteid);

				telecollecteService.save(tlc);

			} catch (Exception err5) {
				Util.writeInFileTransaction(folder, file,
						"capture 500 Error during insert into telec for given authnumber:[" + authnumber
								+ "] and merchantid:[" + merchantid + "]" + err5);

				return getMsgError(folder, file, jsonOrequest, "capture 500 Error during insert into telecollecte",
						null);
			}
		} else {

			lidtelc = n_tlc.getTlc_numtlcolcte();
			double nbr_trs = n_tlc.getTlc_nbrtrans();

			nbr_trs = nbr_trs + 1;

			n_tlc.setTlc_nbrtrans(nbr_trs);

			try {

				telecollecteService.save(n_tlc);
			} catch (Exception err55) {
				Util.writeInFileTransaction(folder, file, "capture 500 Error during update telec for given authnumber:["
						+ authnumber + "] and merchantid:[" + merchantid + "]" + err55);

				return getMsgError(folder, file, jsonOrequest, "capture 500 Error during update telecollecte", null);
			}
		}

		TransactionDto trs = null;
		String frmt_cardnumber = "";
		double dmnt = 0;
		Integer idtrs = null;
		long lidtrs = 0;
		// insert into transaction
		try {
			trs = new TransactionDto();
			trs.setTrsnumcmr(merchantid);
			trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));
			frmt_cardnumber = Util.formatagePan(cardnumber);
			trs.setTrs_codporteur(frmt_cardnumber);
			dmnt = Double.parseDouble(amount);
			trs.setTrs_montant(dmnt);
			// trs.setTrs_dattrans(new Date());
			current_date = new Date();
			Date current_date_1 = getDateWithoutTime(current_date);
			trs.setTrs_dattrans(current_date_1);
			trs.setTrsnumaut(authnumber);
			trs.setTrs_etat("N");
			trs.setTrs_devise(current_hist.getHatDevise());
			trs.setTrs_certif("N");
			idtrs = transactionService.getMAX_ID();
			lidtrs = idtrs.longValue() + 1;
			trs.setTrs_id(lidtrs);
			trs.setTrs_commande(orderid);

			trs.setTrs_procod("0");
			trs.setTrs_groupe(websiteid);
			transactionService.save(trs);

		} catch (Exception err6) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Error during insert into transaction for given authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err6);

			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during insert into transaction", null);
		}

		try {
			current_hist.setHatEtat('T');
			current_hist.setHatdatetlc(current_date);
			current_hist.setOperateurtlc("mxplusapi");
			histoAutoGateService.save(current_hist);

		} catch (Exception err7) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Error during histoauto_gate update for given authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err7);

			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during histoauto_gate update", null);
		}

		String capture_id, dtpattern, sdt, tmpattern, stm = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;

		try {

			capture_id = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			dt = new Date();
			dtpattern = "yyyy-MM-dd";
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);

		} catch (Exception err8) {
			Util.writeInFileTransaction(folder, file,
					"capture 500 Error during jso data preparationfor given authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err8);

			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during jso data preparation", null);
		}

		try {
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("capturedate", sdt);
			jso.put("capturetime", stm);
			jso.put("capture_id", capture_id);
			jso.put("capture_state", "Y");
			jso.put("capture_label", "Captured");
			jso.put("transactionid", transactionid);

			jso.put("merchantid", merchantid);
			jso.put("merchantname", merchantname);
			jso.put("websitename", websiteName);
			jso.put("websiteid", websiteid);
			jso.put("cardnumber", Util.formatCard(cardnumber));
			jso.put("fname", fname);
			jso.put("lname", lname);
			jso.put("email", email);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception err9) {
			Util.writeInFileTransaction(folder, file, "capture 500 Error during jso out processing given "
					+ "authnumber:[" + authnumber + "] and merchantid:[" + merchantid + "]" + err9);

			return getMsgError(folder, file, jsonOrequest, "capture 500 Error during jso out processing", null);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin capture() ************** ");
		System.out.println("*********** Fin capture() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/refund", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String refund(@RequestHeader MultiValueMap<String, String> header, @RequestBody String refund,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_REFUND_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start refund() ************** ");
		System.out.println("*********** Start refund() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "refund api call start ...");
		Util.writeInFileTransaction(folder, file, "refund : [" + refund + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(refund);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "refund 500 malformed json expression " + refund + jserr);
			return getMsgError(folder, file, null, "refund 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "refund 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "refund 500 malformed header" + head_err.getMessage(), null);
			} else {
				Util.writeInFileTransaction(folder, file, "refund 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "refund 500 malformed header " + head_err.getMessage(), null);
			}
		}

		String orderid, authnumber, paymentid, amount, transactionid, merchantid, merchantname, websiteName, websiteid,
				callbackUrl, cardnumber, fname, lname, email = "", securtoken24, mac_value;
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			authnumber = (String) jsonOrequest.get("authnumber");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

			// Merchant info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackUrl = (String) jsonOrequest.get("callbackurl");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "refund 500 malformed json expression " + refund + jerr);
			return getMsgError(folder, file, null, "refund 500 malformed json expression " + jerr.getMessage(), null);
		}

		JSONObject jso = new JSONObject();
		// verification expiration token
		jso = verifieToken(securtoken24, file);
		if (!jso.get("statuscode").equals("00")) {
			Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
			System.out.println("jso : " + jso.toString());
			Util.writeInFileTransaction(folder, file, "*********** Fin refund() ************** ");
			System.out.println("*********** Fin refund() ************** ");
			return jso.toString();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		Util.writeInFileTransaction(folder, file, "refund_" + orderid + timeStamp);

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);
			return getMsgError(folder, file, jsonOrequest, "refund 500 Error during PaiementRequest", null);
		}
		if (current_dmd == null) {
			Util.writeInFileTransaction(folder, file, "refund 500 PaiementRequest not found for given orderid" + orderid
					+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "refund 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		try {
			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(folder, file, jsonOrequest, "refund 500 Error during find HistoAutoGate", null);
		}

		if (current_hist == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "refund 500 Inconsitence HistoAutoGate not found", null);
		}

		TransactionDto trs_check = null;

		try {

			trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);

		} catch (Exception err4) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber:["
							+ authnumber + "] and merchantid:[" + merchantid + "]" + err4);

			return getMsgError(folder, file, jsonOrequest, "refund 500 Error during Transaction", null);
		}

		if (trs_check == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction not found for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "refund 500 Inconsitence Captured Transaction not found",
					null);
		}

		String trs_procod = trs_check.getTrs_procod();
		String trs_state = trs_check.getTrs_etat();

		if (trs_procod == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_procod null for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction trs_procod null  for authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"refund 500 Inconsitence Captured Transaction trs_procod null", null);
		}

		if (trs_state == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_procod null for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction trs_state null for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"refund 500 Inconsitence Captured Transaction trs_procod null", null);
		}

		if (!trs_procod.equalsIgnoreCase("0")) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_procod <> 0 for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction trs_procod <> 0   for authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"refund 500 Inconsitence Captured Transaction trs_procod <> 0", null);
		}

		if (!trs_state.equalsIgnoreCase("E")) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_state <> E for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction  trs_state <> E  for authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"refund 500 Inconsitence Captured Transaction trs_state <> E", null);
		}

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("yyyy-MM-dd");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";

		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file, "refund 500 Error during date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(folder, file, jsonOrequest, "refund 500 Error during date formatting", null);
		}

		String[] mm;
		String[] m;
		String montanttrame = "";

		// 2024-03-05
		montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, jsonOrequest);

		Util.writeInFileTransaction(folder, file, "Switch processing start ...");

		String tlv = "";
		Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		// offline processing

		String tag20_resp = "00"; // Accept all refund offline mode
		String s_status = "Refunded offline";

		Util.writeInFileTransaction(folder, file, "Switch status : [" + s_status + "]");

		if (tag20_resp.equalsIgnoreCase("00"))

		{
			Util.writeInFileTransaction(folder, file, "Switch CODE REP : [00]");

			Util.writeInFileTransaction(folder, file, "Transaction refunded.");

			try {
				Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status A ...");

				current_dmd.setEtat_demande("R");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"refund 500 Error during  demandepaiement update  A for given  orderid:[" + orderid + "]" + e);

				return getMsgError(folder, file, jsonOrequest, "refund 500 Error during  demandepaiement update A",
						null);
			}

			Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status OK.");

			Util.writeInFileTransaction(folder, file, "inserting HistoAutoGate   ...");

			try {

				// Telecollecte n_tlc = tlcservice.getMAXTLC_NRefund(merchantid);
				TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);
				Date current_date = null;
				current_date = new Date();
				long lidtelc = 0;

				if (n_tlc == null) {
					Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
					Integer idtelc = null;

					TelecollecteDto tlc = null;

					// insert into telec
					idtelc = telecollecteService.getMAX_ID();
					Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);

					if (idtelc != null) {
						lidtelc = idtelc.longValue() + 1;
					} else {
						lidtelc = 1;
					}
					tlc = new TelecollecteDto();
					tlc.setTlc_numtlcolcte(lidtelc);
					tlc.setTlc_numtpe(current_hist.getHatCodtpe());
					// tlc.setTlc_typentre("REFUND");
					tlc.setTlc_datcrtfich(current_date);
					tlc.setTlc_nbrtrans(new Double(1));
					tlc.setTlc_gest("N");

					tlc.setTlc_datremise(current_date);
					tlc.setTlc_numremise(new Double(lidtelc));
					// tlc.setTlc_numfich(new Double(0));
					String tmpattern = "HH:mm";
					SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
					String stm = sftm.format(current_date);
					tlc.setTlc_heuremise(stm);
					String acqcode = current_merchant.getCmrCodbqe();
					tlc.setTlc_codbq(acqcode);
					tlc.setTlc_numcmr(merchantid);
					tlc.setTlc_numtpe(websiteid);

					telecollecteService.save(tlc);

				} else {

					lidtelc = n_tlc.getTlc_numtlcolcte();
					double nbr_trs = n_tlc.getTlc_nbrtrans();

					nbr_trs = nbr_trs + 1;

					n_tlc.setTlc_nbrtrans(nbr_trs);

				}

				TransactionDto trs = null;
				String frmt_cardnumber = "";
				double dmnt = 0;
				Integer idtrs = null;
				long lidtrs = 0;
				// insert into transaction
				trs = new TransactionDto();
				trs.setTrsnumcmr(merchantid);
				trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));
				frmt_cardnumber = Util.formatagePan(cardnumber);
				trs.setTrs_codporteur(frmt_cardnumber);
				dmnt = Double.parseDouble(amount);
				trs.setTrs_montant(dmnt);
				// trs.setTrs_dattrans(new Date());
				current_date = new Date();
				Date current_date_1 = getDateWithoutTime(current_date);
				trs.setTrs_dattrans(current_date_1);
				trs.setTrsnumaut("000000"); // trs.setTrs_numaut(authnumber);
				trs.setTrs_etat("N");
				trs.setTrs_devise(current_hist.getHatDevise());
				trs.setTrs_certif("N");
				idtrs = transactionService.getMAX_ID();

				if (idtrs != null) {
					lidtrs = idtrs.longValue() + 1;
				} else {
					lidtrs = 1;
				}

				trs.setTrs_id(lidtrs);
				trs.setTrs_commande(orderid);

				trs.setTrs_procod("9");
				trs.setTrs_groupe(websiteid);
				transactionService.save(trs);

			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"refund 500 Error during  HistoAutoGate insertion or Transaction insertion A for given orderid:["
								+ orderid + "]" + e);

				return getMsgError(folder, file, jsonOrequest,
						"refund 500 Error during  HistoAutoGate insertion or Transaction insertion A", null);
			}

			Util.writeInFileTransaction(folder, file, "inserting HistoAutoGate  OK.");

		} /*
			 * else { //offline processing
			 * 
			 * }
			 */

		String refund_id = "";

		try {
			refund_id = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"refund 500 Error during  refund_id generation for given orderid:[" + orderid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "refund 500 Error during  refund_id generation", null);
		}

		try {
			// Transaction info
			jso.put("statuscode", tag20_resp);
			jso.put("status", s_status);
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("refunddate", date);
			jso.put("rrefundtime", heure);
			jso.put("authnumber", authnumber);
			jso.put("refundid", refund_id);
			jso.put("transactionid", transactionid);

			// Merchant info
			jso.put("merchantid", merchantid);
			jso.put("merchantname", merchantname);
			jso.put("websitename", websiteName);
			jso.put("websiteid", websiteid);
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// Client info
			jso.put("fname", fname);
			jso.put("lname", lname);
			jso.put("email", email);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			Util.writeInFileTransaction(folder, file, "refund 500 Error during jso out processing given authnumber"
					+ "authnumber:[" + authnumber + "]" + err8);

			return getMsgError(folder, file, jsonOrequest, "refund 500 Error during jso out processing", null);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin refund() ************** ");
		System.out.println("*********** Fin refund() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/reversal", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String reversal(@RequestHeader MultiValueMap<String, String> header, @RequestBody String reversal,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_REVERSAL_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start reversal() ************** ");
		System.out.println("*********** Start reversal() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "reversal api call start ...");
		Util.writeInFileTransaction(folder, file, "reversal : [" + reversal + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(reversal);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "reversal 500 malformed json expression " + reversal + jserr);
			return getMsgError(folder, file, null, "reversal 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "reversal 500 malformed header " + head_err.getMessage(), null);
			} else {
				Util.writeInFileTransaction(folder, file, "reversal 500 malformed header" + head_err);
			}
		}

		String orderid, authnumber, paymentid, amount, transactionid, merchantid, merchantname, websiteName, websiteid,
				callbackUrl, cardnumber, fname, lname, email = "", securtoken24, mac_value;

		try {
			// Reversal info
			orderid = (String) jsonOrequest.get("orderid");
			authnumber = (String) jsonOrequest.get("authnumber");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

			// Merchant info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackUrl = (String) jsonOrequest.get("callbackurl");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "reversal 500 malformed json expression " + reversal + jerr);
			return getMsgError(folder, file, null, "reversal 500 malformed json expression " + jerr.getMessage(), null);
		}

		JSONObject jso = new JSONObject();
		// verification expiration token
		jso = verifieToken(securtoken24, file);
		if (!jso.get("statuscode").equals("00")) {
			Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
			System.out.println("jso : " + jso.toString());
			Util.writeInFileTransaction(folder, file, "*********** Fin reversal() ************** ");
			System.out.println("*********** Fin reversal() ************** ");
			return jso.toString();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		Util.writeInFileTransaction(folder, file, "reversal_" + orderid + timeStamp);
		// get demandepaiement id , check if exist

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during PaiementRequest", null);

		}
		if (current_dmd == null) {
			Util.writeInFileTransaction(folder, file, "reversal 500 PaiementRequest not found for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "reversal 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		try {

			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during HistoAutoGate", null);
		}

		if (current_hist == null) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Inconsitence HistoAutoGate not found", null);
		}

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";
			cardnumber = current_dmd.getDem_pan();
		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file, "reversal 500 Error during date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during date formatting", null);
		}

		String[] mm;
		String[] m;
		String montanttrame = "";

		// 2024-03-05
		montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, jsonOrequest);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		Util.writeInFileTransaction(folder, file, "Switch processing start ...");

		String tlv = "";
		Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

		// controls
		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		String mesg_type = "2";
		String merchant_name = merchantname;
		String acq_type = "0000";
		String processing_code = "0";
		String reason_code = "H";
		String transaction_condition = "6";
		String transactionnumber = authnumber;
		merchant_name = Util.pad_merchant(merchantname, 19, ' ');
		String merchant_city = "MOROCCO        ";

		try {

			String currency = current_hist.getHatDevise();
			String expirydate = current_hist.getHatExpdate();
			String rrn = current_hist.getHatRrn();
			transactionnumber = rrn;
			tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
					.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
					.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame).withField(Tags.tag15, currency)
					.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate).withField(Tags.tag16, date)
					.withField(Tags.tag17, heure).withField(Tags.tag10, merc_codeactivite)
					.withField(Tags.tag8, "0" + merchantid).withField(Tags.tag9, merchantid)
					.withField(Tags.tag66, transactionnumber).withField(Tags.tag11, merchant_name)
					.withField(Tags.tag12, merchant_city).withField(Tags.tag13, "MAR")
					.withField(Tags.tag23, reason_code).withField(Tags.tag90, acqcode).withField(Tags.tag19, authnumber)
					.encode();

			Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
			Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
			Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
			Util.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
			Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
			Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
			Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
			Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
			Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
			Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
			Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
			Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
			Util.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
			Util.writeInFileTransaction(folder, file, "tag8_request : [0+" + merchantid + "]");
			Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
			Util.writeInFileTransaction(folder, file, "tag66_request : [" + transactionnumber + "]");
			Util.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
			Util.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
			Util.writeInFileTransaction(folder, file, "tag13_request : [MAR]");
			Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
			Util.writeInFileTransaction(folder, file, "tag19_request : [" + authnumber + "]");

		} catch (Exception err4) {
			Util.writeInFileTransaction(folder, file, "reversal 500 Error during switch tlv buildu for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err4);

			return getMsgError(folder, file, jsonOrequest, "reversa 500 Error during switch tlv buildu", "96");
		}

		Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

		Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

		Util.writeInFileTransaction(folder, file, "Switch Connecting ...");

		String resp_tlv = "";
		SwitchTCPClient sw = SwitchTCPClient.getInstance();

		int port = 0;
		String sw_s = "", s_port = "";
		try {

			s_port = portSwitch;
			sw_s = ipSwitch;

			Util.writeInFileTransaction(folder, file, "Switch IP / Switch PORT : " + sw_s + "/" + s_port);

			port = Integer.parseInt(s_port);

			boolean s_conn = sw.startConnection(sw_s, port);
			Util.writeInFileTransaction(folder, file, "Switch Connecting ...");

			if (s_conn) {
				Util.writeInFileTransaction(folder, file, "Switch Connected.");
				Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

				resp_tlv = sw.sendMessage(tlv);

				Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
				sw.stopConnection();

			} else {

				Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

				return getMsgError(folder, file, jsonOrequest, "reversal 500 Error Switch communication s_conn false",
						"96");
			}

		} catch (SocketTimeoutException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Error Switch communication SocketTimeoutException", "96");
		} catch (UnknownHostException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Error Switch communication UnknownHostException", "96");
		}

		catch (IOException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error Switch communication IOException", "96");
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);

			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Error Switch communication General Exception switch", "96");
		}

		String resp = resp_tlv;
		if (resp == null) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error Switch null response switch", "96");
		}

		if (resp.length() < 3)

		{

			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Error Switch short response length() < 3 switch", "96");
		}

		Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

		Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

		// resp debug =
		// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

		TLVParser tlvp = null;

		// resp debug =
		// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

		String tag0_resp, tag1_resp, tag3_resp, tag8_resp, tag9_resp, tag14_resp, tag15_resp, tag16_resp, tag17_resp,
				tag66_resp, tag18_resp, tag19_resp, tag23_resp, tag20_resp, tag21_resp, tag22_resp, tag80_resp,
				tag98_resp = "";

		try {
			tlvp = new TLVParser(resp);

			tag0_resp = tlvp.getTag(Tags.tag0);
			tag1_resp = tlvp.getTag(Tags.tag1);
			tag3_resp = tlvp.getTag(Tags.tag3);
			tag8_resp = tlvp.getTag(Tags.tag8);
			tag9_resp = tlvp.getTag(Tags.tag9);
			tag14_resp = tlvp.getTag(Tags.tag14);
			tag15_resp = tlvp.getTag(Tags.tag15);
			tag16_resp = tlvp.getTag(Tags.tag16);
			tag17_resp = tlvp.getTag(Tags.tag17);
			tag66_resp = tlvp.getTag(Tags.tag66); // f1
			tag18_resp = tlvp.getTag(Tags.tag18);
			tag19_resp = tlvp.getTag(Tags.tag19); // f2
			tag23_resp = tlvp.getTag(Tags.tag23);
			tag20_resp = tlvp.getTag(Tags.tag20);
			tag21_resp = tlvp.getTag(Tags.tag21);
			tag22_resp = tlvp.getTag(Tags.tag22);
			tag80_resp = tlvp.getTag(Tags.tag80);
			tag98_resp = tlvp.getTag(Tags.tag98);

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during tlv Switch response parse switch",
					"96");
		}

		// controle switch
		if (tag1_resp == null) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Error during tlv Switch response parse tag1_resp tag null", "96");
		}

		if (tag1_resp.length() < 3) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Error during tlv Switch response parse tag1_resp length tag  < 3 switch", "96");
		}

		Util.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
		Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

		Util.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
		Util.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

		if (tag20_resp == null) {
			return getMsgError(folder, file, jsonOrequest, "reversal 500 Switch malfunction response code not present",
					"96");
		}
		if (tag20_resp.length() < 1) {
			return getMsgError(folder, file, jsonOrequest,
					"reversal 500 Switch malfunction response code length incorrect", "96");
		}

		if (tag20_resp.equalsIgnoreCase("00"))

		{
			Util.writeInFileTransaction(folder, file, "Switch CODE REP : [00]");

			Util.writeInFileTransaction(folder, file, "Transaction reversed.");

			try {
				Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status A ...");

				current_dmd.setEtat_demande("A");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"reversal 500 Error during  demandepaiement update  A for given orderid:[" + orderid + "]" + e);

				return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during  demandepaiement update A",
						tag20_resp);
			}

			Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status OK.");

			Util.writeInFileTransaction(folder, file, "Setting HistoAutoGate status A ...");

			try {
				current_hist.setHatEtat('A');
				histoAutoGateService.save(current_hist);
			} catch (Exception e) {

				e.printStackTrace();
				Util.writeInFileTransaction(folder, file,
						"reversal 500 Error during  HistoAutoGate update  A for given orderid:[" + orderid + "]" + e);

				return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during  HistoAutoGate update A",
						tag20_resp);
			}

			Util.writeInFileTransaction(folder, file, "Setting HistoAutoGate status OK.");

		} else {

			Util.writeInFileTransaction(folder, file, "Transaction reversal declined.");
			Util.writeInFileTransaction(folder, file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		String s_status = "";
		try {
			CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp);
			System.out.println("codeReponseDto : " + codeReponseDto);
			Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
			if (codeReponseDto != null) {
				s_status = codeReponseDto.getRpcLibelle();
			}
		} catch (Exception ee) {
			Util.writeInFileTransaction(folder, file, "authorization 500 Error codeReponseDto null");
			ee.printStackTrace();
		}

		Util.writeInFileTransaction(folder, file, "Switch status : [" + s_status + "]");

		Util.writeInFileTransaction(folder, file, "Generating reversalid");

		String uuid_reversalid, reversalid = "";
		try {

			uuid_reversalid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));

			reversalid = uuid_reversalid.substring(uuid_reversalid.length() - 22);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Error during  reversalid generation for given orderid:[" + orderid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during  reversalid generation",
					tag20_resp);
		}

		try {
			// Transaction info
			jso.put("statuscode", tag20_resp);
			jso.put("status", s_status);
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("reversaldate", date);
			jso.put("reversaltime", heure);
			jso.put("authnumber", authnumber);
			jso.put("reversalid", reversalid);
			jso.put("transactionid", reversalid);

			// Merchant info
			jso.put("merchantid", merchantid);
			jso.put("merchantname", merchantname);
			jso.put("websitename", websiteName);
			jso.put("websiteid", websiteid);
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// Client info
			jso.put("fname", fname);
			jso.put("lname", lname);
			jso.put("email", email);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			Util.writeInFileTransaction(folder, file,
					"reversal 500 Error during jso out processing given authnumber:[" + authnumber + "]" + err8);

			return getMsgError(folder, file, jsonOrequest, "reversal 500 Error during jso out processing", tag20_resp);
		}
		Util.writeInFileTransaction(folder, file, "*********** Fin reversal() ************** ");
		System.out.println("*********** Fin reversal() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/cardtoken", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String savingCardToken(@RequestHeader MultiValueMap<String, String> header,
			@RequestBody String savingcardtoken, HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_SAVINGCARD_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start savingCardToken() ************** ");
		System.out.println("*********** Start savingCardToken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "savingcardtoken api call start ...");
		Util.writeInFileTransaction(folder, file, "savingcardtoken : [" + savingcardtoken + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(savingcardtoken);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file,
					"savingcardtoken 500 malformed json expression " + savingcardtoken + jserr);
			return getMsgErrorV1(folder, file, null, "savingcardtoken 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "savingcardtoken 500 malformed header " + head_err.getMessage(),
						null);
			} else {
				Util.writeInFileTransaction(folder, file, "savingcardtoken 500 malformed header" + head_err);
			}
		}

		String merchantid, merchantname, websiteName, websiteid, cardnumber, expirydate, holdername, fname, lname,
				email = "", securtoken24, mac_value, amount, currency, orderid, promoCode, cvv, phone, address,
				mesg_type, merc_codeactivite, transaction_condition, acqcode, merchant_name, merchant_city, acq_type,
				processing_code, reason_code, transactiondate, transactiontime, date, rrn, heure, montanttrame,
				successURL, failURL, num_trs = "", transactiontype, etataut;

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		try {
			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			successURL = (String) jsonOrequest.get("successURL");
			failURL = (String) jsonOrequest.get("failURL");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");
			expirydate = (String) jsonOrequest.get("expirydate");
			holdername = (String) jsonOrequest.get("holdername");
			cvv = (String) jsonOrequest.get("cvv");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

			// Transaction info
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file,
					"savingcardtoken 500 malformed json expression " + savingcardtoken + jerr);
			Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
			System.out.println("*********** Fin savingCardToken() ************** ");
			return getMsgErrorV1(folder, file, null,
					"savingcardtoken 500 malformed json expression " + jerr.getMessage(), null);
		}

		JSONObject jso = new JSONObject();
		try {
			if (cardnumber.equals("")) {
				Util.writeInFileTransaction(folder, file, "cardnumber is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, cardnumber is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");

				return jso.toString();
			}
			if (mac_value.equals("")) {
				Util.writeInFileTransaction(folder, file, "mac_value is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, mac_value is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");

				return jso.toString();
			}
			if (securtoken24.equals("")) {
				Util.writeInFileTransaction(folder, file, "securtoken24 is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, securtoken24 is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");

				return jso.toString();
			}
			int i_card_valid = Util.isCardValid(cardnumber);

			if (i_card_valid == 1) {
				Util.writeInFileTransaction(folder, file, "Error 500 Card number length is incorrect");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 Card number length is incorrect");

				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
				return jso.toString();
			}

			if (i_card_valid == 2) {
				Util.writeInFileTransaction(folder, file, "Error 500 Card number  is not valid incorrect luhn check");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 Card number  is not valid incorrect luhn check");

				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
				return jso.toString();
			}
			// verification expiration token
			jso = verifieToken(securtoken24, file);
			if (!jso.get("statuscode").equals("00")) {
				// Card info
				jso.put("token", "");
				Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
				System.out.println("jso : " + jso.toString());
				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
				System.out.println("*********** Fin savingCardToken() ************** ");
				return jso.toString();
			}
			int dateint = Integer.valueOf(expirydate);

			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();

			Util.writeInFileTransaction(folder, file, "cardtokenDto expirydate input : " + expirydate);
			String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
			// get year from date
			String xx = anne.substring(0, 2) + expirydate.substring(0, 2);
			String MM = expirydate.substring(2, expirydate.length());
			// format date to "yyyy-MM-dd"
			String expirydateFormated = xx + "-" + MM + "-" + "01";
			System.out.println("cardtokenDto expirydate : " + expirydateFormated);
			Util.writeInFileTransaction(folder, file, "cardtokenDto expirydate formated : " + expirydateFormated);
			Date dateExp = dateFormatSimple.parse(expirydateFormated);

			if (dateExp.before(dateToken)) {
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status",
						"saving token failed, invalid expiry date (expiry date must be greater than system date)");
				Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
				System.out.println("jso : " + jso.toString());
				Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
				System.out.println("*********** Fin savingCardToken() ************** ");
				return jso.toString();
			}

			CommercantDto current_merchant = null;
			try {
				current_merchant = commercantService.findByCmrNumcmr(merchantid);
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + merchantid
								+ "]");

				return getMsgErrorV1(folder, file, jsonOrequest,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "15");
			}

			if (current_merchant == null) {
				Util.writeInFileTransaction(folder, file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + merchantid
								+ "]");

				return getMsgErrorV1(folder, file, jsonOrequest,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "15");
			}

			if (current_merchant.getCmrCodactivite() == null) {
				Util.writeInFileTransaction(folder, file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + merchantid
								+ "]");

				return getMsgErrorV1(folder, file, jsonOrequest,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "15");
			}

			if (current_merchant.getCmrCodbqe() == null) {
				Util.writeInFileTransaction(folder, file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + merchantid
								+ "]");

				return getMsgErrorV1(folder, file, jsonOrequest,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "");
			}
			int i_card_type = Util.getCardIss(cardnumber);

			DemandePaiementDto dmd = null;
			DemandePaiementDto dmdSaved = null;
			amount = "0";
			currency = "504";
			try {

				dmd = new DemandePaiementDto();

				dmd.setComid(merchantid);
				// generer commande
				orderid = Util.genCommande(merchantid);
				dmd.setCommande(orderid);
				dmd.setDem_pan(cardnumber);
				dmd.setDem_cvv(cvv);
				dmd.setGalid(websiteid);
				dmd.setSuccessURL(successURL);
				dmd.setFailURL(failURL);
				dmd.setType_carte(i_card_type + "");
				if (amount.equals("") || amount == null) {
					amount = "0";
				}
				if (amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				dmd.setMontant(Double.parseDouble(amount));
				dmd.setNom(lname);
				dmd.setPrenom(fname);
				dmd.setEmail(email);
				dmd.setTel("");
				dmd.setAddress("");
				dmd.setCity("");
				dmd.setCountry("");
				dmd.setState("");
				dmd.setPostcode("");
				dmd.setLangue("E");
				dmd.setEtat_demande("INIT");

				formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
				formatter_2 = new SimpleDateFormat("HH:mm:ss");
				trsdate = new Date();
				transactiondate = formatter_1.format(trsdate);
				transactiontime = formatter_2.format(trsdate);
				// dmd.setDem_date_time(transactiondate + transactiontime);
				dmd.setDem_date_time(dateFormat.format(new Date()));
				dmd.setIs_cof("Y");
				dmd.setIs_addcard("Y");
				dmd.setIs_tokenized("Y");
				dmd.setIs_whitelist("Y");
				dmd.setIs_withsave("Y");

				// generer token
				String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
				dmd.setTokencommande(tokencommande);
				// set transctiontype
				dmd.setTransactiontype("0");

				dmdSaved = demandePaiementService.save(dmd);
				dmdSaved.setExpery(expirydate);

			} catch (Exception err1) {
				Util.writeInFileTransaction(folder, file,
						"savingCardToken 500 Error during DEMANDE_PAIEMENT insertion for given merchantid:["
								+ merchantid + "]" + err1);

				return getMsgErrorV1(folder, file, jsonOrequest,
						"savingCardToken 500 Error during DEMANDE_PAIEMENT insertion", null);
			}

			try {
				formatheure = new SimpleDateFormat("HHmmss");
				formatdate = new SimpleDateFormat("ddMMyy");
				date = formatdate.format(new Date());
				heure = formatheure.format(new Date());
				rrn = Util.getGeneratedRRN();

			} catch (Exception err2) {
				Util.writeInFileTransaction(folder, file,
						"savingCardToken 500 Error during  date formatting for given orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err2);

				return getMsgErrorV1(folder, file, jsonOrequest, "savingCardToken 500 Error during  date formatting",
						null);
			}

			ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();
			// appel 3DSSecure ***********************************************************

			/**
			 * dans la preprod les tests Coca sans 3DSS on commente l'appel 3DSS et on mj
			 * reponseMPI="Y"
			 */
			Util.writeInFileTransaction(folder, file, "environement : " + environement);
			if (environement.equals("PREPROD")) {
				// threeDsecureResponse = autorisationService.preparerReqThree3DSS(dmdSaved,
				// folder, file);

				threeDsecureResponse.setReponseMPI("Y");
			} else {
				threeDsecureResponse = autorisationService.preparerReqThree3DSS(dmdSaved, folder, file);
			}

			// fin 3DSSecure ***********************************************************

			/*
			 * ------------ DEBUT MPI RESPONSE PARAMS ------------
			 */
			String reponseMPI = "";
			String eci = "";
			String cavv = "";
			String threeDSServerTransID = "";
			String xid = "";
			String errmpi = "";
			// String idDemande = "";
			String idDemande = String.valueOf(dmdSaved.getIddemande() == null ? "" : dmdSaved.getIddemande());
			String expiry = ""; // YYMM

			if (threeDsecureResponse.getReponseMPI() != null) {
				reponseMPI = threeDsecureResponse.getReponseMPI();
			}
			/*
			 * if (threeDsecureResponse.getIdDemande() != null) { idDemande =
			 * threeDsecureResponse.getIdDemande(); }
			 */
			if (threeDsecureResponse.getThreeDSServerTransID() != null) {
				threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
			}
			if (threeDsecureResponse.getEci() != null) {
				eci = threeDsecureResponse.getEci();
			} else {
				eci = "";
			}
			if (threeDsecureResponse.getCavv() != null) {
				cavv = threeDsecureResponse.getCavv();
			} else {
				cavv = "";
			}
			if (threeDsecureResponse.getErrmpi() != null) {
				errmpi = threeDsecureResponse.getErrmpi();
			} else {
				errmpi = "";
			}
			if (threeDsecureResponse.getExpiry() != null) {
				expiry = threeDsecureResponse.getExpiry();
			} else {
				expiry = "";
			}

			if (idDemande == null || idDemande.equals("")) {
				Util.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
				dmdSaved.setEtat_demande("MPI_KO");
				demandePaiementService.save(dmdSaved);
				Util.writeInFileTransaction(folder, file,
						"demandePaiement after update MPI_KO idDemande null : " + dmdSaved.toString());
				return getMsgErrorV1(folder, file, jsonOrequest, "AUTO INVALIDE DEMANDE MPI_KO", "96");
			}

			dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

			if (dmd == null) {
				Util.writeInFileTransaction(folder, file,
						"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
								+ idDemande);
				return getMsgErrorV1(folder, file, jsonOrequest, "AUTO INVALIDE DEMANDE NOT FOUND", "96");
			}

			if (reponseMPI.equals("") || reponseMPI == null) {
				dmd.setEtat_demande("MPI_KO");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file,
						"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
				Util.writeInFileTransaction(folder, file, "Response 3DS is null");
				return getMsgErrorV1(folder, file, jsonOrequest, "Response 3DS is null", "96");
			}

			if (reponseMPI.equals("Y")) {
				Util.writeInFileTransaction(folder, file,
						"********************* Cas frictionless responseMPI equal Y *********************");
				if (!threeDSServerTransID.equals("")) {
					dmd.setDem_xid(threeDSServerTransID);
					demandePaiementService.save(dmd);
				}
				// add payment 0 dh test
				
				// 2024-03-05
				montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, jsonOrequest);

				merc_codeactivite = current_merchant.getCmrCodactivite();
				acqcode = current_merchant.getCmrCodbqe();
				merchant_name = Util.pad_merchant(merchantname, 19, ' ');
				Util.writeInFileTransaction(folder, file, "merchant_name : [" + merchant_name + "]");

				merchant_city = "MOROCCO        ";
				Util.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

				acq_type = "0000";
				reason_code = "H";
				transaction_condition = "6";
				mesg_type = "0";
				processing_code = "0";
				transactiontype = "0";

				try {
					formatheure = new SimpleDateFormat("HHmmss");
					formatdate = new SimpleDateFormat("ddMMyy");
					date = formatdate.format(new Date());
					heure = formatheure.format(new Date());
					rrn = Util.getGeneratedRRN();

				} catch (Exception err2) {
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error during  date formatting for given merchantid:[" + merchantid
									+ "]" + err2);

					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error during  date formatting", null);
				}

				boolean cvv_present = check_cvv_presence(cvv);
				boolean is_first_trs = true;

				String first_auth = "";
				long lrec_serie = 0;

				// controls
				Util.writeInFileTransaction(folder, file, "Switch processing start ...");

				String tlv = "";
				Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

				if (!cvv_present) {
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
							"82");
				}

				// not reccuring , normal
				if (cvv_present) {
					Util.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
					try {

						tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
								.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
								.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
								.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
								.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
								.withField(Tags.tag16, date).withField(Tags.tag17, heure)
								.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
								.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
								.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
								.withField(Tags.tag90, acqcode).encode();

						Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
						Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
						Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
						Util.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
						Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
						Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
						Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
						Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
						Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
						Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
						Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
						Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
						Util.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
						Util.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
						Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
						Util.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
						Util.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
						Util.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
						Util.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
						Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");

					} catch (Exception err4) {
						Util.writeInFileTransaction(folder, file,
								"savingcardtoken 500 Error during switch tlv buildup for given merchantid:["
										+ merchantid + "]" + err4);

						return getMsgErrorV1(folder, file, jsonOrequest,
								"savingcardtoken 500 Error during switch tlv buildup", "96");
					}

					Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

				}

				Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

				String resp_tlv = "";
				int port = 0;
				String sw_s = "", s_port = "";
				int switch_ko = 0;
				try {

					s_port = portSwitch;
					sw_s = ipSwitch;

					port = Integer.parseInt(s_port);

					Util.writeInFileTransaction(folder, file, "Switch TCP client V2 Connecting ...");

					SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

					boolean s_conn = switchTCPClient.isConnected();

					if (!s_conn) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

						return getMsgErrorV1(folder, file, jsonOrequest,
								"savingcardtoken 500 Error Switch communication s_conn false", "96");
					}

					if (s_conn) {
						Util.writeInFileTransaction(folder, file, "Switch Connected.");
						Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

						resp_tlv = switchTCPClient.sendMessage(tlv);

						Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
						switchTCPClient.shutdown();
					}

				} catch (UnknownHostException e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error Switch communication UnknownHostException", "96");

				} catch (java.net.ConnectException e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
					switch_ko = 1;
					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error Switch communication ConnectException", "96");
				}

				catch (SocketTimeoutException e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
					switch_ko = 1;
					e.printStackTrace();
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error Switch communication SocketTimeoutException" + "switch ip:["
									+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					return getMsgErrorV1(folder, file, jsonOrequest, "Switch  malfunction  SocketTimeoutException !!!",
							"96");
				}

				catch (IOException e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
					switch_ko = 1;
					e.printStackTrace();
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error Switch communication IOException" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					return getMsgErrorV1(folder, file, jsonOrequest, "Switch  malfunction  IOException !!!", "96");
				}

				catch (Exception e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
					switch_ko = 1;
					e.printStackTrace();
					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error Switch communication General Exception", "96");
				}

				// resp debug =
				// resp_tlv =
				// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

				String resp = resp_tlv;

				if (switch_ko == 0 && resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file, "savingcardtoken 500 Error Switch null response"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					return getMsgErrorV1(folder, file, jsonOrequest, "Switch  malfunction resp null!!!", "96");
				}

				if (switch_ko == 0 && resp.length() < 3) {
					switch_ko = 1;

					Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error Switch short response length() < 3 " + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

				Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

				TLVParser tlvp = null;

				String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null, tag9_resp = null,
						tag14_resp = null, tag15_resp = null, tag16_resp = null, tag17_resp = null, tag66_resp = null,
						tag18_resp = null, tag19_resp = null, tag23_resp = null, tag20_resp = null, tag21_resp = null,
						tag22_resp = null, tag80_resp = null, tag98_resp = null;

				if (switch_ko == 0) {
					try {
						tlvp = new TLVParser(resp);

						tag0_resp = tlvp.getTag(Tags.tag0);
						tag1_resp = tlvp.getTag(Tags.tag1);
						tag3_resp = tlvp.getTag(Tags.tag3);
						tag8_resp = tlvp.getTag(Tags.tag8);
						tag9_resp = tlvp.getTag(Tags.tag9);
						tag14_resp = tlvp.getTag(Tags.tag14);
						tag15_resp = tlvp.getTag(Tags.tag15);
						tag16_resp = tlvp.getTag(Tags.tag16);
						tag17_resp = tlvp.getTag(Tags.tag17);
						tag66_resp = tlvp.getTag(Tags.tag66); // f1
						tag18_resp = tlvp.getTag(Tags.tag18);
						tag19_resp = tlvp.getTag(Tags.tag19); // f2
						tag23_resp = tlvp.getTag(Tags.tag23);
						tag20_resp = tlvp.getTag(Tags.tag20);
						tag21_resp = tlvp.getTag(Tags.tag21);
						tag22_resp = tlvp.getTag(Tags.tag22);
						tag80_resp = tlvp.getTag(Tags.tag80);
						tag98_resp = tlvp.getTag(Tags.tag98);

					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
						switch_ko = 1;
						Util.writeInFileTransaction(folder, file,
								"savingcardtoken 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					}

					// controle switch
					if (tag1_resp == null) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
						switch_ko = 1;
						Util.writeInFileTransaction(folder, file,
								"savingcardtoken 500 Error during tlv Switch response parse tag1_resp tag null"
										+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
										+ resp_tlv + "]");
					}

					if (tag1_resp != null && tag1_resp.length() < 3) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
						switch_ko = 1;
						Util.writeInFileTransaction(folder, file,
								"savingcardtoken 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
										+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
										+ resp_tlv + "]");
					}

					if (tag20_resp == null) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
						switch_ko = 1;
						Util.writeInFileTransaction(folder, file,
								"savingcardtoken 500 Error during tlv Switch response parse tag1_resp tag null"
										+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
										+ resp_tlv + "]");
					}
				}
				Util.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
				Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

				Util.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
				Util.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

				String tag20_resp_verified = "";
				String tag19_res_verified = "";
				String tag66_resp_verified = "";
				tag20_resp_verified = tag20_resp;
				tag19_res_verified = tag19_resp;
				tag66_resp_verified = tag66_resp;
				String s_status, pan_auto = "";

				if (switch_ko == 1) {
					pan_auto = Util.formatagePan(cardnumber);
					Util.writeInFileTransaction(folder, file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
							+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + merchantid);
				}

				Util.writeInFileTransaction(folder, file, "get status ...");

				s_status = "";
				try {
					CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
					System.out.println("codeReponseDto : " + codeReponseDto);
					Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
					if (codeReponseDto != null) {
						s_status = codeReponseDto.getRpcLibelle();
					}
				} catch (Exception ee) {
					Util.writeInFileTransaction(folder, file, "savingcardtoken 500 Error codeReponseDto null");
					ee.printStackTrace();
				}
				
				websiteid = dmd.getGalid();

				Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

				HistoAutoGateDto hist = null;
				Integer Ihist_id = null;

				Util.writeInFileTransaction(folder, file, "Insert into Histogate...");

				try {

					hist = new HistoAutoGateDto();
					Date curren_date_hist = new Date();
					int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

					Util.writeInFileTransaction(folder, file, "formatting pan...");

					pan_auto = Util.formatagePan(cardnumber);
					Util.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

					Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");
					
					Util.writeInFileTransaction(folder, file, "websiteid : " + websiteid);

					Date current_date_1 = getDateWithoutTime(curren_date_hist);
					hist.setHatDatdem(current_date_1);

					hist.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
					hist.setHatMontant(Double.parseDouble(amount));
					hist.setHatNumcmr(merchantid);
					hist.setHatCoderep(tag20_resp_verified);
					tag20_resp = tag20_resp_verified;
					hist.setHatDevise(currency);
					hist.setHatBqcmr(acqcode);
					hist.setHatPorteur(pan_auto);
					hist.setHatMtfref1(s_status);
					hist.setHatNomdeandeur(websiteid);
					hist.setHatNautemt(tag19_res_verified); // f2
					tag19_resp = tag19_res_verified;
					if (tag22_resp != null)
						hist.setHatProcode(tag22_resp.charAt(0));
					else
						hist.setHatProcode('6');
					hist.setHatExpdate(expirydate);
					hist.setHatRepondeur(tag21_resp);
					hist.setHatTypmsg("3");
					hist.setHatRrn(tag66_resp_verified); // f1
					tag66_resp_verified = tag66_resp;
					hist.setHatEtat('E');
					if (websiteid.equals("")) {
						hist.setHatCodtpe("1");
					} else {
						hist.setHatCodtpe(websiteid);
					}
					hist.setHatMcc(merc_codeactivite);
					hist.setHatNumCommande(orderid);
					hist.setHatNumdem(new Long(numTransaction));

					if (check_cvv_presence(cvv)) {

						hist.setIs_cvv_verified("Y");
					} else {

						hist.setIs_cvv_verified("N");
					}

					hist.setIs_3ds("N");
					hist.setIs_addcard("N");
					hist.setIs_whitelist("N");
					hist.setIs_withsave("N");
					hist.setIs_tokenized("N");

					hist.setIs_cof("N");

					Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

					Util.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

					histoAutoGateService.save(hist);
					
					Util.writeInFileTransaction(folder, file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error during  insert in histoautogate for given orderid:[" + orderid
									+ "]" + e);
					try {
						Util.writeInFileTransaction(folder, file, "2eme tentative : HistoAutoGate Saving ... ");
						histoAutoGateService.save(hist);
					} catch (Exception ex) {
						Util.writeInFileTransaction(folder, file,
								"2eme tentative : savingcardtoken 500 Error during  insert in histoautogate for given orderid:["
										+ orderid + "]" + ex);
					}
				}

				Util.writeInFileTransaction(folder, file, "HistoAutoGate OK.");

				if (tag20_resp == null) {
					tag20_resp = "";
				}

				Util.writeInFileTransaction(folder, file, "Generating paymentid...");

				String uuid_paymentid, paymentid = "";
				try {
					uuid_paymentid = String.format("%040d",
							new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
					paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error during  paymentid generation for given orderid:[" + orderid + "]"
									+ e);
					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error during  paymentid generation", tag20_resp);
				}

				Util.writeInFileTransaction(folder, file, "Generating paymentid OK");
				Util.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

				// JSONObject jso = new JSONObject();

				Util.writeInFileTransaction(folder, file, "Preparing autorization api response");

				String authnumber, coderep, motif, merchnatidauth, dtdem = "";

				try {
					authnumber = hist.getHatNautemt();
					coderep = hist.getHatCoderep();
					motif = hist.getHatMtfref1();
					merchnatidauth = hist.getHatNumcmr();
					dtdem = dmd.getDem_pan();
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error during authdata preparation orderid:[" + orderid + "]" + e);

					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error during authdata preparation", tag20_resp);
				}

				if (tag20_resp.equalsIgnoreCase("00")) {
					// insert new cardToken
					CardtokenDto cardtokenDto = new CardtokenDto();
					String tokencard = Util.generateCardToken(merchantid);

					// test if token not exist in DB
					// CardtokenDto checkCardToken =
					// cardtokenService.findByIdMerchantAndTokenAndExprDate(merchantid, tokencard,
					// dateExp);
					CardtokenDto checkCardToken = cardtokenService.findByIdMerchantAndToken(merchantid, tokencard);

					while (checkCardToken != null) {
						tokencard = Util.generateCardToken(merchantid);
						System.out.println("checkCardToken exist => generate new tokencard : " + tokencard);
						Util.writeInFileTransaction(folder, file,
								"checkCardToken exist => generate new tokencard : " + tokencard);
						checkCardToken = cardtokenService.findByIdMerchantAndToken(merchantid, tokencard);
					}
					System.out.println("tokencard : " + tokencard);
					Util.writeInFileTransaction(folder, file, "tokencard : " + tokencard);

					cardtokenDto.setToken(tokencard);
					String tokenid = UUID.randomUUID().toString();
					cardtokenDto.setIdToken(tokenid);
					cardtokenDto.setExprDate(dateExp);
					String dateTokenStr = dateFormat.format(dateToken);
					Date dateTokenFormated = dateFormat.parse(dateTokenStr);
					cardtokenDto.setTokenDate(dateTokenFormated);
					cardtokenDto.setCardNumber(cardnumber);
					cardtokenDto.setIdMerchant(merchantid);
					cardtokenDto.setIdMerchantClient(merchantid);
					cardtokenDto.setFirst_name(fname);
					cardtokenDto.setLast_name(lname);
					cardtokenDto.setHolderName(holdername);
					cardtokenDto.setMcc(merchantid);

					CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

					Util.writeInFileTransaction(folder, file, "Saving CARDTOKEN OK");

					// Card info
					jso.put("token", cardtokenSaved.getToken());
					jso.put("cardnumber", Util.formatCard(cardnumber));

					// Transaction info
					jso.put("statuscode", "00");
					jso.put("status", "saving token successfully");
					jso.put("etataut", "Y");
					jso.put("orderid", orderid);
					jso.put("amount", amount);
					jso.put("transactiondate", date);
					jso.put("transactiontime", heure);
					jso.put("authnumber", authnumber);
					jso.put("paymentid", paymentid);
					jso.put("linkacs", "");

					// Merchant info
					jso.put("merchantid", merchantid);
					jso.put("merchantname", merchantname);
					jso.put("websitename", websiteName);
					jso.put("websiteid", websiteid);

					// Client info
					jso.put("fname", fname);
					jso.put("lname", lname);
					jso.put("email", email);
				} else {
					Util.writeInFileTransaction(folder, file,
							"saving token failed, coderep : [" + tag20_resp + "]" + "motif : [" + s_status + "]");
					// Card info
					jso.put("token", "");
					jso.put("cardnumber", Util.formatCard(cardnumber));

					// Transaction info
					if (tag20_resp.equals("")) {
						jso.put("statuscode", "17");
					} else {
						jso.put("statuscode", tag20_resp);
					}
					jso.put("status",
							"saving token failed, coderep : [" + tag20_resp + "]" + "motif : [" + s_status + "]");
					jso.put("etataut", "Y");
					jso.put("orderid", orderid);
					jso.put("amount", amount);
					jso.put("transactiondate", date);
					jso.put("transactiontime", heure);
					jso.put("authnumber", authnumber);
					jso.put("paymentid", paymentid);
					jso.put("linkacs", "");

					// Merchant info
					jso.put("merchantid", merchantid);
					jso.put("merchantname", merchantname);
					jso.put("websitename", websiteName);
					jso.put("websiteid", websiteid);

					// Client info
					jso.put("fname", fname);
					jso.put("lname", lname);
					jso.put("email", email);
				}
			} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
				// ********************* Cas chalenge responseMPI equal C ou D
				// *********************
				Util.writeInFileTransaction(folder, file, "****** Cas chalenge responseMPI equal C ou D ******");
				try {

					// Transaction info
					jso.put("etataut", "C");
					jso.put("orderid", orderid);
					jso.put("amount", amount);
					jso.put("transactiondate", date);
					jso.put("transactiontime", heure);

					// Merchant info
					jso.put("merchantid", merchantid);
					jso.put("merchantname", merchantname);
					jso.put("websitename", websiteName);
					jso.put("websiteid", websiteid);

					// Card info
					jso.put("cardnumber", Util.formatCard(cardnumber));
					jso.put("token", "");
					jso.put("statuscode", "");
					jso.put("status", "");

					// Client info
					jso.put("fname", fname);
					jso.put("lname", lname);
					jso.put("email", email);

					// Link ACS chalenge info
					jso.put("linkacs", link_chalenge + dmd.getTokencommande());

					// insertion htmlCreq dans la demandePaiement
					dmd.setCreq(threeDsecureResponse.getHtmlCreq());
					dmd.setDem_xid(threeDSServerTransID);
					dmd.setEtat_demande("SND_TO_ACS");
					demandePaiementService.save(dmd);

					System.out.println("link_chalenge " + link_chalenge + dmd.getTokencommande());
					Util.writeInFileTransaction(folder, file,
							"link_chalenge " + link_chalenge + dmd.getTokencommande());

					System.out.println("savingcardtoken api response chalenge :  [" + jso.toString() + "]");
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken api response chalenge :  [" + jso.toString() + "]");
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							"savingcardtoken 500 Error during jso out processing " + ex);

					return getMsgErrorV1(folder, file, jsonOrequest,
							"savingcardtoken 500 Error during jso out processing ", null);
				}
			} else if (reponseMPI.equals("E")) {
				// ********************* Cas responseMPI equal E
				// *********************
				Util.writeInFileTransaction(folder, file, "****** Cas responseMPI equal E ******");
				Util.writeInFileTransaction(folder, file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);

				// Transaction info
				jso.put("statuscode", "96");
				jso.put("status",
						"La transaction en cours n’a pas abouti (Problème authentification 3DSecure), votre compte ne sera pas débité, merci de contacter votre banque .");
				jso.put("etataut", "N");
				jso.put("orderid", orderid);
				jso.put("amount", amount);
				jso.put("transactiondate", date);
				jso.put("transactiontime", heure);
				jso.put("transactionid", "123456");

				// Merchant info
				jso.put("merchantid", merchantid);
				jso.put("merchantname", merchantname);
				jso.put("websitename", websiteName);
				jso.put("websiteid", websiteid);

				// Card info
				jso.put("cardnumber", Util.formatCard(cardnumber));
				jso.put("token", "");
				jso.put("statuscode", "");
				jso.put("status", "");

				// Client info
				jso.put("fname", fname);
				jso.put("lname", lname);
				jso.put("email", email);

				// Link ACS chalenge info :
				jso.put("linkacs", "");

				System.out.println("savingcardtoken api response fail :  [" + jso.toString() + "]");
				Util.writeInFileTransaction(folder, file,
						"savingcardtoken api response fail :  [" + jso.toString() + "]");
			} else {
				switch (errmpi) {
				case "COMMERCANT NON PARAMETRE":
					Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
					dmd.setDem_xid(threeDSServerTransID);
					dmd.setEtat_demande("MPI_CMR_INEX");
					demandePaiementService.save(dmd);
					// externalContext.redirect("operationErreur.xhtml?Error=".concat("COMMERCANT
					// NON PARAMETRE"));
					return getMsgErrorV1(folder, file, jsonOrequest, "COMMERCANT NON PARAMETRE", "15");
				case "BIN NON PARAMETRE":
					Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
					dmd.setEtat_demande("MPI_BIN_NON_PAR");
					dmd.setDem_xid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return getMsgErrorV1(folder, file, jsonOrequest, "BIN NON PARAMETREE", "96");
				case "DIRECTORY SERVER":
					Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
					dmd.setEtat_demande("MPI_DS_ERR");
					dmd.setDem_xid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return getMsgErrorV1(folder, file, jsonOrequest, "MPI_DS_ERR", "96");
				case "CARTE ERRONEE":
					Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
					dmd.setEtat_demande("MPI_CART_ERROR");
					dmd.setDem_xid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return getMsgErrorV1(folder, file, jsonOrequest, "CARTE ERRONEE", "96");
				case "CARTE NON ENROLEE":
					Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
					dmd.setEtat_demande("MPI_CART_NON_ENR");
					dmd.setDem_xid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return getMsgErrorV1(folder, file, jsonOrequest, "CARTE NON ENROLLE", "96");
				}
			}
		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file,
					"cardtoken 500 Error during CARDTOKEN Saving : " + ex.getMessage());
			// Card info
			jso.put("token", "");

			// Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "saving token failed ");

			Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
			System.out.println("*********** Fin savingCardToken() ************** ");

			return jso.toString();
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin savingCardToken() ************** ");
		System.out.println("*********** Fin savingCardToken() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/cardtokenOld", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String getCardTkenOld(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cardtoken,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_SAVINGCARD_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start getCardTken() ************** ");
		System.out.println("*********** Start getCardTken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "cardtoken api call start ...");
		Util.writeInFileTransaction(folder, file, "cardtoken : [" + cardtoken + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(cardtoken);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "cardtoken 500 malformed json expression " + cardtoken + jserr);
			return getMsgError(folder, file, null, "cardtoken 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "cardtoken 500 malformed header " + head_err.getMessage(), null);
			} else {
				Util.writeInFileTransaction(folder, file, "cardtoken 500 malformed header" + head_err);
			}
		}

		String merchantid, merchantname, websiteName, websiteid, cardnumber, expirydate, holdername, fname, lname,
				email = "", securtoken24, mac_value;

		try {
			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");
			expirydate = (String) jsonOrequest.get("expirydate");
			holdername = (String) jsonOrequest.get("holdername");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

			// Transaction info
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "cardtoken 500 malformed json expression " + cardtoken + jerr);
			Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
			System.out.println("*********** Fin getCardTken() ************** ");
			return getMsgError(folder, file, null, "cardtoken 500 malformed json expression " + jerr.getMessage(),
					null);
		}

		JSONObject jso = new JSONObject();
		try {
			if (cardnumber.equals("")) {
				Util.writeInFileTransaction(folder, file, "cardnumber is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, cardnumber is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");

				return jso.toString();
			}
			if (mac_value.equals("")) {
				Util.writeInFileTransaction(folder, file, "mac_value is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, mac_value is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");

				return jso.toString();
			}
			if (securtoken24.equals("")) {
				Util.writeInFileTransaction(folder, file, "securtoken24 is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, securtoken24 is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");

				return jso.toString();
			}
			int i_card_valid = Util.isCardValid(cardnumber);

			if (i_card_valid == 1) {
				Util.writeInFileTransaction(folder, file, "Error 500 Card number length is incorrect");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 Card number length is incorrect");

				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
				return jso.toString();
			}

			if (i_card_valid == 2) {
				Util.writeInFileTransaction(folder, file, "Error 500 Card number  is not valid incorrect luhn check");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 Card number  is not valid incorrect luhn check");

				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
				return jso.toString();
			}
			// verification expiration token
			jso = verifieToken(securtoken24, file);
			if (!jso.get("statuscode").equals("00")) {
				// Card info
				jso.put("token", "");
				Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
				System.out.println("jso : " + jso.toString());
				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
				System.out.println("*********** Fin getCardTken() ************** ");
				return jso.toString();
			}
			int dateint = Integer.valueOf(expirydate);

			// insert new cardToken
			CardtokenDto cardtokenDto = new CardtokenDto();

			String tokencard = Util.generateCardToken(merchantid);

			cardtokenDto.setToken(tokencard);
			Util.writeInFileTransaction(folder, file, "cardtokenDto token : " + cardtokenDto.getToken());

			String tokenid = UUID.randomUUID().toString();
			cardtokenDto.setIdToken(tokenid);

			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();

			Util.writeInFileTransaction(folder, file, "cardtokenDto expirydate input : " + expirydate);
			String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
			// get year from date
			String xx = anne.substring(0, 2) + expirydate.substring(0, 2);
			String mm = expirydate.substring(2, expirydate.length());
			// format date to "yyyy-MM-dd"
			expirydate = xx + "-" + mm + "-" + "01";
			System.out.println("cardtokenDto expirydate : " + expirydate);
			Util.writeInFileTransaction(folder, file, "cardtokenDto expirydate formated : " + expirydate);
			Date dateExp = dateFormatSimple.parse(expirydate);

			if (dateExp.before(dateToken)) {
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status",
						"saving token failed, invalid expiry date (expiry date must be greater than system date)");
				Util.writeInFileTransaction(folder, file, "jso : " + jso.toString());
				System.out.println("jso : " + jso.toString());
				Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
				System.out.println("*********** Fin getCardTken() ************** ");
				return jso.toString();
			}

			cardtokenDto.setExprDate(dateExp);
			String dateTokenStr = dateFormat.format(dateToken);
			Date dateTokenFormated = dateFormat.parse(dateTokenStr);
			cardtokenDto.setTokenDate(dateTokenFormated);
			cardtokenDto.setCardNumber(cardnumber);
			cardtokenDto.setIdMerchant(merchantid);
			cardtokenDto.setIdMerchantClient(merchantid);
			cardtokenDto.setFirst_name(fname);
			cardtokenDto.setLast_name(lname);
			cardtokenDto.setHolderName(holdername);
			cardtokenDto.setMcc(merchantid);

			CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

			// Card info
			jso.put("token", cardtokenSaved.getToken());

			// Transaction info
			jso.put("statuscode", "00");
			jso.put("status", "saving token successfully");

			Util.writeInFileTransaction(folder, file, "Saving CARDTOKEN OK");

		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file,
					"cardtoken 500 Error during CARDTOKEN Saving : " + ex.getMessage());
			// Card info
			jso.put("token", "");

			// Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "saving token failed ");

			Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
			System.out.println("*********** Fin getCardTken() ************** ");

			return jso.toString();
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
		System.out.println("*********** Fin getCardTken() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/deleteCardtoken", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String deleteCardTken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cardtoken,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_DELETECRDTKN_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start deleteCardTken() ************** ");
		System.out.println("*********** Start deleteCardTken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "deleteCardTken api call start ...");
		Util.writeInFileTransaction(folder, file, "deleteCardTken : [" + cardtoken + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(cardtoken);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file,
					"deleteCardTken 500 malformed json expression " + cardtoken + jserr);
			return getMsgError(folder, file, null, "deleteCardTken 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {

			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "deleteCardTken 500 malformed header " + head_err.getMessage(),
						null);
			} else {
				Util.writeInFileTransaction(folder, file, "deleteCardTken 500 malformed header" + head_err);
			}
		}

		String merchantid, cardnumber, token;
		try {
			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");

			// Card info
			// cardnumber = (String) jsonOrequest.get("cardnumber");
			token = (String) jsonOrequest.get("token");

			// Client info
		} catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file,
					"deleteCardTken 500 malformed json expression " + cardtoken + jserr);
			return getMsgError(folder, file, null, "deleteCardTken 500 malformed json expression " + jserr.getMessage(),
					null);
		}

		JSONObject jso = new JSONObject();
		try {
			if (merchantid.equals("")) {
				Util.writeInFileTransaction(folder, file, "merchantid is empty");
				// Card info
				jso.put("token", token);

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "delete token failed, merchantid is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin deleteCardTken() ************** ");

				return jso.toString();
			}
			if (token.equals("")) {
				Util.writeInFileTransaction(folder, file, "token is empty");
				// Card info
				jso.put("token", "");

				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "delete token failed, token is empty");

				Util.writeInFileTransaction(folder, file, "*********** Fin deleteCardTken() ************** ");

				return jso.toString();
			}
			/*
			 * if(cardnumber.equals("")) { Util.writeInFileTransaction(folder, file,
			 * "cardnumber is empty"); // Card info jso.put("token", token);
			 * 
			 * // Transaction info jso.put("statuscode", "17"); jso.put("status",
			 * "delete token failed, cardnumber is empty");
			 * 
			 * Util.writeInFileTransaction(folder, file,
			 * "*********** Fin deleteCardTken() ************** ");
			 * 
			 * return jso.toString(); } int i_card_valid = Util.isCardValid(cardnumber);
			 * 
			 * if (i_card_valid == 1) { Util.writeInFileTransaction(folder, file,
			 * "Error 500 Card number length is incorrect"); // Card info jso.put("token",
			 * token);
			 * 
			 * // Transaction info jso.put("statuscode", "17"); jso.put("status",
			 * "Error 500 Card number length is incorrect");
			 * 
			 * Util.writeInFileTransaction(folder, file,
			 * "*********** Fin deleteCardTken() ************** "); return jso.toString(); }
			 * 
			 * if (i_card_valid == 2) { Util.writeInFileTransaction(folder,
			 * file,"Error 500 Card number is not valid incorrect luhn check"); // Card info
			 * jso.put("token", token);
			 * 
			 * // Transaction info jso.put("statuscode", "17"); jso.put("status",
			 * "Error 500 Card number is not valid incorrect luhn check");
			 * 
			 * Util.writeInFileTransaction(folder, file,
			 * "*********** Fin deleteCardTken() ************** "); return jso.toString(); }
			 */
			// delete cardToken

			CardtokenDto cardTokenTodelete = cardtokenService.findByIdMerchantAndToken(merchantid, token);

			cardtokenService.delete(cardTokenTodelete);

			// Card info
			jso.put("token", token);

			// Transaction info
			jso.put("statuscode", "00");
			jso.put("status", "delete token successfully ");

			Util.writeInFileTransaction(folder, file, "Delete CARDTOKEN OK");

		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file, "Error during delete token : " + ex);
			// Card info
			jso.put("token", token);

			// Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "delete token failed");
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin deleteCardTken() ************** ");
		System.out.println("*********** Fin deleteCardTken() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/histo/exportexcel", consumes = "application/json", produces = "application/json")
	public void exportToExcel(@RequestHeader MultiValueMap<String, String> header, @RequestBody String req,
			HttpServletResponse response) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_EXPORTE_EXCEL_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start exportToExcel() ************** ");
		System.out.println("*********** Start exportToExcel() ************** ");

		Util.writeInFileTransaction(folder, file, "exportToExcel api call start ...");
		Util.writeInFileTransaction(folder, file, "exportToExcel : [" + req + "]");

		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=HistoriqueTrs_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(req);

		} catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "exportToExcel 500 malformed json expression " + req + jserr);
		}

		String merchantid = "", orderid = "", dateDem = "";
		try {
			// Transaction info
			merchantid = (String) jsonOrequest.get("merchantid");

			orderid = (String) jsonOrequest.get("orderid");
			dateDem = (String) jsonOrequest.get("dateDem");

		} catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "exportToExcel 500 malformed json expression " + req + jserr);
		}

		try {

			// List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findAll();
			List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findByHatNumcmr(merchantid);

			GenerateExcel excelExporter = new GenerateExcel(listHistoGate);

			excelExporter.export(response);

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "exportToExcel 500 merchantid:[" + merchantid + "]");
			Util.writeInFileTransaction(folder, file, "exportToExcel 500 exception" + e);
			e.printStackTrace();
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin exportToExcel ***********");
		System.out.println("*********** Fin exportToExcel ***********");
	}

	@PostMapping(value = "/napspayment/cpautorisation", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String cpautorisation(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cpauths,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CPAUTORISATION_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start cpautorisation() ************** ");
		System.out.println("*********** Start cpautorisation() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		logger.info("cpautorisation api call start ...");

		Util.writeInFileTransaction(folder, file, "cpautorisation api call start ...");

		Util.writeInFileTransaction(folder, file, "cpautorisation : [" + cpauths + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(cpauths);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 malformed json expression " + cpauths + jserr);
			return getMsgError(folder, file, null, "cpautorisation 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {
			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file,
						"cpautorisation 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "cpautorisation 500 malformed header", null);
			}

			else {
				Util.writeInFileTransaction(folder, file, "cpautorisation 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "cpautorisation 500 malformed header " + head_err.getMessage(),
						null);
			}

		}

		String capture, currency, orderid, recurring, amount, transactionid, merchantid, capture_id, merchantname,
				websiteName, websiteid, callbackurl, cardnumber, token = "", expirydate, cvv, fname, lname, email,
				authnumber, acqcode, acq_type, date, rrn, heure, securtoken24, mac_value, transactiontype, paymentid,
				promoCode, callbackUrl, holdername, country, phone, city, state, zipcode, address, mesg_type,
				merc_codeactivite, merchant_name, merchant_city, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, montanttrame, num_trs = "", successURL, failURL, etataut;

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;

		try {
			// Transaction info
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

			orderid = (String) jsonOrequest.get("orderid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");
			paymentid = (String) jsonOrequest.get("paymentid");
			authnumber = (String) jsonOrequest.get("authnumber");

			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackurl = (String) jsonOrequest.get("callbackurl");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");
			// token = (String) jsonOrequest.get("token");
			// expirydate = (String) jsonOrequest.get("expirydate");
			// cvv = (String) jsonOrequest.get("cvv");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "cpautorisation 500 malformed json expression " + jerr);
			return getMsgError(folder, file, null, "cpautorisation 500 malformed json expression " + jerr.getMessage(),
					null);
		}

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		Util.writeInFileTransaction(folder, file, "cpautorisation_" + orderid + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "");
		}

		DemandePaiementDto check_dmd = null;
		HistoAutoGateDto current_hist = null;
		// get demandepaiement id , check if exist
		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);
		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during PaiementRequest", null);
		}
		if (check_dmd == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 PaiementRequest misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 PaiementRequest misconfigured in DB or not existing", "15");
		}

		try {
			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);
		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:[" + orderid
							+ "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]" + err2);
			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Error during Transaction not found orderid:[" + orderid + "] + and authnumber:["
							+ authnumber + "]" + "and merchantid:[" + merchantid + "]",
					null);
		}

		if (current_hist == null) {
			Util.writeInFileTransaction(folder, file, "cpautorisation 500 Transaction not found orderid:[" + orderid
					+ "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]");
			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Transaction not found orderid:["
					+ orderid + "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]",
					null);
		}

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during  date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err2);
			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during  date formatting", null);
		}

		JSONObject jso = new JSONObject();
		String codrep = "", motif, merchnatidauth, dtdem = "";
		Double montantPreAuto = 0.00;
		Double montantCfr = 0.00;
		try {
			acqcode = current_merchant.getCmrCodbqe();
			acq_type = "0000";
			codrep = current_hist.getHatCoderep();
			motif = current_hist.getHatMtfref1();
			merchnatidauth = current_hist.getHatNumcmr();
			dtdem = check_dmd.getDem_pan();
			montantPreAuto = check_dmd.getMontant();
			montantCfr = Double.valueOf(amount);
			successURL = check_dmd.getSuccessURL();
			failURL = check_dmd.getFailURL();
			address = check_dmd.getAddress();
			zipcode = check_dmd.getPostcode();
			phone = check_dmd.getTel();
			country = check_dmd.getCountry();
			city = check_dmd.getCity();
			state = check_dmd.getState();
			if (cardnumber.equals("")) {
				cardnumber = check_dmd.getDem_pan();
			}
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during authdata preparation orderid:[" + orderid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during authdata preparation",
					codrep);
		}
		Util.writeInFileTransaction(folder, file, "montantPreAuto : " + montantPreAuto);
		Util.writeInFileTransaction(folder, file, "montantCfr : " + montantCfr);
		// toujours on fait la telecollecte auto dans la confirmation pre-auto (capture=
		// "Y")
		capture = "Y";
		
		Util.writeInFileTransaction(folder, file, "confirmer telecollecte montantPreAuto");
		if (codrep.equalsIgnoreCase("00")) {
			if(montantCfr <= montantPreAuto) {
				montantPreAuto = montantCfr;
			}
			String capture_status = "N";
			int exp_flag = 0;

			if (capture.equalsIgnoreCase("Y")) {

				Date current_date = null;
				current_date = new Date();
				Util.writeInFileTransaction(folder, file, "Automatic capture start...");

				Util.writeInFileTransaction(folder, file, "Getting authnumber");

				authnumber = current_hist.getHatNautemt();
				Util.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

				Util.writeInFileTransaction(folder, file, "Getting authnumber");
				TransactionDto trs_check = null;

				try {
					trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
				} catch (Exception ee) {

					Util.writeInFileTransaction(folder, file,
							"trs_check trs_check exception e : [" + ee.toString() + "]");
				}

				if (trs_check != null) {
					// do nothing
					Util.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
				} else {

					Util.writeInFileTransaction(folder, file, "inserting into telec start ...");
					try {

						// insert into telec

						TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

						long lidtelc = 0;

						if (n_tlc == null) {
							Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
							Integer idtelc = null;

							TelecollecteDto tlc = null;

							// insert into telec
							idtelc = telecollecteService.getMAX_ID();
							Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);

							lidtelc = idtelc.longValue() + 1;
							tlc = new TelecollecteDto();
							tlc.setTlc_numtlcolcte(lidtelc);
							tlc.setTlc_numtpe(current_hist.getHatCodtpe());
							tlc.setTlc_datcrtfich(current_date);
							tlc.setTlc_nbrtrans(new Double(1));
							tlc.setTlc_gest("N");
							tlc.setTlc_datremise(current_date);
							tlc.setTlc_numremise(new Double(lidtelc));
							// tlc.setTlc_numfich(new Double(0));
							String tmpattern = "HH:mm";
							SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
							String stm = sftm.format(current_date);
							tlc.setTlc_heuremise(stm);
							tlc.setTlc_codbq(acqcode);
							tlc.setTlc_numcmr(merchantid);
							tlc.setTlc_numtpe(websiteid);
							telecollecteService.save(tlc);

						} else {
							Util.writeInFileTransaction(folder, file, "n_tlc !=null ");

							lidtelc = n_tlc.getTlc_numtlcolcte();
							double nbr_trs = n_tlc.getTlc_nbrtrans();

							nbr_trs = nbr_trs + 1;

							n_tlc.setTlc_nbrtrans(nbr_trs);

							telecollecteService.save(n_tlc);

						}

						// insert into transaction
						TransactionDto trs = new TransactionDto();
						trs.setTrsnumcmr(merchantid);
						trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));

						String frmt_cardnumber = Util.formatagePan(cardnumber);
						trs.setTrs_codporteur(frmt_cardnumber);

						trs.setTrs_montant(montantPreAuto);
						// trs.setTrs_dattrans(new Date());

						current_date = new Date();
						Date current_date_1 = getDateWithoutTime(current_date);
						trs.setTrs_dattrans(current_date_1);
						trs.setTrsnumaut(authnumber);
						trs.setTrs_etat("N");
						trs.setTrs_devise(current_hist.getHatDevise());
						trs.setTrs_certif("N");
						Integer idtrs = transactionService.getMAX_ID();
						long lidtrs = idtrs.longValue() + 1;
						trs.setTrs_id(lidtrs);
						trs.setTrs_commande(orderid);
						trs.setTrs_procod("0");
						trs.setTrs_groupe(websiteid);
						transactionService.save(trs);

						current_hist.setHatEtat('T');
						current_hist.setHatdatetlc(current_date);
						current_hist.setOperateurtlc("mxplusapi");
						histoAutoGateService.save(current_hist);

						capture_id = String.format("%040d",
								new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
						Date dt = new Date();
						String dtpattern = "yyyy-MM-dd";
						SimpleDateFormat sfdt = new SimpleDateFormat(dtpattern);
						String sdt = sfdt.format(dt);
						String tmpattern = "HH:mm:ss";
						SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
						String stm = sftm.format(dt);
						Util.writeInFileTransaction(folder, file, "inserting into telec ok");
						capture_status = "Y";

					} catch (Exception e) {
						exp_flag = 1;
						Util.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing " + e);
						codrep = "96";
						motif = "cpautorisation failed";
					}
				}
				if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
					capture_status.equalsIgnoreCase("N");

				Util.writeInFileTransaction(folder, file, "Automatic capture end.");
			}
		} else {
			Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
			Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + codrep + "]");
		}
		
		if (montantCfr > montantPreAuto) {
			Util.writeInFileTransaction(folder, file, "if(montantCfr > montantPreAuto)");
			Double montantToDebite = 0.00;
			montantToDebite = montantCfr - montantPreAuto;
			Date trsdate = null;
			Util.writeInFileTransaction(folder, file,
					"montantToDebite => (montantCfr - montantPreAuto) : " + montantToDebite);

			String orderidToDebite = Util.genCommande(merchantid);
			Util.writeInFileTransaction(folder, file, "generer new commande To debite : " + orderidToDebite);

			int i_card_type = Util.getCardIss(cardnumber);
			DemandePaiementDto dmd = null;
			DemandePaiementDto dmdSaved = null;

			acq_type = "0000";
			reason_code = "H";
			transaction_condition = "6";
			mesg_type = "0";
			processing_code = "";
			String xid = "";
			transactiontype = "0"; // 0 payment , P preauto
			currency = "504";
			cvv = ""; // a revoir
			recurring = "N";
			if (transactiontype.equals("0")) {
				processing_code = "0";
			} else if (transactiontype.equals("P")) {
				processing_code = "P";
			} else {
				processing_code = "0";
			}
			expirydate = ""; // a revoir

			try {
				dmd = new DemandePaiementDto();

				dmd.setComid(merchantid);
				dmd.setCommande(orderidToDebite);
				dmd.setDem_pan(cardnumber);
				dmd.setDem_cvv(cvv);
				dmd.setGalid(websiteid);
				dmd.setSuccessURL(successURL);
				dmd.setFailURL(failURL);
				dmd.setType_carte(i_card_type + "");
				if (amount.equals("") || amount == null) {
					amount = "0";
				}
				if (amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				dmd.setMontant(Double.parseDouble(amount));
				dmd.setNom(lname);
				dmd.setPrenom(fname);
				dmd.setEmail(email);
				dmd.setTel(phone);
				dmd.setAddress(address);
				dmd.setCity(city);
				dmd.setCountry(country);
				dmd.setState(state);
				dmd.setPostcode(zipcode);
				// dmd.setDateexpnaps(expirydate);
				dmd.setLangue("E");
				dmd.setEtat_demande("INIT");

				formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
				formatter_2 = new SimpleDateFormat("HH:mm:ss");
				trsdate = new Date();
				transactiondate = formatter_1.format(trsdate);
				transactiontime = formatter_2.format(trsdate);
				// dmd.setDem_date_time(transactiondate + transactiontime);
				dmd.setDem_date_time(dateFormat.format(new Date()));
				if (recurring.equalsIgnoreCase("Y"))
					dmd.setIs_cof("Y");
				if (recurring.equalsIgnoreCase("N"))
					dmd.setIs_cof("N");

				dmd.setIs_addcard("N");
				dmd.setIs_tokenized("N");
				dmd.setIs_whitelist("N");
				dmd.setIs_withsave("N");

				// generer token
				String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
				dmd.setTokencommande(tokencommande);
				// set transctiontype
				dmd.setTransactiontype(transactiontype);

				dmdSaved = demandePaiementService.save(dmd);
			} catch (Exception err1) {
				Util.writeInFileTransaction(folder, file,
						"authorization 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]"
								+ err1);

				return getMsgError(folder, file, jsonOrequest,
						"authorization 500 Error during DEMANDE_PAIEMENT insertion", null);
			}

			// 2024-03-05
			montanttrame = formatMontantTrame(folder, file, amount, orderidToDebite, merchantid, jsonOrequest);

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(merchantname, 19, ' ');
			Util.writeInFileTransaction(folder, file, "merchant_name : [" + merchant_name + "]");

			merchant_city = "MOROCCO        ";
			Util.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

			// ajout cavv (cavv+eci) xid dans la trame
			String champ_cavv = "";
			/*
			 * xid = threeDSServerTransID; if (cavv == null || eci == null) { champ_cavv =
			 * null; Util.writeInFileTransaction(folder, file,
			 * "cavv == null || eci == null"); } else if (cavv != null && eci != null) {
			 * champ_cavv = cavv + eci; Util.writeInFileTransaction(folder, file,
			 * "cavv != null && eci != null"); Util.writeInFileTransaction(folder, file,
			 * "champ_cavv : [" + champ_cavv + "]"); } else {
			 * Util.writeInFileTransaction(folder, file, "champ_cavv = null"); champ_cavv =
			 * null; }
			 */

			boolean cvv_present = check_cvv_presence(cvv);
			if (!token.equals("")) {
				cvv_present = true;
			}
			cvv_present = true; // a revoir
			String first_auth = "";
			long lrec_serie = 0;

			// controls
			Util.writeInFileTransaction(folder, file, "Switch processing start ...");

			String tlv = "";
			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

			if (!cvv_present) {
				Util.writeInFileTransaction(folder, file,
						"cpautorisation 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				return getMsgError(folder, file, jsonOrequest,
						"cpautorisation 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
						"82");
			}

			// not reccuring , normal
			if (cvv_present) {
				Util.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
				try {
					tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
							.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
							.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
							.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
							.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
							.withField(Tags.tag16, date).withField(Tags.tag17, heure)
							.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
							.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
							.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
							.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
							.withField(Tags.tag168, xid).encode();

					Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
					Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
					Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
					Util.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
					Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
					Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
					Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
					Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
					Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
					Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
					Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
					Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
					Util.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
					Util.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
					Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
					Util.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
					Util.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
					Util.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
					Util.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
					Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
					Util.writeInFileTransaction(folder, file, "tag167_request : [" + champ_cavv + "]");
					Util.writeInFileTransaction(folder, file, "tag168_request : [" + xid + "]");

				} catch (Exception err4) {
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during switch tlv buildup for given orderid:[" + orderidToDebite
									+ "] and merchantid:[" + merchantid + "]" + err4);

					return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during switch tlv buildup",
							"96");
				}
				Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");
			}

			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {
				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				Util.writeInFileTransaction(folder, file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					//return getMsgError(folder, file, jsonOrequest,
					//		"cpautorisation 500 Error Switch communication s_conn false", "96");
					motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
					codrep = "96";
				}

				if (s_conn) {
					Util.writeInFileTransaction(folder, file, "Switch Connected.");
					Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				//return getMsgError(folder, file, jsonOrequest,
				//		"cpautorisation 500 Error Switch communication UnknownHostException", "96");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
				codrep = "96";
			} catch (java.net.ConnectException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				//return getMsgError(folder, file, jsonOrequest,
				//		"cpautorisation 500 Error Switch communication ConnectException", "96");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
				codrep = "96";
			}

			catch (SocketTimeoutException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file,
						"cpautorisation 500 Error Switch communication SocketTimeoutException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				//return getMsgError(folder, file, jsonOrequest, "Switch  malfunction  SocketTimeoutException !!!", "96");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
				codrep = "96";
			}

			catch (IOException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "cpautorisation 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				//return getMsgError(folder, file, jsonOrequest, "Switch  malfunction  IOException !!!", "96");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
				codrep = "96";
			}

			catch (Exception e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				//return getMsgError(folder, file, jsonOrequest,
				//		"cpautorisation 500 Error Switch communication General Exception", "96");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
				codrep = "96";
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				Util.writeInFileTransaction(folder, file, "cpautorisation 500 Error Switch null response"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				//return getMsgError(folder, file, jsonOrequest, "Switch  malfunction resp null!!!", "96");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
				codrep = "96";
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				Util.writeInFileTransaction(folder, file, "cpautorisation 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
			}

			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

			TLVParser tlvp = null;

			String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null, tag9_resp = null,
					tag14_resp = null, tag15_resp = null, tag16_resp = null, tag17_resp = null, tag66_resp = null,
					tag18_resp = null, tag19_resp = null, tag23_resp = null, tag20_resp = null, tag21_resp = null,
					tag22_resp = null, tag80_resp = null, tag98_resp = null;

			if (switch_ko == 0) {
				try {
					tlvp = new TLVParser(resp);

					tag0_resp = tlvp.getTag(Tags.tag0);
					tag1_resp = tlvp.getTag(Tags.tag1);
					tag3_resp = tlvp.getTag(Tags.tag3);
					tag8_resp = tlvp.getTag(Tags.tag8);
					tag9_resp = tlvp.getTag(Tags.tag9);
					tag14_resp = tlvp.getTag(Tags.tag14);
					tag15_resp = tlvp.getTag(Tags.tag15);
					tag16_resp = tlvp.getTag(Tags.tag16);
					tag17_resp = tlvp.getTag(Tags.tag17);
					tag66_resp = tlvp.getTag(Tags.tag66); // f1
					tag18_resp = tlvp.getTag(Tags.tag18);
					tag19_resp = tlvp.getTag(Tags.tag19); // f2
					tag23_resp = tlvp.getTag(Tags.tag23);
					tag20_resp = tlvp.getTag(Tags.tag20);
					tag21_resp = tlvp.getTag(Tags.tag21);
					tag22_resp = tlvp.getTag(Tags.tag22);
					tag80_resp = tlvp.getTag(Tags.tag80);
					tag98_resp = tlvp.getTag(Tags.tag98);

				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// controle switch
				if (tag1_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during tlv Switch response parse tag1_resp tag null"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag1_resp != null && tag1_resp.length() < 3) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag20_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during tlv Switch response parse tag1_resp tag null"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}
			}
			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
			Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			Util.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
			Util.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

			String tag20_resp_verified = "";
			String tag19_res_verified = "";
			String tag66_resp_verified = "";
			tag20_resp_verified = tag20_resp;
			tag19_res_verified = tag19_resp;
			tag66_resp_verified = tag66_resp;
			String s_status, pan_auto = "";

			if (switch_ko == 1) {
				pan_auto = Util.formatagePan(cardnumber);
				Util.writeInFileTransaction(folder, file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + montantToDebite + "/" + date + "/" + merchantid);
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;
			if (tag20_resp == null) {
				tag20_resp = "";
			}
			if (tag20_resp.equalsIgnoreCase("00")) {
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[00]");
				try {
					Util.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE ...");

					dmdSaved.setEtat_demande("SW_PAYE");
					demandePaiementService.save(dmdSaved);
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
									+ orderidToDebite + "]" + e);
				}
				Util.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE OK");
				
				Util.writeInFileTransaction(folder, file, "Insert into Histogate...");

				try {
					hist = new HistoAutoGateDto();
					Date curren_date_hist = new Date();
					int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

					Util.writeInFileTransaction(folder, file, "get status ...");

					s_status = "";
					try {
						CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
						System.out.println("codeReponseDto : " + codeReponseDto);
						Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
						if (codeReponseDto != null) {
							s_status = codeReponseDto.getRpcLibelle();
						}
					} catch (Exception ee) {
						Util.writeInFileTransaction(folder, file, "cpautorisation 500 Error codeReponseDto null");
						ee.printStackTrace();
					}

					Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

					Util.writeInFileTransaction(folder, file, "formatting pan...");

					pan_auto = Util.formatagePan(cardnumber);
					Util.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

					Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");
					
					Util.writeInFileTransaction(folder, file, "websiteid : " + websiteid);

					Date current_date_1 = getDateWithoutTime(curren_date_hist);
					hist.setHatDatdem(current_date_1);

					hist.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
					//hist.setHatMontant(Double.parseDouble(amount));
					hist.setHatMontant(montantToDebite);
					hist.setHatNumcmr(merchantid);
					hist.setHatCoderep(tag20_resp_verified);
					tag20_resp = tag20_resp_verified;
					hist.setHatDevise(currency);
					hist.setHatBqcmr(acqcode);
					hist.setHatPorteur(pan_auto);
					hist.setHatMtfref1(s_status);
					hist.setHatNomdeandeur(websiteid);
					hist.setHatNautemt(tag19_res_verified); // f2
					tag19_resp = tag19_res_verified;
					if (tag22_resp != null)
						hist.setHatProcode(tag22_resp.charAt(0));
					else
						hist.setHatProcode('6');
					hist.setHatExpdate(expirydate);
					hist.setHatRepondeur(tag21_resp);
					hist.setHatTypmsg("3");
					hist.setHatRrn(tag66_resp_verified); // f1
					tag66_resp_verified = tag66_resp;
					hist.setHatEtat('E');
					if (websiteid.equals("")) {
						hist.setHatCodtpe("1");
					} else {
						hist.setHatCodtpe(websiteid);
					}
					hist.setHatMcc(merc_codeactivite);
					hist.setHatNumCommande(orderidToDebite);
					hist.setHatNumdem(new Long(numTransaction));

					if (check_cvv_presence(cvv)) {
						hist.setIs_cvv_verified("Y");
					} else {
						hist.setIs_cvv_verified("N");
					}

					hist.setIs_3ds("N");
					hist.setIs_addcard("N");
					// if (card_destination == 1)
					// hist.setIs_national("Y");
					// else
					// hist.setIs_national("N");
					hist.setIs_whitelist("N");
					hist.setIs_withsave("N");
					hist.setIs_tokenized("N");

					if (recurring.equalsIgnoreCase("Y"))
						hist.setIs_cof("Y");
					if (recurring.equalsIgnoreCase("N"))
						hist.setIs_cof("N");

					Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

					Util.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

					histoAutoGateService.save(hist);
					
					Util.writeInFileTransaction(folder, file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"cpautorisation 500 Error during  insert in histoautogate for given orderid:[" + orderidToDebite
									+ "]" + e);
					try {
						Util.writeInFileTransaction(folder, file, "2eme tentative : HistoAutoGate Saving ... ");
						histoAutoGateService.save(hist);
					} catch (Exception ex) {
						Util.writeInFileTransaction(folder, file,
								"2eme tentative : cpautorisation 500 Error during  insert in histoautogate for given orderid:["
										+ orderidToDebite + "]" + ex);
					}
				}

				Util.writeInFileTransaction(folder, file, "HistoAutoGate OK.");

				HistoAutoGateDto hist1 = null;
				try {
					// get histoauto check if exist
					hist1 = histoAutoGateService.findByHatNumCommandeAndHatNumcmr(orderidToDebite, merchantid);
					if (hist1 == null) {
						hist1 = hist;
					}
				} catch (Exception err2) {
					Util.writeInFileTransaction(folder, file,
							"cpauthorization 500 Error during HistoAutoGate findByNumAuthAndNumCommercant orderid:["
									+ orderidToDebite + "] and merchantid:[" + merchantid + "]" + err2);
				}

				String capture_status = "N";
				int exp_flag = 0;

				if (capture.equalsIgnoreCase("Y")) {

					Date current_date = null;
					current_date = new Date();
					Util.writeInFileTransaction(folder, file, "Automatic capture start...");

					Util.writeInFileTransaction(folder, file, "Getting authnumber");

					authnumber = hist1.getHatNautemt();
					Util.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

					Util.writeInFileTransaction(folder, file, "Getting authnumber");
					TransactionDto trs_check = null;

					try {
						trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
					} catch (Exception ee) {

						Util.writeInFileTransaction(folder, file,
								"trs_check trs_check exception e : [" + ee.toString() + "]");
					}

					if (trs_check != null) {
						// do nothing
						Util.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
					} else {

						Util.writeInFileTransaction(folder, file, "inserting into telec start ...");
						try {

							// insert into telec

							TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

							long lidtelc = 0;

							if (n_tlc == null) {
								Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
								Integer idtelc = null;

								TelecollecteDto tlc = null;

								// insert into telec
								idtelc = telecollecteService.getMAX_ID();
								Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);

								lidtelc = idtelc.longValue() + 1;
								tlc = new TelecollecteDto();
								tlc.setTlc_numtlcolcte(lidtelc);

								tlc.setTlc_numtpe(hist1.getHatCodtpe());

								tlc.setTlc_datcrtfich(current_date);
								tlc.setTlc_nbrtrans(new Double(1));
								tlc.setTlc_gest("N");

								tlc.setTlc_datremise(current_date);
								tlc.setTlc_numremise(new Double(lidtelc));
								// tlc.setTlc_numfich(new Double(0));
								String tmpattern = "HH:mm";
								SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
								String stm = sftm.format(current_date);
								tlc.setTlc_heuremise(stm);

								tlc.setTlc_codbq(acqcode);
								tlc.setTlc_numcmr(merchantid);
								tlc.setTlc_numtpe(websiteid);
								telecollecteService.save(tlc);

							} else {
								Util.writeInFileTransaction(folder, file, "n_tlc !=null ");

								lidtelc = n_tlc.getTlc_numtlcolcte();
								double nbr_trs = n_tlc.getTlc_nbrtrans();

								nbr_trs = nbr_trs + 1;

								n_tlc.setTlc_nbrtrans(nbr_trs);

								telecollecteService.save(n_tlc);

							}

							// insert into transaction
							TransactionDto trs = new TransactionDto();
							trs.setTrsnumcmr(merchantid);
							trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));

							String frmt_cardnumber = Util.formatagePan(cardnumber);
							trs.setTrs_codporteur(frmt_cardnumber);

							trs.setTrs_montant(montantToDebite);
							// trs.setTrs_dattrans(new Date());

							current_date = new Date();
							Date current_date_1 = getDateWithoutTime(current_date);
							trs.setTrs_dattrans(current_date_1);

							trs.setTrsnumaut(authnumber);
							trs.setTrs_etat("N");
							trs.setTrs_devise(hist1.getHatDevise());
							trs.setTrs_certif("N");
							Integer idtrs = transactionService.getMAX_ID();
							long lidtrs = idtrs.longValue() + 1;
							trs.setTrs_id(lidtrs);
							trs.setTrs_commande(orderidToDebite);
							trs.setTrs_procod("0");
							trs.setTrs_groupe(websiteid);
							transactionService.save(trs);

							hist1.setHatEtat('T');
							hist1.setHatdatetlc(current_date);
							hist1.setOperateurtlc("mxplusapi");
							histoAutoGateService.save(hist1);

							capture_id = String.format("%040d",
									new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
							Date dt = new Date();
							String dtpattern = "yyyy-MM-dd";
							SimpleDateFormat sfdt = new SimpleDateFormat(dtpattern);
							String sdt = sfdt.format(dt);
							String tmpattern = "HH:mm:ss";
							SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
							String stm = sftm.format(dt);
							Util.writeInFileTransaction(folder, file, "inserting into telec ok");
							capture_status = "Y";

						} catch (Exception e) {
							exp_flag = 1;
							Util.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing " + e);
							codrep = "96";
							motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
						}
					}
					if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
						capture_status.equalsIgnoreCase("N");

					Util.writeInFileTransaction(folder, file, "Automatic capture end.");
				}
			} else {
				Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + codrep + "]");
				motif = "cpautorisation pre-autorisation approuved, but supplement amount failed";
			}
			
		}

		Util.writeInFileTransaction(folder, file, "Preparing cpautorisation api response");
		try {

			// Transaction info
			jso.put("statuscode", codrep);
			jso.put("status", motif);
			jso.put("etataut", "Y");
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("transactiondate", date);
			jso.put("transactiontime", heure);
			jso.put("authnumber", authnumber);
			jso.put("paymentid", paymentid);
			jso.put("transactionid", transactionid);

			// Merchant info
			jso.put("merchantid", merchnatidauth);
			jso.put("merchantname", merchantname);
			jso.put("websitename", websiteName);
			jso.put("websiteid", websiteid);

			// Card info
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// Client info
			jso.put("fname", fname);
			jso.put("lname", lname);
			jso.put("email", email);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception jsouterr) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during jso out processing given authnumber:[" + authnumber + "]"
							+ jsouterr);
			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during jso out processing",
					codrep);
		}

		System.out.println("cpautorisation api response :  [" + jso.toString() + "]");
		Util.writeInFileTransaction(folder, file, "cpautorisation api response :  [" + jso.toString() + "]");

		Util.writeInFileTransaction(folder, file, "*********** Fin cpautorisation() ************** ");
		System.out.println("*********** Fin cpautorisation() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/cpautorisationOLD", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String cpautorisationOLD(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cpauths,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CPAUTORISATION" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start cpautorisation() ************** ");
		System.out.println("*********** Start cpautorisation() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		logger.info("cpautorisation api call start ...");

		Util.writeInFileTransaction(folder, file, "cpautorisation api call start ...");

		Util.writeInFileTransaction(folder, file, "cpautorisation : [" + cpauths + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(cpauths);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 malformed json expression " + cpauths + jserr);
			return getMsgError(folder, file, null, "cpautorisation 500 malformed json expression", null);
		}

		if (header != null)
			Util.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			Util.writeInFileTransaction(folder, file, "error header is null !");

		try {

			if (header != null) {

				if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				else if (header.get("x-api-key") != null)
					api_key = (String) header.get("x-api-key").get(0);
				if (header.get("x-product") != null)
					api_product = (String) header.get("x-product").get(0);
				else if (header.get("X-PRODUCT") != null)
					api_product = (String) header.get("X-PRODUCT").get(0);
				if (header.get("x-version") != null)
					api_version = (String) header.get("x-version").get(0);
				else if (header.get("X-VERSION") != null)
					api_version = (String) header.get("X-VERSION").get(0);
				if (header.get("user-agent") != null)
					api_user_agent = (String) header.get("user-agent").get(0);
				else if (header.get("USER-AGENT") != null)
					api_user_agent = (String) header.get("USER-AGENT").get(0);
			}

		} catch (Exception head_err) {
			if (header.toString() != null) {
				Util.writeInFileTransaction(folder, file,
						"cpautorisation 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "cpautorisation 500 malformed header", null);
			}

			else {
				Util.writeInFileTransaction(folder, file, "cpautorisation 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "cpautorisation 500 malformed header " + head_err.getMessage(),
						null);
			}

		}

		String capture, currency, orderid, recurring, amount, transactionid, merchantid, capture_id, merchantname,
				websiteName, websiteid, callbackurl, cardnumber, token, expirydate, cvv, fname, lname, email,
				authnumber, acqcode, acq_type, date, rrn, heure, securtoken24, mac_value, transactiontype, paymentid;

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;

		try {
			// Transaction info
			securtoken24 = (String) jsonOrequest.get("securtoken24");
			mac_value = (String) jsonOrequest.get("mac_value");

			orderid = (String) jsonOrequest.get("orderid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");
			paymentid = (String) jsonOrequest.get("paymentid");
			authnumber = (String) jsonOrequest.get("authnumber");

			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
			merchantname = (String) jsonOrequest.get("merchantname");
			websiteName = (String) jsonOrequest.get("websitename");
			websiteid = (String) jsonOrequest.get("websiteid");
			callbackurl = (String) jsonOrequest.get("callbackurl");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");
			// token = (String) jsonOrequest.get("token");
			// expirydate = (String) jsonOrequest.get("expirydate");
			// cvv = (String) jsonOrequest.get("cvv");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "cpautorisation 500 malformed json expression " + jerr);
			return getMsgError(folder, file, null, "cpautorisation 500 malformed json expression " + jerr.getMessage(),
					null);
		}

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		Util.writeInFileTransaction(folder, file, "cpautorisation_" + orderid + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "");
		}

		DemandePaiementDto check_dmd = null;
		HistoAutoGateDto current_hist = null;
		// get demandepaiement id , check if exist
		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during PaiementRequest", null);
		}
		if (check_dmd == null) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 PaiementRequest misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 PaiementRequest misconfigured in DB or not existing", "15");
		}

		try {
			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:[" + orderid
							+ "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]" + err2);
			return getMsgError(folder, file, jsonOrequest,
					"cpautorisation 500 Error during Transaction not found orderid:[" + orderid + "] + and authnumber:["
							+ authnumber + "]" + "and merchantid:[" + merchantid + "]",
					null);
		}

		if (current_hist == null) {
			Util.writeInFileTransaction(folder, file, "cpautorisation 500 Transaction not found orderid:[" + orderid
					+ "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]");
			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Transaction not found orderid:["
					+ orderid + "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]",
					null);
		}

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during  date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err2);
			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during  date formatting", null);
		}

		JSONObject jso = new JSONObject();

		acqcode = current_merchant.getCmrCodbqe();
		acq_type = "0000";

		String codrep = "", motif, merchnatidauth, dtdem = "";
		try {
			codrep = current_hist.getHatCoderep();
			motif = current_hist.getHatMtfref1();
			merchnatidauth = current_hist.getHatNumcmr();
			dtdem = check_dmd.getDem_pan();
			if (cardnumber.equals("")) {
				cardnumber = current_hist.getHatPorteur();
			}
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during authdata preparation orderid:[" + orderid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during authdata preparation",
					codrep);
		}
		if (codrep.equalsIgnoreCase("00")) {

			String capture_status = "N";
			int exp_flag = 0;
			// toujours on fait la telecollecte auto dans la confirmation pre-auto (capture
			// = "Y")
			capture = "Y";

			if (capture.equalsIgnoreCase("Y")) {

				Date current_date = null;
				current_date = new Date();
				Util.writeInFileTransaction(folder, file, "Automatic capture start...");

				Util.writeInFileTransaction(folder, file, "Getting authnumber");

				authnumber = current_hist.getHatNautemt();
				Util.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

				Util.writeInFileTransaction(folder, file, "Getting authnumber");
				TransactionDto trs_check = null;

				try {
					trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
				} catch (Exception ee) {

					Util.writeInFileTransaction(folder, file,
							"trs_check trs_check exception e : [" + ee.toString() + "]");
				}

				if (trs_check != null) {
					// do nothing
					Util.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
				} else {

					Util.writeInFileTransaction(folder, file, "inserting into telec start ...");
					try {

						// insert into telec

						TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

						long lidtelc = 0;

						if (n_tlc == null) {
							Util.writeInFileTransaction(folder, file, "getMAXTLC_N n_tlc = null");
							Integer idtelc = null;

							TelecollecteDto tlc = null;

							// insert into telec
							idtelc = telecollecteService.getMAX_ID();
							Util.writeInFileTransaction(folder, file, "getMAX_ID idtelc : " + idtelc);

							lidtelc = idtelc.longValue() + 1;
							tlc = new TelecollecteDto();
							tlc.setTlc_numtlcolcte(lidtelc);

							tlc.setTlc_numtpe(current_hist.getHatCodtpe());

							tlc.setTlc_datcrtfich(current_date);
							tlc.setTlc_nbrtrans(new Double(1));
							tlc.setTlc_gest("N");

							tlc.setTlc_datremise(current_date);
							tlc.setTlc_numremise(new Double(lidtelc));
							// tlc.setTlc_numfich(new Double(0));
							String tmpattern = "HH:mm";
							SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
							String stm = sftm.format(current_date);
							tlc.setTlc_heuremise(stm);

							tlc.setTlc_codbq(acqcode);
							tlc.setTlc_numcmr(merchantid);
							tlc.setTlc_numtpe(websiteid);
							telecollecteService.save(tlc);

						} else {
							Util.writeInFileTransaction(folder, file, "n_tlc !=null ");

							lidtelc = n_tlc.getTlc_numtlcolcte();
							double nbr_trs = n_tlc.getTlc_nbrtrans();

							nbr_trs = nbr_trs + 1;

							n_tlc.setTlc_nbrtrans(nbr_trs);

							telecollecteService.save(n_tlc);

						}

						// insert into transaction
						TransactionDto trs = new TransactionDto();
						trs.setTrsnumcmr(merchantid);
						trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));

						String frmt_cardnumber = Util.formatagePan(cardnumber);
						trs.setTrs_codporteur(frmt_cardnumber);

						double dmnt = 0;

						dmnt = Double.parseDouble(amount);

						trs.setTrs_montant(dmnt);
						// trs.setTrs_dattrans(new Date());

						current_date = new Date();
						Date current_date_1 = getDateWithoutTime(current_date);
						trs.setTrs_dattrans(current_date_1);

						trs.setTrsnumaut(authnumber);
						trs.setTrs_etat("N");
						trs.setTrs_devise(current_hist.getHatDevise());
						trs.setTrs_certif("N");
						Integer idtrs = transactionService.getMAX_ID();
						long lidtrs = idtrs.longValue() + 1;
						trs.setTrs_id(lidtrs);
						trs.setTrs_commande(orderid);
						trs.setTrs_procod("0");
						trs.setTrs_groupe(websiteid);
						transactionService.save(trs);

						current_hist.setHatEtat('T');
						current_hist.setHatdatetlc(current_date);
						current_hist.setOperateurtlc("mxplusapi");
						histoAutoGateService.save(current_hist);

						capture_id = String.format("%040d",
								new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
						Date dt = new Date();
						String dtpattern = "yyyy-MM-dd";
						SimpleDateFormat sfdt = new SimpleDateFormat(dtpattern);
						String sdt = sfdt.format(dt);
						String tmpattern = "HH:mm:ss";
						SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
						String stm = sftm.format(dt);
						Util.writeInFileTransaction(folder, file, "inserting into telec ok");
						capture_status = "Y";

					} catch (Exception e) {
						exp_flag = 1;
						Util.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing " + e);
						codrep = "96";
						motif = "cpautorisation failed";
					}
				}
				if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
					capture_status.equalsIgnoreCase("N");

				Util.writeInFileTransaction(folder, file, "Automatic capture end.");
			}

		} else {
			Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
			Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + codrep + "]");
		}

		// JSONObject jso = new JSONObject();

		Util.writeInFileTransaction(folder, file, "Preparing cpautorisation api response");

		// reccurent transaction processing

		// reccurent insert and update

		try {

			// Transaction info
			jso.put("statuscode", codrep);
			jso.put("status", motif);
			jso.put("etataut", "Y");
			jso.put("orderid", orderid);
			jso.put("amount", amount);
			jso.put("transactiondate", date);
			jso.put("transactiontime", heure);
			jso.put("authnumber", authnumber);
			jso.put("paymentid", paymentid);
			jso.put("transactionid", transactionid);

			// Merchant info
			jso.put("merchantid", merchnatidauth);
			jso.put("merchantname", merchantname);
			jso.put("websitename", websiteName);
			jso.put("websiteid", websiteid);

			// Card info
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// Client info
			jso.put("fname", fname);
			jso.put("lname", lname);
			jso.put("email", email);

			Util.writeInFileTransaction(folder, file, "json res : [" + jso.toString() + "]");
			System.out.println("json res : [" + jso.toString() + "]");

		} catch (Exception jsouterr) {
			Util.writeInFileTransaction(folder, file,
					"cpautorisation 500 Error during jso out processing given authnumber:[" + authnumber + "]"
							+ jsouterr);
			return getMsgError(folder, file, jsonOrequest, "cpautorisation 500 Error during jso out processing",
					codrep);
		}

		System.out.println("cpautorisation api response :  [" + jso.toString() + "]");
		Util.writeInFileTransaction(folder, file, "cpautorisation api response :  [" + jso.toString() + "]");

		Util.writeInFileTransaction(folder, file, "*********** Fin cpautorisation() ************** ");
		System.out.println("*********** Fin cpautorisation() ************** ");

		return jso.toString();
	}

	public JSONObject verifieToken(String securtoken24, String file) {
		JSONObject jso = new JSONObject();
		if (!securtoken24.equals("")) {
			JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
			String token = securtoken24;
			try {
				String userFromToken = jwtTokenUtil.getUsernameFromToken(token, secret);
				Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token, secret);
				Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token, secret);

				Util.writeInFileTransaction(folder, file, "userFromToken generated : " + userFromToken);
				String dateSysStr = dateFormat.format(new Date());
				Util.writeInFileTransaction(folder, file, "dateSysStr : " + dateSysStr);
				Util.writeInFileTransaction(folder, file, "dateExpiration : " + dateExpiration);
				String dateExpirationStr = dateFormat.format(dateExpiration);
				Util.writeInFileTransaction(folder, file, "dateExpirationStr : " + dateExpirationStr);
				String condition = isTokenExpired == false ? "NO" : "YES";
				Util.writeInFileTransaction(folder, file, "token is expired : " + condition);
				if (condition.equalsIgnoreCase("YES")) {
					Util.writeInFileTransaction(folder, file, "Error 500 securtoken24 is expired");

					// Transaction info
					jso.put("statuscode", "17");
					jso.put("status", "Error 500 securtoken24 is expired");

					return jso;
				} else {
					jso.put("statuscode", "00");
				}
			} catch (Exception ex) {
				// Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 securtoken24 " + ex.getMessage());
				System.out.println(jso.get("status"));

				return jso;
			}
		}
		return jso;
	}

	public String getMsgError(String folder, String file, JSONObject jsonOrequest, String msg, String coderep) {
		Util.writeInFileTransaction(folder, file, "*********** Start getMsgError() ************** ");
		System.out.println("*********** Start getMsgError() ************** ");

		JSONObject jso = new JSONObject();
		if (jsonOrequest != null) {
			jso.put("orderid", (String) jsonOrequest.get("orderid"));
			jso.put("merchantid", (String) jsonOrequest.get("merchantid"));
			jso.put("amount", (String) jsonOrequest.get("amount"));
		} else {
			jso.put("orderid", "");
			jso.put("merchantid", "");
			jso.put("amount", "");
		}
		if (coderep != null) {
			jso.put("statuscode", coderep);
		} else {
			jso.put("statuscode", "17");
		}

		jso.put("status", msg);
		jso.put("etataut", "N");
		jso.put("linkacs", "");
		jso.put("url", "");
		jso.put("idDemande", "");

		Util.writeInFileTransaction(folder, file, "json : " + jso.toString());
		System.out.println("json : " + jso.toString());

		Util.writeInFileTransaction(folder, file, "*********** Fin getMsgError() ************** ");
		System.out.println("*********** Fin getMsgError() ************** ");
		return jso.toString();
	}

	public String getMsgErrorV1(String folder, String file, JSONObject jsonOrequest, String msg, String coderep) {
		Util.writeInFileTransaction(folder, file, "*********** Start getMsgErrorV1() ************** ");
		System.out.println("*********** Start getMsgErrorV1() ************** ");

		JSONObject jso = new JSONObject();
		if (jsonOrequest != null) {
			jso.put("merchantid", (String) jsonOrequest.get("merchantid"));
		} else {
			jso.put("orderid", "");
			jso.put("merchantid", "");
			jso.put("amount", "");
		}
		if (coderep != null) {
			jso.put("statuscode", coderep);
		} else {
			jso.put("statuscode", "17");
		}

		jso.put("status", msg);
		jso.put("etataut", "N");
		jso.put("linkacs", "");
		jso.put("url", "");
		jso.put("idDemande", "");

		Util.writeInFileTransaction(folder, file, "json : " + jso.toString());
		System.out.println("json : " + jso.toString());

		Util.writeInFileTransaction(folder, file, "*********** Fin getMsgErrorV1() ************** ");
		System.out.println("*********** Fin getMsgErrorV1() ************** ");
		return jso.toString();
	}

	private boolean is_reccuring_check(String recurring) {
		if (recurring.equalsIgnoreCase("Y"))
			return true;
		else
			return false;
	}

	private boolean check_reccuring_flag(String recurring) {
		if (recurring == null)
			return false;
		if (recurring.length() < 1)
			return false;
		if (recurring.length() > 1)
			return false;
		if (recurring.equalsIgnoreCase("Y") || recurring.equalsIgnoreCase("N"))
			return true;
		return false;
	}

	private boolean check_cvv_presence(String cvv) {
		if (cvv == null)
			return false;
		if (cvv.length() < 3)
			return false;
		if (cvv.length() > 3)
			return false;
		return true;
	}

	@SuppressWarnings("deprecation")
	private Date getDateWithoutTime(Date d) {

		if (d == null)
			d = new Date();
		Date d_notime = null;
		try {

			d_notime = new Date(d.getYear(), d.getMonth(), d.getDate());

			d_notime.setHours(0);
			d_notime.setMinutes(0);
			d_notime.setSeconds(0);

		} catch (Exception e) {
			return d; // leave it as it is if not null
		}
		return d_notime;

	}

	private String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid,
			JSONObject jsonOrequest) {
		String montanttrame;
		String[] mm;
		String[] m;
		try {
			montanttrame = "";

			mm = new String[2];

			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			System.out.println("montant : [" + amount + "]");
			Util.writeInFileTransaction(folder, file, "montant : [" + amount + "]");

			String montantt = amount + "";

			mm = montantt.split("\\.");
			if (mm[1].length() == 1) {
				montanttrame = amount + "0";
			} else {
				montanttrame = amount + "";
			}

			m = new String[2];
			m = montanttrame.split("\\.");
			if (m[1].equals("0")) {
				montanttrame = montanttrame.replace(".", "0");
			} else
				montanttrame = montanttrame.replace(".", "");
			montanttrame = Util.formatageCHamps(montanttrame, 12);
			System.out.println("montanttrame : [" + montanttrame + "]");
			Util.writeInFileTransaction(folder, file, "montanttrame : [" + montanttrame + "]");
		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error during  amount formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(folder, file, jsonOrequest, "authorization 500 Error during  amount formatting", null);
		}
		return montanttrame;
	}

}
