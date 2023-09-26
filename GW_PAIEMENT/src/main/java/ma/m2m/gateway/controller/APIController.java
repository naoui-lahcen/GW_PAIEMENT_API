package ma.m2m.gateway.controller;

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

import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.RequestDto;
import ma.m2m.gateway.dto.SWHistoAutoDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.model.SWHistoAuto;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.risk.GWRiskAnalysis;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.ControlRiskCmrService;
import ma.m2m.gateway.service.DemandePaiementService;
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
//import ma.m2m.gateway.service.SWHistoAutoService;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class APIController {

	private static Logger logger = LogManager.getLogger(APIController.class);

	private Traces traces = new Traces();
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

	// @Autowired
	// SWHistoAutoService swHistoAutoService;

	@Autowired
	CardtokenService cardtokenService;

	@Autowired
	private ControlRiskCmrService controlRiskCmrService;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public APIController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
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
		file = "API_AUTH_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start authorization() ************** ");
		System.out.println("*********** Start authorization() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		logger.info("authorization api call start ...");

		traces.writeInFileTransaction(folder, file, "authorization api call start ...");

		traces.writeInFileTransaction(folder, file, "authorization : [" + auths + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(auths);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "authorization 500 malformed json expression" + auths + jserr);
			return getMsgError(null, "authorization 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file,
						"authorization 500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "authorization 500 malformed header", null);
			}

			else {
				traces.writeInFileTransaction(folder, file, "authorization 500 malformed header" + head_err);
				return getMsgError(null, "authorization 500 malformed header", null);
			}

		}

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", successURL, failURL,
				transactiontype, etataut;
		// JSDONRequest
		// Transaction info
		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		try {
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
			traces.writeInFileTransaction(folder, file, "authorization 500 malformed json expression" + jerr);
			return getMsgError(null, "authorization 500 malformed json expression", null);
		}
		// get cardnumber by token
		if (!token.equals("") && token != null) {
			try {
				CardtokenDto card = cardtokenService.findByIdMerchantAndToken(merchantid, token);
				if (card != null) {
					if (card.getCardNumber() != null) {
						cardnumber = card.getCardNumber();
					}
				}
			} catch (Exception jerr) {
				traces.writeInFileTransaction(folder, file, "authorization 500 token nout found" + jerr);
				return getMsgError(jsonOrequest, "authorization 500 token not found", null);
			}
		}

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		traces.writeInFileTransaction(folder, file, "authorization_" + orderid + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Merchant misconfigured in DB or not existing", "");
		}

		// get demandepaiement id , check if exist

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(jsonOrequest, "authorization 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Error Already exist in PaiementRequest", "16");
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			traces.writeInFileTransaction(folder, file, "authorization 500 Card number length is incorrect orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Card number length is incorrect", null);
		}

		if (i_card_valid == 2) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Card number  is not valid incorrect luhn check orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "authorization 500 Card number  is not valid incorrect luhn check", null);
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
			dmd.setDateexpnaps(expirydate);
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
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]"
							+ err1);

			return getMsgError(jsonOrequest, "authorization 500 Error during DEMANDE_PAIEMENT insertion", null);
		}

		// for test control risk
		GWRiskAnalysis riskAnalysis = new GWRiskAnalysis(folder, file);
		try {
			ControlRiskCmrDto controlRiskCmr = controlRiskCmrService.findByNumCommercant(dmdSaved.getComid());
			List<HistoAutoGateDto> porteurFlowPerDay = histoAutoGateService
					.getPorteurMerchantFlowPerDay(dmdSaved.getComid(), dmdSaved.getDem_pan());
			String msg = riskAnalysis.executeRiskControls(dmdSaved.getComid(), dmdSaved.getMontant(),
					dmdSaved.getDem_pan(), controlRiskCmr, porteurFlowPerDay);
			if (!msg.equalsIgnoreCase("OK")) {
				traces.writeInFileTransaction(folder, file, "authorization 500" + msg);
				return getMsgError(jsonOrequest, "authorization 500 " + msg, null);
			}
			// fin control risk
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 ControlRiskCmr misconfigured in DB or not existing merchantid:[" + dmdSaved.getComid()
							+ e);
			return getMsgError(jsonOrequest, "authorization 500 Error ControlRiskCmr", null);
		}
		
		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();

		} catch (Exception err2) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Error during  date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(jsonOrequest, "authorization 500 Error during  date formatting", null);
		}

		JSONObject jso = new JSONObject();

		// appel 3DSSecure ***********************************************************

		ThreeDSecureResponse threeDsecureResponse = autorisationService.preparerReqThree3DSS(dmdSaved, folder, file);
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
		String idDemande = "";
		String expiry = ""; // YYMM

		/*
		 * ------------ DEBUT COF INSTANCES ------------
		 */
		// TokenProcessor tk = new TokenProcessor();
		// Param_COF current_pcof = null;
		/*
		 * ------------ END COF INSTANCES ------------
		 */
		String link = "";
		String ref = "";
		String idService = "";
		String IdTxMTC = "";
		String statut = "";
		String retourWSKey5 = "";
		String Sec;
		String idTxSysPmt = "";
		String dateTX = "";
		int idsysPmt;
		String numTrans = "";

		// long numTransaction;

		if (threeDsecureResponse.getReponseMPI() != null) {
			reponseMPI = threeDsecureResponse.getReponseMPI();
		}
		if (threeDsecureResponse.getIdDemande() != null) {
			idDemande = threeDsecureResponse.getIdDemande();
		}
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
			traces.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
			dmdSaved.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmdSaved);
			traces.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO idDemande null : " + dmdSaved.toString());
			return getMsgError(jsonOrequest, "AUTO INVALIDE DEMANDE MPI_KO", "96");
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			Util.writeInFileTransaction(folder, file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			return getMsgError(jsonOrequest, "AUTO INVALIDE DEMANDE NOT FOUND", "96");
		}

		if (reponseMPI.equals("") || reponseMPI == null) {
			dmd.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmd);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			Util.writeInFileTransaction(folder, file, "Response 3DS is null");
			return getMsgError(jsonOrequest, "Response 3DS is null", "96");
		}
		// for test
		// reponseMPI = "D";
		// String htmlCreq = "<form action='https://acs2.bankofafrica.ma:443/lacs2'
		// method='post'enctype='application/x-www-form-urlencoded'><input
		// type='hidden'name='creq'value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjllZjUwNjk3LWRiMTctNGZmMy04MDYzLTc0ZTAwMTk0N2I4YiIsCiAgImFjc1RyYW5zSUQiOiAiZjM2ZDA3ZWQtZGJhOS00ZTkzLWE2OGMtMzNmYjAyMDgxZDVmIiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0='
		// /></form>";
		// threeDsecureResponse.setHtmlCreq(htmlCreq);
		// fin
		if (reponseMPI.equals("Y")) {
			// ********************* Frictionless responseMPI equal Y *********************
			traces.writeInFileTransaction(folder, file,
					"********************* Cas frictionless responseMPI equal Y *********************");

			dmd.setDem_xid(threeDSServerTransID);
			demandePaiementService.save(dmd);

			try {
				montanttrame = "";

				mm = new String[2];
				System.out.println("montant v0 : " + amount);
				traces.writeInFileTransaction(folder, file, "montant v0 : " + amount);

				if (amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				if (!amount.contains(".") && !amount.contains(",")) {
					amount = amount + "." + "00";
				}
				System.out.println("montant v1 : " + amount);
				traces.writeInFileTransaction(folder, file, "montant v1 : " + amount);

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
				System.out.println("montanttrame : " + montanttrame);
				traces.writeInFileTransaction(folder, file, "montanttrame : " + montanttrame);
			} catch (Exception err3) {
				traces.writeInFileTransaction(folder, file,
						"authorization 500 Error during  amount formatting for given orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err3);

				return getMsgError(jsonOrequest, "authorization 500 Error during  amount formatting", null);
			}

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(merchantname, 19, ' ');
			traces.writeInFileTransaction(folder, file, "merchant_name : [" + merchant_name + "]");

			merchant_city = "MOROCCO        ";
			traces.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

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
				traces.writeInFileTransaction(folder, file, "cavv == null || eci == null");
			} else if (cavv != null && eci != null) {
				champ_cavv = cavv + eci;
				traces.writeInFileTransaction(folder, file, "cavv != null && eci != null");
				traces.writeInFileTransaction(folder, file, "champ_cavv : [" + champ_cavv + "]");
			} else {
				traces.writeInFileTransaction(folder, file, "champ_cavv = null");
				champ_cavv = null;
			}

			boolean cvv_present = check_cvv_presence(cvv);
			boolean is_reccuring = is_reccuring_check(recurring);
			boolean is_first_trs = true;

			String first_auth = "";
			long lrec_serie = 0;

			// controls
			traces.writeInFileTransaction(folder, file, "Switch processing start ...");

			String tlv = "";
			traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

			if (!cvv_present && !is_reccuring) {
				traces.writeInFileTransaction(folder, file,
						"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				return getMsgError(jsonOrequest,
						"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
						"82");
			}

			// not reccuring , normal
			if (cvv_present && !is_reccuring) {
				traces.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
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

					traces.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
					traces.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
					traces.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
					traces.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
					traces.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
					traces.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
					traces.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
					traces.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
					traces.writeInFileTransaction(folder, file, "tag18_request : [761454]");
					traces.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
					traces.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
					traces.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
					traces.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
					traces.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
					traces.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
					traces.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
					traces.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
					traces.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
					traces.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
					traces.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
					traces.writeInFileTransaction(folder, file, "tag167_request : [" + champ_cavv + "]");
					traces.writeInFileTransaction(folder, file, "tag168_request : [" + xid + "]");

				} catch (Exception err4) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err4);

					return getMsgError(jsonOrequest, "authorization 500 Error during switch tlv buildup", "96");
				}

				traces.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

			}

			// reccuring
			if (is_reccuring) {
				traces.writeInFileTransaction(folder, file, "reccuring");
			}

			traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
//			SwitchTCPClient sw = SwitchTCPClient.getInstance();
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {

				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				traces.writeInFileTransaction(folder, file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					return getMsgError(jsonOrequest, "authorization 500 Error Switch communication s_conn false", "96");
				}

				if (s_conn) {
					traces.writeInFileTransaction(folder, file, "Switch Connected.");
					traces.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					traces.writeInFileTransaction(folder, file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				return getMsgError(jsonOrequest, "authorization 500 Error Switch communication UnknownHostException",
						"96");

			} catch (java.net.ConnectException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				return getMsgError(jsonOrequest, "authorization 500 Error Switch communication ConnectException", "96");
			}

			catch (SocketTimeoutException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				traces.writeInFileTransaction(folder, file,
						"authorization 500 Error Switch communication SocketTimeoutException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return getMsgError(jsonOrequest, "Switch  malfunction  SocketTimeoutException !!!", "96");
			}

			catch (IOException e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				traces.writeInFileTransaction(folder, file, "authorization 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return getMsgError(jsonOrequest, "Switch  malfunction  IOException !!!", "96");
			}

			catch (Exception e) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				return getMsgError(jsonOrequest, "authorization 500 Error Switch communication General Exception",
						"96");
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				traces.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				traces.writeInFileTransaction(folder, file, "authorization 500 Error Switch null response"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return getMsgError(jsonOrequest, "Switch  malfunction resp null!!!", "96");
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				traces.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				traces.writeInFileTransaction(folder, file,
						"authorization 500 Error Switch short response length() < 3 " + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
			}

			traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			traces.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

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
					traces.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// controle switch
				if (tag1_resp == null) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag1_resp != null && tag1_resp.length() < 3) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag20_resp == null) {
					traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
					switch_ko = 1;
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}
			}
			traces.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
			traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

			traces.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
			traces.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

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
				traces.writeInFileTransaction(folder, file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + merchantid);
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;

			traces.writeInFileTransaction(folder, file, "Insert into Histogate...");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

				traces.writeInFileTransaction(folder, file, "get status ...");

				// s_status = histservice.getLib("RPC_LIBELLE", "CODEREPONSE", "RPC_CODE='" +
				// tag20_resp + "'");
				// if (s_status == null)
				s_status = "";
				traces.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

				traces.writeInFileTransaction(folder, file, "get max id ...");

				// Ihist_id = hist.getMAX_ID("HISTOAUTO_GATE", "HAT_ID");
				// Ihist_id = histoAutoGateService.getMAX_ID();
				// long currentid = Ihist_id.longValue() + 1;
				// hist.setId(currentid);

				traces.writeInFileTransaction(folder, file, "max id : [" + Ihist_id + "]");

				traces.writeInFileTransaction(folder, file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);
				traces.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

				traces.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");

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
				hist.setHatCodtpe(websiteid);
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

				traces.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

				traces.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

				histoAutoGateService.save(hist);

			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file, "authorization 500"
						+ "Error during  insert in histoautogate for given orderid:[" + orderid + "]" + e);
			}

			traces.writeInFileTransaction(folder, file, "HistoAutoGate OK.");

			if (tag20_resp == null) {
				tag20_resp = "";
			}

			if (tag20_resp.equalsIgnoreCase("00"))

			{
				traces.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[00]");
				try {
					traces.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE ...");

					dmd.setEtat_demande("SW_PAYE");
					demandePaiementService.save(dmd);
				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
									+ orderid + "]" + e);
				}

				traces.writeInFileTransaction(folder, file, "udapate etat demande : SW_PAYE OK");

				String capture_status = "N";
				int exp_flag = 0;

				if (capture.equalsIgnoreCase("Y")) {

					Date current_date = null;
					current_date = new Date();
					traces.writeInFileTransaction(folder, file, "Automatic capture start...");

					traces.writeInFileTransaction(folder, file, "Getting authnumber");

					String authnumber = hist.getHatNautemt();
					traces.writeInFileTransaction(folder, file, "authnumber : [" + authnumber + "]");

					traces.writeInFileTransaction(folder, file, "Getting authnumber");
					TransactionDto trs_check = null;

					try {
						trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);
					} catch (Exception ee) {

						traces.writeInFileTransaction(folder, file,
								"trs_check trs_check exception e : [" + ee.toString() + "]");
					}

					if (trs_check != null) {
						// do nothing
						traces.writeInFileTransaction(folder, file, "trs_check != null do nothing for now ...");
					} else {

						traces.writeInFileTransaction(folder, file, "inserting into telec start ...");
						try {

							// insert into telec

							TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);

							long lidtelc = 0;

							if (n_tlc == null) {
								Integer idtelc = null;

								TelecollecteDto tlc = null;

								// insert into telec
								idtelc = telecollecteService.getMAX_ID();
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
								traces.writeInFileTransaction(folder, file, "n_tlc !null ");

								lidtelc = n_tlc.getTlc_numtlcolcte();
								double nbr_trs = n_tlc.getTlc_nbrtrans();

								nbr_trs = nbr_trs + 1;

								n_tlc.setTlc_nbrtrans(nbr_trs);

								telecollecteService.save(n_tlc);

							}

							// insert into transaction
							TransactionDto trs = new TransactionDto();
							trs.setTrs_numcmr(merchantid);
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

							trs.setTrs_numaut(authnumber);
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
							traces.writeInFileTransaction(folder, file, "inserting into telec ok");
							capture_status = "Y";

						} catch (Exception e) {
							exp_flag = 1;
							traces.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing" + e);
						}
					}
					if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
						capture_status.equalsIgnoreCase("N");

					traces.writeInFileTransaction(folder, file, "Automatic capture end.");
				}

			} else {

				traces.writeInFileTransaction(folder, file, "transaction declined !!! ");
				traces.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {

					traces.writeInFileTransaction(folder, file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtat_demande("SW_REJET");
					demandePaiementService.save(dmd);

				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file, "authorization 500"
							+ "Error during  DemandePaiement update SW_REJET for given orderid:[" + orderid + "]" + e);

					return getMsgError(jsonOrequest, "authorization 500 Error during  DemandePaiement update SW_REJET",
							tag20_resp);
				}

				traces.writeInFileTransaction(folder, file, "update Demandepaiement status to SW_REJET OK.");

			}

			traces.writeInFileTransaction(folder, file, "Generating paymentid...");

			String uuid_paymentid, paymentid = "";
			try {
				uuid_paymentid = String.format("%040d",
						new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
				paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file,
						"authorization 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + e);
				return getMsgError(jsonOrequest, "authorization 500 Error during  paymentid generation", tag20_resp);
			}

			traces.writeInFileTransaction(folder, file, "Generating paymentid OK");
			traces.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

			// JSONObject jso = new JSONObject();

			traces.writeInFileTransaction(folder, file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDem_pan();
			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file,
						"authorization 500 Error during authdata preparation orderid:[" + orderid + "]" + e);

				return getMsgError(jsonOrequest, "authorization 500 Error during authdata preparation", tag20_resp);
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
				traces.writeInFileTransaction(folder, file,
						"authorization 500 Error during jso out processing given authnumber:[" + authnumber + "]"
								+ jsouterr);
				return getMsgError(jsonOrequest, "authorization 500 Error during jso out processing", tag20_resp);
			}

			System.out.println("autorization api response frictionless :  [" + jso.toString() + "]");
			traces.writeInFileTransaction(folder, file,
					"autorization api response frictionless :  [" + jso.toString() + "]");
			// fin
			// *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// ********************* Cas chalenge responseMPI equal C ou D
			// *********************
			traces.writeInFileTransaction(folder, file, "****** Cas chalenge responseMPI equal C ou D ******");
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
				demandePaiementService.save(dmd);

				System.out.println("link_chalenge " + link_chalenge + dmd.getTokencommande());
				traces.writeInFileTransaction(folder, file, "link_chalenge " + link_chalenge + dmd.getTokencommande());

				System.out.println("autorization api response chalenge :  [" + jso.toString() + "]");
				traces.writeInFileTransaction(folder, file,
						"autorization api response chalenge :  [" + jso.toString() + "]");
			} catch (Exception ex) {
				traces.writeInFileTransaction(folder, file, "authorization 500 Error during jso out processing " + ex);

				return getMsgError(jsonOrequest, "authorization 500 Error during jso out processing ", null);
			}
		} else if (reponseMPI.equals("E")) {
			// ********************* Cas responseMPI equal E
			// *********************
			traces.writeInFileTransaction(folder, file, "****** Cas responseMPI equal E ******");
			traces.writeInFileTransaction(folder, file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
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
			// traces.writeInFileTransaction(folder, file, "link_fail " + link_fail +
			// dmd.getTokencommande());

			System.out.println("autorization api response fail :  [" + jso.toString() + "]");
			traces.writeInFileTransaction(folder, file, "autorization api response fail :  [" + jso.toString() + "]");
		} else {
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				traces.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("MPI_CMR_INEX");
				demandePaiementService.save(dmd);
				// externalContext.redirect("operationErreur.xhtml?Error=".concat("COMMERCANT
				// NON PARAMETRE"));
				return getMsgError(jsonOrequest, "COMMERCANT NON PARAMETRE", "15");
			case "BIN NON PARAMETRE":
				traces.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(jsonOrequest, "BIN NON PARAMETREE", "96");
			case "DIRECTORY SERVER":
				traces.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(jsonOrequest, "MPI_DS_ERR", "96");
			case "CARTE ERRONEE":
				traces.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(jsonOrequest, "CARTE ERRONEE", "96");
			case "CARTE NON ENROLEE":
				traces.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				return getMsgError(jsonOrequest, "CARTE NON ENROLLE", "96");
			}
		}
		traces.writeInFileTransaction(folder, file, "*********** Fin authorization() ************** ");
		System.out.println("*********** Fin authorization() ************** ");
		return jso.toString();

	}

	@PostMapping(value = "/napspayment/linkpayment", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String getLink(@RequestHeader MultiValueMap<String, String> header, @RequestBody String linkP,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_LINK_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start getLink() ************** ");
		System.out.println("*********** Start getLink() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		traces.writeInFileTransaction(folder, file, "getLink api call start ...");
		traces.writeInFileTransaction(folder, file, "getLink : [" + linkP + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(linkP);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "getLink 500 malformed json expression" + linkP + jserr);
			return getMsgError(null, "getLink 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file,
						"getLink 500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "getLink 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "getLink 500 malformed header" + head_err);
				return getMsgError(null, "getLink 500 malformed header", null);
			}
		}

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;

		String orderid, amount, merchantid, merchantname, websiteName, websiteid, recurring, country, phone, city,
				state, zipcode, address, expirydate, transactiondate, transactiontime, callbackUrl, fname, lname,
				email = "", successURL, failURL, idDemande;
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			amount = (String) jsonOrequest.get("amount");
			recurring = (String) jsonOrequest.get("recurring");

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
			traces.writeInFileTransaction(folder, file, "getLink 500 malformed json expression " + linkP + jerr);
			return getMsgError(null, "getLink 500 malformed json expression", null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "getLink 500 Merchant misconfigured in DB or not existing", "15");
		}

		DemandePaiementDto check_dmd = null;

		try {
			/*
			 * get demandePaiement par datetime avec cette format DateFormat dateFormat =
			 * new SimpleDateFormat("yyyy-MM-dd HH"); String dateSysStr =
			 * dateFormat.format(new Date()); System.out.println("dateSysStr : " +
			 * dateSysStr); dateSysStr = dateSysStr+"%"; check_dmd =
			 * demandePaiementService.findByCommandeAndComidAndDate(orderid, merchantid,
			 * dateSysStr);
			 */
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(jsonOrequest, "getLink 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null) {
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "getLink 500 Error Already exist in PaiementRequest", "16");
		}

		String url = "", status = "", statuscode = "";

		try {

			dmd = new DemandePaiementDto();

			dmd.setComid(merchantid);
			dmd.setCommande(orderid);
			dmd.setGalid(websiteid);
			dmd.setSuccessURL(successURL);
			dmd.setFailURL(failURL);
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

			dmdSaved = demandePaiementService.save(dmd);

			url = link_success + dmdSaved.getTokencommande();
			statuscode = "00";
			status = "OK";
			idDemande = String.valueOf(dmdSaved.getIddemande());

		} catch (Exception err1) {
			url = "";
			statuscode = "";
			status = "KO";
			idDemande = "";
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + err1);

			return getMsgError(jsonOrequest, "getLink 500 Error during DEMANDE_PAIEMENT insertion", null);
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

			traces.writeInFileTransaction(folder, file, "json : " + jso.toString());
			System.out.println("json : " + jso.toString());

		} catch (Exception err8) {
			traces.writeInFileTransaction(folder, file,
					"getLink 500 Error during jso out processing given orderid:[" + orderid + "]" + err8);

			return getMsgError(jsonOrequest, "getLink 500 Error during jso out processing", null);
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin getLink() ************** ");
		System.out.println("*********** Fin getLink() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/createtocken24", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String generateToken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String token24,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start generateToken() ************** ");
		System.out.println("*********** Start generateToken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		// BasicConfigurator.configure();
		traces.writeInFileTransaction(folder, file, "token24 api call start ...");
		traces.writeInFileTransaction(folder, file, "token24 : [" + token24 + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(token24);
		} catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "token24 500 malformed json expression" + token24 + jserr);
			return getMsgError(null, "token24 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

		// check the header
		try {

			traces.writeInFileTransaction(folder, file, "token24 api header check ...");

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
				traces.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "token24 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "token24 500 malformed header" + head_err);
				return getMsgError(null, "token24 500 malformed header", null);
			}
		}

		String cx_user, cx_password, cx_reason, error_msg, error_code, mac_value = "";
		try {
			// Merchant info
			cx_user = (String) jsonOrequest.get("cx_user");
			cx_password = (String) jsonOrequest.get("cx_password");
			cx_reason = (String) jsonOrequest.get("cx_reason");
			mac_value = (String) jsonOrequest.get("mac_value");

		} catch (Exception jerr) {
			traces.writeInFileTransaction(folder, file, "token24 500 malformed json expression" + token24 + jerr);
			return getMsgError(null, "token24 500 malformed json expression", null);
		}

		// pour tester la generation du tocken
		JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
		error_code = "";
		JSONObject jso = new JSONObject();
		String token = "";
		try {
			// token = jwtTokenUtil.generateToken(usernameToken, secret);
			token = jwtTokenUtil.generateToken(cx_user, cx_password);
			String userFromToken = jwtTokenUtil.getUsernameFromToken(token);
			Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token);
			Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token);

			System.out.println("token generated : " + token);
			traces.writeInFileTransaction(folder, file, "userFromToken generated : " + userFromToken);
			System.out.println("userFromToken generated : " + userFromToken);
			String dateSysStr = dateFormat.format(new Date());
			System.out.println("dateSysStr : " + dateSysStr);
			traces.writeInFileTransaction(folder, file, "dateSysStr : " + dateSysStr);
			System.out.println("dateExpiration : " + dateExpiration);
			traces.writeInFileTransaction(folder, file, "dateExpiration : " + dateExpiration);
			String dateExpirationStr = dateFormat.format(dateExpiration);
			System.out.println("dateExpirationStr : " + dateExpirationStr);
			traces.writeInFileTransaction(folder, file, "dateExpirationStr : " + dateExpirationStr);
			String condition = isTokenExpired == false ? "Non" : "OUI";
			System.out.println("token is expired : " + condition);
			traces.writeInFileTransaction(folder, file, "token is expired : " + condition);
			error_msg = "le token est généré avec succès";
			error_code = "00";
		} catch (Exception ex) {
			traces.writeInFileTransaction(folder, file, "echec lors de la génération du token");
			error_msg = "echec lors de la génération du token";
			error_code = "17";
		}

		jso.put("error_msg", error_msg);
		jso.put("error_code", error_code);
		jso.put("securtoken_24", token);
		jso.put("cx_user", cx_user);
		jso.put("mac_value", mac_value);

		System.out.println("response : " + jso.toString());
		traces.writeInFileTransaction(folder, file, "response : " + jso.toString());

		// fin
		System.out.println("*********** Fin generateToken() ************** ");
		traces.writeInFileTransaction(folder, file, "*********** Fin generateToken() ************** ");

		return jso.toString();
	}

	@RequestMapping(value = "/napspayment/chalenge/token/{token}", method = RequestMethod.GET)
	public String chalengeapi(@PathVariable(value = "token") String token, Model model) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start chalengeapi ************** ");
		System.out.println("*********** Start chalengeapi ************** ");

		String page = "chalenge";

		traces.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto current_dem = demandePaiementService.findByTokencommande(token);
		String msgRefus = "Une erreur est survenue, merci de réessayer plus tard";

		if (current_dem != null) {
			traces.writeInFileTransaction(folder, file, "current_dem is exist OK");
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
					traces.writeInFileTransaction(folder, file, "Le lien de chalence acs est null !!!");

					model.addAttribute("demandeDto", current_dem);
					page = "error";
				} else {
					System.out.println("current_dem htmlCreq : " + current_dem.getCreq());
					traces.writeInFileTransaction(folder, file, "current_dem htmlCreq : " + current_dem.getCreq());

					model.addAttribute("demandeDto", current_dem);
				}

			}
		} else {
			DemandePaiementDto demande = new DemandePaiementDto();
			msgRefus = "Votre commande est introuvable ";
			demande.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demande);
			traces.writeInFileTransaction(folder, file, "current_dem not found ");
			System.out.println("current_dem null ");
			page = "error";
		}

		System.out.println("return to " + page + ".html");

		traces.writeInFileTransaction(folder, file, "*********** Fin chalengeapi ************** ");
		System.out.println("*********** Fin chalengeapi ************** ");

		return page;
	}

	@PostMapping(value = "/napspayment/status", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String status(@RequestHeader MultiValueMap<String, String> header, @RequestBody String status,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start status() ************** ");
		System.out.println("*********** Start status() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		// BasicConfigurator.configure();
		traces.writeInFileTransaction(folder, file, "status api call start ...");
		traces.writeInFileTransaction(folder, file, "status : [" + status + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(status);
		} catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "status 500 malformed json expression" + status + jserr);
			return getMsgError(null, "status 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

		// check the header
		try {

			traces.writeInFileTransaction(folder, file, "status api header check ...");

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
				traces.writeInFileTransaction(folder, file, "500 malformed header " + header.toString() + head_err);
				return getMsgError(null, "status 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "status 500 malformed header" + head_err);
				return getMsgError(null, "status 500 malformed header", null);
			}
		}

		String orderid, authnumber, paymentid, amount, transactionid, merchantid = "";
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			authnumber = (String) jsonOrequest.get("authnumber");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");
			// Merchant info
			merchantid = (String) jsonOrequest.get("merchantid");

		} catch (Exception jerr) {
			traces.writeInFileTransaction(folder, file, "status 500 malformed json expression " + status + jerr);
			return getMsgError(null, "status 500 malformed json expression", null);
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		traces.writeInFileTransaction(folder, file, "status_" + orderid + timeStamp);

		String status_ = "Uknown";
		String statuscode_ = "06";

		DemandePaiementDto current_dmd = null;

		String dcurrent_dmd, dtpattern, tmpattern, respcode, s_respcode = "";
		SimpleDateFormat sfdt, sftm = null;
		Date datdem, datetlc = null;
		Character E = '\0';

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"status 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);
			return getMsgError(jsonOrequest, "status 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			traces.writeInFileTransaction(folder, file, "status 500 PaiementRequest not found orderidderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			return getMsgError(jsonOrequest, "status 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		if (authnumber.length() < 1) {
			try {

				// get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmr(orderid, merchantid);

			} catch (Exception err2) {
				traces.writeInFileTransaction(folder, file,
						"status 500 Error during HistoAutoGate findByNumAuthAndNumCommercant orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err2);
				return getMsgError(jsonOrequest, "status 500 Error during find HistoAutoGate", null);
			}
		} else {

			try {
				// get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
						merchantid);

			} catch (Exception err2) {
				traces.writeInFileTransaction(folder, file,
						"Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:[" + orderid
								+ "] + and authnumber:[" + authnumber + "]" + "and merchantid:[" + merchantid + "]"
								+ err2);
				return getMsgError(jsonOrequest, "status 500 Error during find HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			String dmd_etat = "";
			if (current_dmd.getEtat_demande() != null) {
				dmd_etat = current_dmd.getEtat_demande();
			}
			if (dmd_etat.equalsIgnoreCase("PAYE")) {
				traces.writeInFileTransaction(folder, file,
						"Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
								+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
								+ merchantid + "]");
				return getMsgError(jsonOrequest, "status 500 Inconsitence HistoAutoGate not found", null);
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
			traces.writeInFileTransaction(folder, file, "status 500 Error during status processing for given authnumber"
					+ " :[" + authnumber + "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(jsonOrequest, "status 500 Error during status processing", null);
		}

		JSONObject jso = new JSONObject();
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
				traces.writeInFileTransaction(folder, file,
						"status 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber" + " :["
								+ authnumber + "] and merchantid:[" + merchantid + "]" + err4);

				return getMsgError(jsonOrequest, "status 500 Error during Transaction", null);
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

		} catch (Exception err3) {
			err3.printStackTrace();
			traces.writeInFileTransaction(folder, file,
					"status 500 Error during jso out processing for given authnumber " + " :[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(jsonOrequest, "status 500 Error during jso out processing", null);
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin status() ************** ");
		System.out.println("*********** Fin status() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/capture", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String capture(@RequestHeader MultiValueMap<String, String> header, @RequestBody String capture,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start capture() ************** ");
		System.out.println("*********** Start capture() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		traces.writeInFileTransaction(folder, file, "capture api call start ...");
		traces.writeInFileTransaction(folder, file, "capture : [" + capture + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(capture);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "capture 500 malformed json expression" + capture + jserr);
			return getMsgError(null, "capture 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file,
						"capture 500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "capture 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "capture 500 malformed header " + head_err);
				return getMsgError(null, "capture 500 malformed header", null);
			}
		}

		// String sheader = header.toString();
		// Transaction info

		String orderid, paymentid, amount, authnumber, transactionid, merchantid, merchantname, websiteName, websiteid,
				callbackUrl, cardnumber, fname, lname, email = "";
		try {
			orderid = (String) jsonOrequest.get("orderid");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			authnumber = (String) jsonOrequest.get("authnumber");
			transactionid = (String) jsonOrequest.get("transactionid");

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
			traces.writeInFileTransaction(folder, file, "capture 500 malformed json expression " + capture + jerr);
			return getMsgError(null, "capture 500 malformed json expression", null);
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		traces.writeInFileTransaction(folder, file, "capture_" + orderid + timeStamp);
		// get demandepaiement id , check if exist

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);
			return getMsgError(jsonOrequest, "capture 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			traces.writeInFileTransaction(folder, file, "captue 500 PaiementRequest not found for orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			return getMsgError(jsonOrequest, "captue 500 PaiementRequest not found", null);
		}

		// get histoauto check if exist

		HistoAutoGateDto current_hist = null;

		try {

			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err2);
			return getMsgError(jsonOrequest, "capture 500 Error during HistoAutoGate", null);
		}

		if (current_hist == null) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "capture 500 Inconsitence HistoAutoGate not found", null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(jsonOrequest, "capture 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "capture 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "capture 500 Merchant misconfigured in DB or not existing", "15");
		}

		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		// check if already telecollected

		TransactionDto trs_check = null;

		try {

			trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);

		} catch (Exception err4) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber:["
							+ authnumber + "] and merchantid:[" + merchantid + "]" + err4);

			return getMsgError(jsonOrequest, "capture 500 Error during Transaction", null);
		}

		if (trs_check != null) {
			traces.writeInFileTransaction(folder, file, "capture 500 Transaction already captured  for given "
					+ "authnumber:[" + authnumber + "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "500 Transaction already captured", null);
		}

		TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);
		Date current_date = null;
		current_date = new Date();
		long lidtelc = 0;

		if (n_tlc == null) {
			Integer idtelc = null;

			TelecollecteDto tlc = null;

			try {
				// insert into telec
				// idtelc = tlcservice.getMAX_ID("TELECOLLECTE", "TLC_NUMTLCOLCTE");
				idtelc = telecollecteService.getMAX_ID();
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
				traces.writeInFileTransaction(folder, file,
						"capture 500 Error during insert into telec for given authnumber:[" + authnumber
								+ "] and merchantid:[" + merchantid + "]" + err5);

				return getMsgError(jsonOrequest, "capture 500 Error during insert into telecollecte", null);
			}
		} else {

			lidtelc = n_tlc.getTlc_numtlcolcte();
			double nbr_trs = n_tlc.getTlc_nbrtrans();

			nbr_trs = nbr_trs + 1;

			n_tlc.setTlc_nbrtrans(nbr_trs);

			try {

				telecollecteService.save(n_tlc);
			} catch (Exception err55) {
				traces.writeInFileTransaction(folder, file,
						"capture 500 Error during update telec for given authnumber:[" + authnumber
								+ "] and merchantid:[" + merchantid + "]" + err55);

				return getMsgError(jsonOrequest, "capture 500 Error during update telecollecte", null);
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
			trs.setTrs_numcmr(merchantid);
			trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));
			frmt_cardnumber = Util.formatagePan(cardnumber);
			trs.setTrs_codporteur(frmt_cardnumber);
			dmnt = Double.parseDouble(amount);
			trs.setTrs_montant(dmnt);
			// trs.setTrs_dattrans(new Date());
			current_date = new Date();
			Date current_date_1 = getDateWithoutTime(current_date);
			trs.setTrs_dattrans(current_date_1);
			trs.setTrs_numaut(authnumber);
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
			traces.writeInFileTransaction(folder, file,
					"capture 500 Error during insert into transaction for given authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err6);

			return getMsgError(jsonOrequest, "capture 500 Error during insert into transaction", null);
		}

		try {
			current_hist.setHatEtat('T');
			current_hist.setHatdatetlc(current_date);
			current_hist.setOperateurtlc("mxplusapi");
			histoAutoGateService.save(current_hist);

		} catch (Exception err7) {
			traces.writeInFileTransaction(folder, file,
					"capture 500 Error during histoauto_gate update for given authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err7);

			return getMsgError(jsonOrequest, "capture 500 Error during histoauto_gate update", null);
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
			traces.writeInFileTransaction(folder, file,
					"capture 500 Error during jso data preparationfor given authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]" + err8);

			return getMsgError(jsonOrequest, "capture 500 Error during jso data preparation", null);
		}

		JSONObject jso = new JSONObject();

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
		} catch (Exception err9) {
			traces.writeInFileTransaction(folder, file, "capture 500 Error during jso out processing given "
					+ "authnumber:[" + authnumber + "] and merchantid:[" + merchantid + "]" + err9);

			return getMsgError(jsonOrequest, "capture 500 Error during jso out processing", null);
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin capture() ************** ");
		System.out.println("*********** Fin capture() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/refund", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String refund(@RequestHeader MultiValueMap<String, String> header, @RequestBody String refund,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start refund() ************** ");
		System.out.println("*********** Start refund() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		traces.writeInFileTransaction(folder, file, "refund api call start ...");
		traces.writeInFileTransaction(folder, file, "refund : [" + refund + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(refund);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "refund 500 malformed json expression" + refund + jserr);
			return getMsgError(null, "refund 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file,
						"refund 500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "refund 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "refund 500 malformed header" + head_err);
				return getMsgError(null, "refund 500 malformed header", null);
			}
		}

		String orderid, authnumber, paymentid, amount, transactionid, merchantid, merchantname, websiteName, websiteid,
				callbackUrl, cardnumber, fname, lname, email = "";
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			authnumber = (String) jsonOrequest.get("authnumber");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");

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
			traces.writeInFileTransaction(folder, file, "refund 500 malformed json expression " + refund + jerr);
			return getMsgError(null, "refund 500 malformed json expression", null);
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		traces.writeInFileTransaction(folder, file, "refund_" + orderid + timeStamp);

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);
			return getMsgError(jsonOrequest, "refund 500 Error during PaiementRequest", null);
		}
		if (current_dmd == null) {
			traces.writeInFileTransaction(folder, file, "refund 500 PaiementRequest not found for given orderid"
					+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		try {
			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(jsonOrequest, "refund 500 Error during find HistoAutoGate", null);
		}

		if (current_hist == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Inconsitence HistoAutoGate not found", null);
		}

		TransactionDto trs_check = null;

		try {

			trs_check = transactionService.findByTrsnumautAndTrsnumcmr(authnumber, merchantid);

		} catch (Exception err4) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber:["
							+ authnumber + "] and merchantid:[" + merchantid + "]" + err4);

			return getMsgError(jsonOrequest, "refund 500 Error during Transaction", null);
		}

		if (trs_check == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction not found for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Inconsitence Captured Transaction not found", null);
		}

		String trs_procod = trs_check.getTrs_procod();
		String trs_state = trs_check.getTrs_etat();

		if (trs_procod == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_procod null for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction trs_procod null  for authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Inconsitence Captured Transaction trs_procod null", null);
		}

		if (trs_state == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_procod null for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction trs_state null for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Inconsitence Captured Transaction trs_procod null", null);
		}

		if (!trs_procod.equalsIgnoreCase("0")) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_procod <> 0 for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction trs_procod <> 0   for authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Inconsitence Captured Transaction trs_procod <> 0", null);
		}

		if (!trs_state.equalsIgnoreCase("E")) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Inconsitence Captured Transaction trs_state <> E for authnumber and DemandePaiement is PAYE status"
							+ "Captured Transaction  trs_state <> E  for authnumber:[" + authnumber
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Inconsitence Captured Transaction trs_state <> E", null);
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
			traces.writeInFileTransaction(folder, file, "refund 500 Error during date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(jsonOrequest, "refund 500 Error during date formatting", null);
		}

		String[] mm;
		String[] m;
		String montanttrame = "";

		try {
			montanttrame = "";

			mm = new String[2];

			System.out.println("montant v0 : " + amount);
			traces.writeInFileTransaction(folder, file, "montant v0 : " + amount);

			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			System.out.println("montant v1 : " + amount);
			traces.writeInFileTransaction(folder, file, "montant v1 : " + amount);

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
			System.out.println("montanttrame : " + montanttrame);
			traces.writeInFileTransaction(folder, file, "montanttrame : " + montanttrame);
		} catch (Exception err4) {
			traces.writeInFileTransaction(folder, file, "refund 500 Error during amount formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err4);

			return getMsgError(jsonOrequest, "refund 500 Error during amount formatting", null);
		}

		traces.writeInFileTransaction(folder, file, "Switch processing start ...");

		String tlv = "";
		traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]" + e);

			return getMsgError(jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "refund 500 Merchant misconfigured in DB or not existing", "15");
		}

		// offline processing

		String tag20_resp = "00"; // Accept all refund offline mode
		String s_status = "Refunded offline";

		traces.writeInFileTransaction(folder, file, "Switch status : [" + s_status + "]");

		if (tag20_resp.equalsIgnoreCase("00"))

		{
			traces.writeInFileTransaction(folder, file, "Switch CODE REP : [00]");

			traces.writeInFileTransaction(folder, file, "Transaction refunded.");

			try {
				traces.writeInFileTransaction(folder, file, "Setting DemandePaiement status A ...");

				current_dmd.setEtat_demande("R");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file,
						"refund 500 Error during  demandepaiement update  A for given  orderid:[" + orderid + "]" + e);

				return getMsgError(jsonOrequest, "refund 500 Error during  demandepaiement update A", null);
			}

			traces.writeInFileTransaction(folder, file, "Setting DemandePaiement status OK.");

			traces.writeInFileTransaction(folder, file, "inserting HistoAutoGate   ...");

			try {

				// Telecollecte n_tlc = tlcservice.getMAXTLC_NRefund(merchantid);
				TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(merchantid);
				Date current_date = null;
				current_date = new Date();
				long lidtelc = 0;

				if (n_tlc == null) {
					Integer idtelc = null;

					TelecollecteDto tlc = null;

					// insert into telec
					idtelc = telecollecteService.getMAX_ID();
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
				trs.setTrs_numcmr(merchantid);
				trs.setTrs_numtlcolcte(Double.valueOf(lidtelc));
				frmt_cardnumber = Util.formatagePan(cardnumber);
				trs.setTrs_codporteur(frmt_cardnumber);
				dmnt = Double.parseDouble(amount);
				trs.setTrs_montant(dmnt);
				// trs.setTrs_dattrans(new Date());
				current_date = new Date();
				Date current_date_1 = getDateWithoutTime(current_date);
				trs.setTrs_dattrans(current_date_1);
				trs.setTrs_numaut("000000"); // trs.setTrs_numaut(authnumber);
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
				traces.writeInFileTransaction(folder, file,
						"refund 500 Error during  HistoAutoGate insertion or Transaction insertion A for given orderid:["
								+ orderid + "]" + e);

				return getMsgError(jsonOrequest,
						"refund 500 Error during  HistoAutoGate insertion or Transaction insertion A", null);
			}

			traces.writeInFileTransaction(folder, file, "inserting HistoAutoGate  OK.");

		} /*
			 * else { //offline processing
			 * 
			 * }
			 */

		JSONObject jso = new JSONObject();

		String refund_id = "";

		try {
			refund_id = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));

		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"refund 500 Error during  refund_id generation for given orderid:[" + orderid + "]" + e);

			return getMsgError(jsonOrequest, "refund 500 Error during  refund_id generation", null);
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

		} catch (Exception err8) {
			traces.writeInFileTransaction(folder, file, "refund 500 Error during jso out processing given authnumber"
					+ "authnumber:[" + authnumber + "]" + err8);

			return getMsgError(jsonOrequest, "refund 500 Error during jso out processing", null);
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin refund() ************** ");
		System.out.println("*********** Fin refund() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/reversal", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String reversal(@RequestHeader MultiValueMap<String, String> header, @RequestBody String reversal,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start reversal() ************** ");
		System.out.println("*********** Start reversal() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		traces.writeInFileTransaction(folder, file, "reversal api call start ...");
		traces.writeInFileTransaction(folder, file, "reversal : [" + reversal + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(reversal);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "reversal 500 malformed json expression" + reversal + jserr);
			return getMsgError(null, "reversal 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "reversal 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "reversal 500 malformed header" + head_err);
			}
		}

		String orderid, authnumber, paymentid, amount, transactionid, merchantid, merchantname, websiteName, websiteid,
				callbackUrl, cardnumber, fname, lname, email = "";

		try {
			// Reversal info
			orderid = (String) jsonOrequest.get("orderid");
			authnumber = (String) jsonOrequest.get("authnumber");
			paymentid = (String) jsonOrequest.get("paymentid");
			amount = (String) jsonOrequest.get("amount");
			transactionid = (String) jsonOrequest.get("transactionid");

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
			traces.writeInFileTransaction(folder, file, "reversal 500 malformed json expression " + reversal + jerr);
			return getMsgError(null, "reversal 500 malformed json expression", null);
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		traces.writeInFileTransaction(folder, file, "reversal_" + orderid + timeStamp);
		// get demandepaiement id , check if exist

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(jsonOrequest, "reversal 500 Error during PaiementRequest", null);

		}
		if (current_dmd == null) {
			traces.writeInFileTransaction(folder, file, "reversal 500 PaiementRequest not found for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "reversal 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		try {

			// get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(orderid, authnumber,
					merchantid);

		} catch (Exception err2) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err2);

			return getMsgError(jsonOrequest, "reversal 500 Error during HistoAutoGate", null);
		}

		if (current_hist == null) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Inconsitence HistoAutoGate not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + authnumber + "] and merchantid:["
							+ merchantid + "]");

			return getMsgError(jsonOrequest, "reversal 500 Inconsitence HistoAutoGate not found", null);
		}

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";

		} catch (Exception err3) {
			traces.writeInFileTransaction(folder, file, "reversal 500 Error during date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err3);

			return getMsgError(jsonOrequest, "reversal 500 Error during date formatting", null);
		}

		String[] mm;
		String[] m;
		String montanttrame = "";

		try {
			montanttrame = "";

			mm = new String[2];
			System.out.println("montant v0 : " + amount);
			traces.writeInFileTransaction(folder, file, "montant v0 : " + amount);

			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			System.out.println("montant v1 : " + amount);
			traces.writeInFileTransaction(folder, file, "montant v1 : " + amount);

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
			System.out.println("montanttrame : " + montanttrame);
			traces.writeInFileTransaction(folder, file, "montanttrame : " + montanttrame);
		} catch (Exception err4) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Error during amount formatting for given orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]" + err4);

			return getMsgError(jsonOrequest, "reversal 500 Error during amount formatting", null);
		}

		traces.writeInFileTransaction(folder, file, "Switch processing start ...");

		String tlv = "";
		traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

		// controls
		String mesg_type = "2";
		String merchant_name = merchantname;
		String acq_type = "0000";
		String processing_code = "0";
		String reason_code = "H";
		String transaction_condition = "6";
		String transactionnumber = authnumber;

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(jsonOrequest, "reversal 500 Merchant misconfigured in DB or not existing", "15");
		}

		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();
		merchant_name = Util.pad_merchant(merchantname, 19, ' ');
		String merchant_city = "MOROCCO        ";
		acq_type = "0000";
		processing_code = "0";
		reason_code = "H";
		transaction_condition = "6";

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

			traces.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
			traces.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
			traces.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
			traces.writeInFileTransaction(folder, file, "tag22_request : [" + transaction_condition + "]");
			traces.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
			traces.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
			traces.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
			traces.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
			traces.writeInFileTransaction(folder, file, "tag18_request : [761454]");
			traces.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
			traces.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
			traces.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
			traces.writeInFileTransaction(folder, file, "tag10_request : [" + merc_codeactivite + "]");
			traces.writeInFileTransaction(folder, file, "tag8_request : [0+" + merchantid + "]");
			traces.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
			traces.writeInFileTransaction(folder, file, "tag66_request : [" + transactionnumber + "]");
			traces.writeInFileTransaction(folder, file, "tag11_request : [" + merchant_name + "]");
			traces.writeInFileTransaction(folder, file, "tag12_request : [" + merchant_city + "]");
			traces.writeInFileTransaction(folder, file, "tag13_request : [MAR]");
			traces.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
			traces.writeInFileTransaction(folder, file, "tag19_request : [" + authnumber + "]");

		} catch (Exception err4) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Error during switch tlv buildu for given orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]" + err4);

			return getMsgError(jsonOrequest, "reversa 500 Error during switch tlv buildu", "96");
		}

		traces.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

		traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

		traces.writeInFileTransaction(folder, file, "Switch Connecting ...");

		String resp_tlv = "";
		SwitchTCPClient sw = SwitchTCPClient.getInstance();

		int port = 0;
		String sw_s = "", s_port = "";
		try {

			s_port = portSwitch;
			sw_s = ipSwitch;

			traces.writeInFileTransaction(folder, file, "Switch IP / Switch PORT : " + sw_s + "/" + s_port);

			port = Integer.parseInt(s_port);

			boolean s_conn = sw.startConnection(sw_s, port);
			traces.writeInFileTransaction(folder, file, "Switch Connecting ...");

			if (s_conn) {
				traces.writeInFileTransaction(folder, file, "Switch Connected.");
				traces.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

				resp_tlv = sw.sendMessage(tlv);

				traces.writeInFileTransaction(folder, file, "Switch TLV Request end.");
				sw.stopConnection();

			} else {

				traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

				return getMsgError(jsonOrequest, "reversal 500 Error Switch communication s_conn false", "96");
			}

		} catch (SocketTimeoutException e) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(jsonOrequest, "reversal 500 Error Switch communication SocketTimeoutException", "96");
		} catch (UnknownHostException e) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(jsonOrequest, "reversal 500 Error Switch communication UnknownHostException", "96");
		}

		catch (IOException e) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);

			return getMsgError(jsonOrequest, "reversal 500 Error Switch communication IOException", "96");
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);

			return getMsgError(jsonOrequest, "reversal 500 Error Switch communication General Exception switch", "96");
		}

		String resp = resp_tlv;
		if (resp == null) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(jsonOrequest, "reversal 500 Error Switch null response switch", "96");
		}

		if (resp.length() < 3)

		{

			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(jsonOrequest, "reversal 500 Error Switch short response length() < 3 switch", "96");
		}

		traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

		traces.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

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
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);

			return getMsgError(jsonOrequest, "reversal 500 Error during tlv Switch response parse switch", "96");
		}

		// controle switch
		if (tag1_resp == null) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(jsonOrequest, "reversal 500 Error during tlv Switch response parse tag1_resp tag null",
					"96");
		}

		if (tag1_resp.length() < 3) {
			traces.writeInFileTransaction(folder, file, "Switch  malfunction !!!");

			return getMsgError(jsonOrequest,
					"reversal 500 Error during tlv Switch response parse tag1_resp length tag  < 3 switch", "96");
		}

		traces.writeInFileTransaction(folder, file, "Switch TLV Respnose Processed");
		traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

		traces.writeInFileTransaction(folder, file, "tag0_resp : [" + tag0_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag1_resp : [" + tag1_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag3_resp : [" + tag3_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag8_resp : [" + tag8_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag9_resp : [" + tag9_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag14_resp : [" + tag14_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag15_resp : [" + tag15_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag16_resp : [" + tag16_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag17_resp : [" + tag17_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag66_resp : [" + tag66_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag18_resp : [" + tag18_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag19_resp : [" + tag19_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag23_resp : [" + tag23_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag20_resp : [" + tag20_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag21_resp : [" + tag21_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag22_resp : [" + tag22_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag80_resp : [" + tag80_resp + "]");
		traces.writeInFileTransaction(folder, file, "tag98_resp : [" + tag98_resp + "]");

		if (tag20_resp == null) {
			return getMsgError(jsonOrequest, "reversal 500 Switch malfunction response code not present", "96");
		}
		if (tag20_resp.length() < 1) {
			return getMsgError(jsonOrequest, "reversal 500 Switch malfunction response code length incorrect", "96");
		}

		if (tag20_resp.equalsIgnoreCase("00"))

		{
			traces.writeInFileTransaction(folder, file, "Switch CODE REP : [00]");

			traces.writeInFileTransaction(folder, file, "Transaction reversed.");

			try {
				traces.writeInFileTransaction(folder, file, "Setting DemandePaiement status A ...");

				current_dmd.setEtat_demande("A");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				traces.writeInFileTransaction(folder, file,
						"reversal 500 Error during  demandepaiement update  A for given orderid:[" + orderid + "]" + e);

				return getMsgError(jsonOrequest, "reversal 500 Error during  demandepaiement update A", tag20_resp);
			}

			traces.writeInFileTransaction(folder, file, "Setting DemandePaiement status OK.");

			traces.writeInFileTransaction(folder, file, "Setting HistoAutoGate status A ...");

			try {
				current_hist.setHatEtat('A');
				histoAutoGateService.save(current_hist);
			} catch (Exception e) {

				e.printStackTrace();
				traces.writeInFileTransaction(folder, file,
						"reversal 500 Error during  HistoAutoGate update  A for given orderid:[" + orderid + "]" + e);

				return getMsgError(jsonOrequest, "reversal 500 Error during  HistoAutoGate update A", tag20_resp);
			}

			traces.writeInFileTransaction(folder, file, "Setting HistoAutoGate status OK.");

		} else {

			traces.writeInFileTransaction(folder, file, "Transaction reversal declined.");
			traces.writeInFileTransaction(folder, file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		String s_status = "";

		traces.writeInFileTransaction(folder, file, "Switch status : [" + s_status + "]");

		JSONObject jso = new JSONObject();

		traces.writeInFileTransaction(folder, file, "Generating reversalid");

		String uuid_reversalid, reversalid = "";
		try {

			uuid_reversalid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));

			reversalid = uuid_reversalid.substring(uuid_reversalid.length() - 22);
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Error during  reversalid generation for given orderid:[" + orderid + "]" + e);

			return getMsgError(jsonOrequest, "reversal 500 Error during  reversalid generation", tag20_resp);
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
		} catch (Exception err8) {
			traces.writeInFileTransaction(folder, file,
					"reversal 500 Error during jso out processing given authnumber:[" + authnumber + "]" + err8);

			return getMsgError(jsonOrequest, "reversal 500 Error during jso out processing", tag20_resp);
		}
		traces.writeInFileTransaction(folder, file, "*********** Fin reversal() ************** ");
		System.out.println("*********** Fin reversal() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/cardtoken", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String getCardTken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cardtoken,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start getCardTken() ************** ");
		System.out.println("*********** Start getCardTken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		traces.writeInFileTransaction(folder, file, "cardtoken api call start ...");
		traces.writeInFileTransaction(folder, file, "cardtoken : [" + cardtoken + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(cardtoken);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "cardtoken 500 malformed json expression" + cardtoken + jserr);
			return getMsgError(null, "cardtoken 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "cardtoken 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "cardtoken 500 malformed header" + head_err);
			}
		}

		String merchantid, merchantname, websiteName, websiteid, cardnumber, expirydate, holdername, cvv, fname, lname,
				email = "";

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
			cvv = (String) jsonOrequest.get("cvv");

			// Client info
			fname = (String) jsonOrequest.get("fname");
			lname = (String) jsonOrequest.get("lname");
			email = (String) jsonOrequest.get("email");
		} catch (Exception jerr) {
			traces.writeInFileTransaction(folder, file, "cardtoken 500 malformed json expression " + cardtoken + jerr);
			return getMsgError(null, "cardtoken 500 malformed json expression", null);
		}

		JSONObject jso = new JSONObject();
		try {
			// insert new cardToken
			CardtokenDto cardtokenDto = new CardtokenDto();

			String tokencard = Util.generateCardToken(merchantid);

			cardtokenDto.setToken(tokencard);
			System.out.println("cardtokenDto token : " + cardtokenDto.getToken());
			traces.writeInFileTransaction(folder, file, "cardtokenDto token : " + cardtokenDto.getToken());

			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();
			String dateTokenStr = dateFormat.format(dateToken);
			System.out.println("cardtokenDto dateTokenStr : " + dateTokenStr);
			traces.writeInFileTransaction(folder, file, "cardtokenDto dateTokenStr : " + dateTokenStr);
			Date dateTokenFormated = dateFormat.parse(dateTokenStr);
			System.out.println("cardtokenDto dateTokenFormated : " + dateTokenFormated);
			traces.writeInFileTransaction(folder, file, "cardtokenDto dateTokenFormated : " + dateTokenFormated);
			cardtokenDto.setTokenDate(dateTokenFormated);
			cardtokenDto.setCardNumber(cardnumber);
			cardtokenDto.setIdMerchant(merchantid);
			cardtokenDto.setFirst_name(fname);
			cardtokenDto.setLast_name(lname);

			System.out.println("cardtokenDto expirydate : " + expirydate);
			traces.writeInFileTransaction(folder, file, "Insert into table CARDTOKEN OK");

			cardtokenDto.setExprDate(expirydate);
			cardtokenDto.setHolderName(holdername);
			cardtokenDto.setMcc(merchantid);

			CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

			// Card info
			jso.put("token", cardtokenSaved.getToken());

			// Transaction info
			jso.put("statuscode", "00");
			jso.put("status", "saving token successfully");

			traces.writeInFileTransaction(folder, file, "Insert into table CARDTOKEN OK");

		} catch (Exception ex) {
			traces.writeInFileTransaction(folder, file, "Error during CARDTOKEN Saving : " + ex);
			// Card info
			jso.put("token", "");

			// Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "saving token failed ");
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin getCardTken() ************** ");
		System.out.println("*********** Fin getCardTken() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/deleteCardtoken", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String deleteCardTken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cardtoken,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start deleteCardTken() ************** ");
		System.out.println("*********** Start deleteCardTken() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		traces.writeInFileTransaction(folder, file, "deleteCardTken api call start ...");
		traces.writeInFileTransaction(folder, file, "deleteCardTken : [" + cardtoken + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(cardtoken);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file,
					"deleteCardTken 500 malformed json expression " + cardtoken + jserr);
			return getMsgError(null, "deleteCardTken 500 malformed json expression", null);
		}

		if (header != null)
			traces.writeInFileTransaction(folder, file, "header : [" + header.toString() + "]");
		else
			traces.writeInFileTransaction(folder, file, "error header is null !");

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
				traces.writeInFileTransaction(folder, file, "500 malformed header" + header.toString() + head_err);
				return getMsgError(null, "deleteCardTken 500 malformed header", null);
			} else {
				traces.writeInFileTransaction(folder, file, "deleteCardTken 500 malformed header" + head_err);
			}
		}

		String merchantid, cardnumber, token;
		try {
			// Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");

			// Card info
			cardnumber = (String) jsonOrequest.get("cardnumber");
			token = (String) jsonOrequest.get("token");

			// Client info
		} catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file,
					"deleteCardTken 500 malformed json expression" + cardtoken + jserr);
			return getMsgError(null, "deleteCardTken 500 malformed json expression", null);
		}

		JSONObject jso = new JSONObject();
		try {
			// delete cardToken

			CardtokenDto cardTokenTodelete = cardtokenService.findByIdMerchantAndToken(merchantid, cardnumber);

			cardtokenService.delete(cardTokenTodelete);

			// Card info
			jso.put("token", token);

			// Transaction info
			jso.put("statuscode", "00");
			jso.put("status", "delete token successfully ");

			traces.writeInFileTransaction(folder, file, "Delete CARDTOKEN OK");

		} catch (Exception ex) {
			traces.writeInFileTransaction(folder, file, "Error during delete token : " + ex);
			// Card info
			jso.put("token", token);

			// Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "delete token failed");
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin deleteCardTken() ************** ");
		System.out.println("*********** Fin deleteCardTken() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/histo/exportexcel", consumes = "application/json", produces = "application/json")
	public void exportToExcel(@RequestHeader MultiValueMap<String, String> header, @RequestBody String req,
			HttpServletResponse response) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start exportToExcel() ************** ");
		System.out.println("*********** Start exportToExcel() ************** ");

		traces.writeInFileTransaction(folder, file, "exportToExcel api call start ...");
		traces.writeInFileTransaction(folder, file, "exportToExcel : [" + req + "]");

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
			traces.writeInFileTransaction(folder, file, "exportToExcel 500 malformed json expression " + req + jserr);
		}

		String merchantid = "", orderid = "", dateDem = "";
		try {
			// Transaction info
			merchantid = (String) jsonOrequest.get("merchantid");

			orderid = (String) jsonOrequest.get("orderid");
			dateDem = (String) jsonOrequest.get("dateDem");

		} catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "exportToExcel 500 malformed json expression" + req + jserr);
		}

		try {

			// List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findAll();
			List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findByHatNumcmr(merchantid);

			GenerateExcel excelExporter = new GenerateExcel(listHistoGate);

			excelExporter.export(response);

		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file, "exportToExcel 500 merchantid:[" + merchantid + "]");
			traces.writeInFileTransaction(folder, file, "exportToExcel 500 exception" + e);
			e.printStackTrace();
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin exportToExcel ***********");
		System.out.println("*********** Fin exportToExcel ***********");
	}

	@RequestMapping(path = "/napspayment/cpautorisation", produces = "application/json; charset=UTF-8")
	public ResponseEntity<responseDto> cpautorisation(@RequestBody RequestDto requestDto) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start cpautorisation() ************** ");
		System.out.println("*********** Start cpautorisation() ************** ");

		System.out.println("*********** Start cpautorisation ************** ");
		System.out.println("requestDto commerçant recupérée : " + requestDto.getMerchantid());
		System.out.println("requestDto Commande recupérée : " + requestDto.getOrderid());
		System.out.println("requestDto montant recupérée : " + requestDto.getAmount());

		responseDto response = new responseDto();

		traces.writeInFileTransaction(folder, file, "*********** Fin cpautorisation() ************** ");
		System.out.println("*********** Fin cpautorisation() ************** ");

		return ResponseEntity.ok().body(response);
	}

	@PostMapping(value = "/napspayment/testapi", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String testapi(@RequestHeader MultiValueMap<String, String> header, @RequestBody String req,
			HttpServletResponse response, Model model) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "API_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "*********** Start testapi ************** ");
		System.out.println("*********** Start testapi ************** ");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(req);
		}

		catch (JSONException jserr) {
			traces.writeInFileTransaction(folder, file, "testapi 500 malformed json expression" + req + jserr);
			return getMsgError(null, "testapi 500 malformed json expression", null);
		}

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", successURL, failURL,
				transactiontype;

		JSONObject jso = new JSONObject();

		try {
			DemandePaiementDto dmd = new DemandePaiementDto();

			dmd.setMsgRefus("testapi");
			dmd.setTokencommande("6B3FZ2015425468441HH67V1210500");
			// String htmlCreq = "<form action='https://acs2.bankofafrica.ma:443/lacs2'
			// method='post'enctype='application/x-www-form-urlencoded'><input
			// type='hidden'name='creq'value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjllZjUwNjk3LWRiMTctNGZmMy04MDYzLTc0ZTAwMTk0N2I4YiIsCiAgImFjc1RyYW5zSUQiOiAiZjM2ZDA3ZWQtZGJhOS00ZTkzLWE2OGMtMzNmYjAyMDgxZDVmIiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0='/></form>";
			// dmd.setCreq(htmlCreq);

			model.addAttribute("demandeDto", dmd);

			String linkacs = link_chalenge + dmd.getTokencommande();

			// Transaction info
			jso.put("orderid", "12345678912546");
			jso.put("amount", "100");
			jso.put("transactionid", "1122554466");
			jso.put("statuscode", "00");
			jso.put("status", "OK");

			// Link ACS chalenge info
			jso.put("linkacs", linkacs);

			traces.writeInFileTransaction(folder, file, "testapi json : " + jso.toString());
			System.out.println("testapi json : " + jso.toString());

			// response.sendRedirect(link_index);
			response.sendRedirect(linkacs);

		} catch (Exception ex) {
			// Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "failed");
			System.out.println("testapi error json : " + jso.toString());
			traces.writeInFileTransaction(folder, file, "testapi error json : " + jso.toString());
			traces.writeInFileTransaction(folder, file, "testapi exception : " + ex);
			System.out.println("testapi exception : " + ex);
		}

		traces.writeInFileTransaction(folder, file, "*********** Fin testapi ************** ");
		System.out.println("*********** Fin testapi ************** ");
		return jso.toString();
	}

	public String getMsgError(JSONObject jsonOrequest, String msg, String coderep) {
		traces.writeInFileTransaction(folder, file, "*********** Start getMsgError() ************** ");
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

		traces.writeInFileTransaction(folder, file, "json : " + jso.toString());
		System.out.println("json : " + jso.toString());

		traces.writeInFileTransaction(folder, file, "*********** Fin getMsgError() ************** ");
		System.out.println("*********** Fin getMsgError() ************** ");
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

}
