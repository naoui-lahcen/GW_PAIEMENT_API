package ma.m2m.gateway.controller;

import java.io.IOException;
import java.math.BigInteger;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.m2m.gateway.dto.*;
import ma.m2m.gateway.model.Emetteur;
import ma.m2m.gateway.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.config.JwtTokenUtil;
import ma.m2m.gateway.reporting.GenerateExcel;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
import ma.m2m.gateway.utils.Util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Controller
public class APIController {

	private static final Logger logger = LogManager.getLogger(APIController.class);

	private LocalDateTime dateF;
	private String folder;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;

	private Gson gson;

	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDSS;

	@Value("${key.LINK_SUCCESS}")
	private String linkSuccess;

	@Value("${key.LINK_CCB}")
	private String linkCcb;

	@Value("${key.LINK_FAIL}")
	private String linkFail;

	@Value("${key.LINK_CHALENGE}")
	private String linkChalenge;

	@Value("${key.LINK_INDEX}")
	private String linkIndex;

	@Value("${key.SWITCH_URL}")
	private String ipSwitch;

	@Value("${key.SWITCH_PORT}")
	private String portSwitch;

	@Value("${key.SECRET}")
	private String secret;

	@Value("${key.USER_TOKEN}")
	private String usernameToken;

	@Value("${key.JWT_TOKEN_VALIDITY}")
	private long jwtTokenValidity;

	@Value("${key.ENVIRONEMENT}")
	private String environement;

	//@Autowired
	private final DemandePaiementService demandePaiementService;

	//@Autowired
	private final AutorisationService autorisationService;

	//@Autowired
	private final HistoAutoGateService histoAutoGateService;

	//@Autowired
	private final TransactionService transactionService;

	//@Autowired
	private final CommercantService commercantService;

	//@Autowired
	private final TelecollecteService telecollecteService;

	//@Autowired
	private final CardtokenService cardtokenService;

	//@Autowired
	private final CodeReponseService codeReponseService;

	//@Autowired
	private final GalerieService galerieService;

	private final ReccuringTransactionService recService;

	private final EmetteurService emetteurService;

	private final APIParamsService apiParamsService;
	
	public static final String DF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	DateFormat dateFormat = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
	DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

	public APIController(DemandePaiementService demandePaiementService, AutorisationService autorisationService,
			HistoAutoGateService histoAutoGateService, CommercantService commercantService, 
			GalerieService galerieService, TelecollecteService telecollecteService, 
			TransactionService transactionService, CardtokenService cardtokenService, 
			CodeReponseService codeReponseService, ReccuringTransactionService recService,
						 EmetteurService emetteurService, APIParamsService apiParamsService) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		dateF = LocalDateTime.now(ZoneId.systemDefault());
		folder = dateF.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
		this.demandePaiementService = demandePaiementService;
		this.autorisationService = autorisationService;
		this.histoAutoGateService = histoAutoGateService;
		this.commercantService = commercantService;
		this.galerieService = galerieService;
		this.telecollecteService = telecollecteService;
		this.transactionService = transactionService;
		this.cardtokenService = cardtokenService;
		this.codeReponseService = codeReponseService;
		this.recService = recService;
		this.emetteurService = emetteurService;
		this.apiParamsService = apiParamsService;
	}

	@PostMapping(value = "/napspayment/authorization", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String authorization(@RequestHeader MultiValueMap<String, String> header, @RequestBody String auths,
								HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_AUTH_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start authorization() ************** ");
		logger.info("*********** Start authorization() ************** ");

		logger.info("authorization api call start ...");

		autorisationService.logMessage(file, "authorization api call start ...");

		autorisationService.logMessage(file, "authorization : [" + auths + "]");

		LinkRequestDto linkRequestDto;

		try {
			linkRequestDto = new ObjectMapper().readValue(auths, LinkRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "authorization 500 malformed json expression " + auths + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "authorization 500 malformed json expression", null);
		}

		String capture_id, mesg_type, merc_codeactivite, amount,cardnumber,websiteid,expirydate,acqcode,cvv,
				merchant_city, acq_type, processing_code, reason_code, transaction_condition,merchant_name,currency,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, num_trs = "", etataut, auth3ds;

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		try {
			cardnumber = linkRequestDto.getCardnumber();
			expirydate = linkRequestDto.getExpirydate() == null ? "" : linkRequestDto.getExpirydate();
			auth3ds = linkRequestDto.getAuth3ds() == null ? "Y" : linkRequestDto.getAuth3ds();
			cvv = linkRequestDto.getCvv() == null ? "" : linkRequestDto.getCvv();
			currency = linkRequestDto.getCurrency() == null ? "504" : linkRequestDto.getCurrency();
			Double montant = 0.00;
			amount = Util.sanitizeAmount(linkRequestDto.getAmount());
			montant = Double.valueOf(amount);
			if (montant < 5) {
				return Util.getMsgError(folder, file, null, "The amount must be greater than or equal to 5dh", null);
			}
		} catch (Exception e) {
			autorisationService.logMessage(file, "The amount must be greater than or equal to 5dh" + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "The amount must be greater than or equal to 5dh" + e.getMessage(),
					null);
		}

		// TODO: get cardnumber by token
		if (!linkRequestDto.getToken().equals("") && linkRequestDto.getToken() != null && !linkRequestDto.getToken().equals("null")) {
			try {
				CardtokenDto card = cardtokenService.findByIdMerchantAndToken(linkRequestDto.getMerchantid(), linkRequestDto.getToken());
				if (card != null) {
					if (card.getCardNumber() != null) {
						cardnumber = card.getCardNumber();
						if (linkRequestDto.getExpirydate().equals("")) {
							String dateExStr = dateFormat.format(card.getExprDate());
							expirydate = dateExStr.substring(2, 4) + dateExStr.substring(5, 7);
						}
					}
				}
			} catch (Exception jerr) {
				autorisationService.logMessage(file, "authorization 500 token not found" + Util.formatException(jerr));
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500 token not found", null);
			}
		}

		String timeStamp = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS).format(new Date());

		autorisationService.logMessage(file, "authorization_" + linkRequestDto.getOrderid() + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(linkRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorization 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorization 500 Merchant misconfigured in DB or not existing", "");
		}

		GalerieDto galerie = null;

		try {
			galerie = galerieService.findByCodeCmr(linkRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"authorization 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorization 500 Galerie misconfigured in DB or not existing", "15");
		}

		if (galerie == null) {
			autorisationService.logMessage(file,
					"authorization 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorization 500 Galerie misconfigured in DB or not existing", "15");
		}

		websiteid = linkRequestDto.getWebsiteid() == null ? "" : linkRequestDto.getWebsiteid();
		if (!websiteid.equals(galerie.getCodeGal())) {
			websiteid = galerie.getCodeGal();
		}

		// TODO: get demandepaiement id , check if exist

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(linkRequestDto.getOrderid(), linkRequestDto.getMerchantid());

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"authorization 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err1));

			return Util.getMsgError(folder, file, linkRequestDto, "authorization 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null) {
			autorisationService.logMessage(file,
					"authorization 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:["
							+ linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "authorization 500 Error Already exist in PaiementRequest",
					"16");
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			autorisationService.logMessage(file, "authorization 500 Card number length is incorrect orderid:["
					+ linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "The card number is incomplete, please try again.", null);
		}

		if (i_card_valid == 2) {
			autorisationService.logMessage(file,
					"authorization 500 Card number  is not valid incorrect luhn check orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "The card number is invalid, please try again.", null);
		}

		int i_card_type = Util.getCardIss(cardnumber);

		try {

			dmd = new DemandePaiementDto();

			dmd.setComid(linkRequestDto.getMerchantid());
			dmd.setCommande(linkRequestDto.getOrderid());
			dmd.setDemPan(cardnumber);
			dmd.setDemCvv(cvv);
			dmd.setGalid(websiteid);
			dmd.setSuccessURL(linkRequestDto.getSuccessURL());
			dmd.setFailURL(linkRequestDto.getFailURL());
			dmd.setTypeCarte(i_card_type + "");
			amount = Util.sanitizeAmount(amount);
			dmd.setMontant(Double.parseDouble(amount));
			dmd.setNom(linkRequestDto.getLname());
			dmd.setPrenom(linkRequestDto.getFname());
			dmd.setEmail(linkRequestDto.getEmail());
			dmd.setTel(linkRequestDto.getPhone());
			dmd.setAddress(linkRequestDto.getAddress());
			dmd.setCity(linkRequestDto.getCity());
			dmd.setCountry(linkRequestDto.getCountry());
			dmd.setState(linkRequestDto.getState());
			dmd.setPostcode(linkRequestDto.getZipcode());
			if(linkRequestDto.getTransactiontype() == null) {
				linkRequestDto.setTransactiontype("0");
			}
			if (linkRequestDto.getTransactiontype().equals("P")) {
				// TODO: stokage date exp pour utiliser dans la capture (api : .../cpautorisation)
				dmd.setDateexpnaps(expirydate);
			}
			dmd.setLangue("E");
			dmd.setEtatDemande("INIT");

			formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			dmd.setDemDateTime(dateFormat.format(new Date()));
			if (linkRequestDto.getRecurring() != null && linkRequestDto.getRecurring().equalsIgnoreCase("Y")) {
				dmd.setIsCof("Y");
			}
			if (linkRequestDto.getRecurring() != null && linkRequestDto.getRecurring().equalsIgnoreCase("N")) {
				dmd.setIsCof("N");
			}
			dmd.setIs3ds(auth3ds);
			dmd.setIsAddcard("N");
			dmd.setIsTokenized("N");
			dmd.setIsWhitelist("N");
			dmd.setIsWithsave("N");

			// TODO: generer token
			String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
			dmd.setTokencommande(tokencommande);
			// TODO: set transctiontype
			dmd.setTransactiontype(linkRequestDto.getTransactiontype());
			// TODO: insérer info capture dans le champ Refdemande
			dmd.setRefdemande("capture=" + linkRequestDto.getCapture());

			dmdSaved = demandePaiementService.save(dmd);
			dmdSaved.setExpery(expirydate);

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"authorization 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + linkRequestDto.getOrderid() + "]"
							+ Util.formatException(err1));

			return Util.getMsgError(folder, file, linkRequestDto,
					"The current transaction was not successful, your account will not be debited, please try again.",
					null);
		}

		// TODO: for test control risk
		// TODO: refactoring code 2024-03-20
		autorisationService.logMessage(file, "Debut controlleRisk");
		try {
			String msg = autorisationService.controlleRisk(dmdSaved, folder, file);
			if (!msg.equalsIgnoreCase("OK")) {
				dmdSaved.setEtatDemande("REJET_RISK_CTRL");
				dmdSaved.setDemCvv("");
				demandePaiementService.save(dmdSaved);
				autorisationService.logMessage(file, "authorization 500 " + msg);
				return Util.getMsgError(folder, file, linkRequestDto, msg, null);
			}
		} catch (Exception e) {
			dmdSaved.setDemCvv("");
			dmdSaved.setEtatDemande("REJET_RISK_CTRL");
			demandePaiementService.save(dmdSaved);
			autorisationService.logMessage(file,
					"authorization 500 ControlRiskCmr misconfigured in DB or not existing merchantid:["
							+ dmdSaved.getComid() + Util.formatException(e));
			return Util.getMsgError(folder, file, linkRequestDto,
					"The current transaction was not successful, your account will not be debited, please try again.", null);
		}
		autorisationService.logMessage(file, "Fin controlleRisk");
		String dtpattern, sdt, tmpattern, stm = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;
		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
		} catch (Exception err2) {
			autorisationService.logMessage(file,
					"authorization 500 Error during  date formatting for given orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err2));

			return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.", null);
		}

		autorisationService.logMessage(file, "Generating paymentid...");

		String uuid_paymentid, paymentid = "";
		try {
			uuid_paymentid = String.format("%040d",	new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"authorization 500 Error during  paymentid generation for given orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(e));
		}

		autorisationService.logMessage(file, "Generating paymentid OK");
		autorisationService.logMessage(file, "paymentid :[" + paymentid + "]");

		JSONObject jso = new JSONObject();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		// TODO: appel 3DSSecure ***********************************************************

		/**
		 * dans la preprod les tests sans 3DSS on commente l'appel 3DSS et on mj
		 * reponseMPI="Y"
		 */
		autorisationService.logMessage(file, "environement : " + environement);
		if (environement.equals("PREPROD")) {
			threeDsecureResponse.setReponseMPI("Y");
		} else {
			if (auth3ds.equals("N")) {
				autorisationService.logMessage(file, "Si auth3ds = N passer sans 3DS ");
				threeDsecureResponse.setReponseMPI("Y");
			} else {
				autorisationService.logMessage(file, "Si auth3ds = Y passer avec 3DS ");
				threeDsecureResponse = autorisationService.preparerAeqThree3DSS(dmdSaved, folder, file);
			}
		}
		// TODO: fin 3DSSecure ***********************************************************

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
		String expiry = ""; // TODO: YYMM

		reponseMPI = threeDsecureResponse.getReponseMPI();

		threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();

		eci = threeDsecureResponse.getEci() == null ? "" : threeDsecureResponse.getEci();

		cavv = threeDsecureResponse.getCavv() == null ? "" : threeDsecureResponse.getCavv();

		errmpi = threeDsecureResponse.getErrmpi() == null ? "" : threeDsecureResponse.getErrmpi();

		expiry = threeDsecureResponse.getExpiry() == null ? "" : threeDsecureResponse.getExpiry();

		if (idDemande == null || idDemande.equals("")) {
			dmdSaved.setDemCvv("");
			demandePaiementService.save(dmdSaved);
			autorisationService.logMessage(file, "received idDemande from MPI is Null or Empty");
			dmdSaved.setEtatDemande("MPI_KO");
			demandePaiementService.save(dmdSaved);
			autorisationService.logMessage(file,
					"demandePaiement after update MPI_KO idDemande null : " + dmdSaved.toString());
			return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.", "96");
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			dmdSaved.setDemCvv("");
			demandePaiementService.save(dmdSaved);
			autorisationService.logMessage(file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.", "96");
		}

		if (reponseMPI == null || reponseMPI.equals("")) {
			dmd.setDemCvv("");
			dmd.setEtatDemande("MPI_KO");
			demandePaiementService.save(dmd);
			autorisationService.logMessage(file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			autorisationService.logMessage(file, "Response 3DS is null");
			return Util.getMsgError(folder, file, linkRequestDto, "Response 3DS is null", "96");
		}

		if (reponseMPI.equals("Y")) {
			// TODO: ********************* Frictionless responseMPI equal Y *********************
			autorisationService.logMessage(file,
					"********************* Cas frictionless responseMPI equal Y *********************");
			if (threeDSServerTransID != null && !threeDSServerTransID.equals("")) {
				dmd.setDemxid(threeDSServerTransID);
				dmd.setIs3ds("N");
				demandePaiementService.save(dmd);
			}

			// TODO: 2024-03-05
			montanttrame = Util.formatMontantTrame(folder, file, amount, linkRequestDto.getOrderid(), linkRequestDto.getMerchantid(), linkRequestDto);

			merc_codeactivite = current_merchant.getCmrCodactivite();
			acqcode = current_merchant.getCmrCodbqe();
			merchant_name = Util.pad_merchant(linkRequestDto.getMerchantname(), 19, ' ');

			merchant_city = "MOROCCO        ";

			acq_type = "0000";
			reason_code = "H";
			transaction_condition = "6";
			mesg_type = "0";
			processing_code = "";

			if (linkRequestDto.getTransactiontype().equals("0")) {
				processing_code = "0";
			} else if (linkRequestDto.getTransactiontype().equals("P")) {
				processing_code = "P";
			} else {
				processing_code = "0";
			}

			// TODO: ajout cavv (cavv+eci) xid dans la trame
			String champ_cavv = "";
			xid = threeDSServerTransID;
			if (cavv == null || eci == null) {
				champ_cavv = null;
				autorisationService.logMessage(file, "cavv == null || eci == null");
			} else if (cavv != null && eci != null) {
				champ_cavv = cavv + eci;
			} else {
				autorisationService.logMessage(file, "champ_cavv = null");
				champ_cavv = null;
			}

			APIParamsDto api_param = null;

			try {
				api_param = apiParamsService.findByMerchantIDAndProductAndVersion(linkRequestDto.getMerchantid(), "MXPLUS", "1.0");
			} catch (Exception e) {
				autorisationService.logMessage(file, "error during api params retrevial");
			}

			/** ce controle n'est pas obligatoire afin de ne pas impacter les cmr deja en prod
			if (api_param == null) {
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500, error api params not connfigured in DB api_param", "96");
			}
			String is_ok = api_param.getAuthorization();

			if (is_ok == null) {
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500, error api params not connfigured in DB api_param", "96");
			}
			if (!is_ok.equalsIgnoreCase("Y")) {
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500, This api call is disabled for the moment api_param", "96");
			}
			String is_recurring = api_param.getReccuring();

			if (is_recurring == null) {
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500, error api params not connfigured in DB api_param", "96");
			}
			if (!is_recurring.equalsIgnoreCase("Y")) {
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500, This api call is disabled for the moment api_param", "96");
			}*/
			if (linkRequestDto.getRecurring() != null) {
				if(!linkRequestDto.getRecurring().equals("Y") && !linkRequestDto.getRecurring().equals("N")) {
					return Util.getMsgError(folder, file, linkRequestDto, "authorization 500, reccuring flag must be present and should be Y or N", "96");
				}
			}

			EmetteurDto natIssuer = emetteurService.getNATIusser(cardnumber);

			int card_destination = 1;

			if (natIssuer == null) {
				card_destination = Util.card_switch(folder, file, cardnumber, false, null);
				autorisationService.logMessage(file, "natIssuer is null card_destination : " + card_destination);
			} else {
				String switch_server_code = natIssuer.getEmtCodeserv();
				if (switch_server_code == null) {
					switch_server_code = "EMPTY";
				}
				card_destination = Util.card_switch(folder, file, cardnumber, true, switch_server_code.trim());
				autorisationService.logMessage(file, "natIssuer is not null card_destination/switch_server_code : "
						+ card_destination + " / " + switch_server_code);
			}

			boolean reccurent_cvv_check_obligatory = false;
			if (card_destination == 0 || card_destination == 1) {
				reccurent_cvv_check_obligatory = true;
			}

			boolean cvv_present = checkCvvPresence(cvv);
			boolean is_reccuring = isReccuringCheck(linkRequestDto.getRecurring());
			boolean is_first_trs = true;
			if (linkRequestDto.getToken() != null && !linkRequestDto.getToken().equals("")) {
				cvv_present = true;
			}
			String first_auth = "";
			long lrec_serie = 0;
			String rec_serie = "";

			autorisationService.logMessage(file, "Switch processing start ...");

			String tlv = "";
			autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

			autorisationService.logMessage(file, "cvv_present : " + cvv_present);
			autorisationService.logMessage(file, "is_reccuring : " + is_reccuring);

			if (!cvv_present && !is_reccuring) {
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file,
						"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				return Util.getMsgError(folder, file, linkRequestDto,
						"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
						"82");
			}

			// TODO: not reccuring , normal
			if (cvv_present && !is_reccuring) {
				autorisationService.logMessage(file, "not reccuring , normal cvv_present && !is_reccuring");
				try {
					tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
							.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
							.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
							.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
							.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
							.withField(Tags.tag16, date).withField(Tags.tag17, heure)
							.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + linkRequestDto.getMerchantid())
							.withField(Tags.tag9, linkRequestDto.getMerchantid()).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
							.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
							.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
							.withField(Tags.tag168, xid).encode();
				} catch (Exception err4) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"authorization 500 Error during switch tlv buildup for given orderid:[" + linkRequestDto.getOrderid()
									+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err4));

					return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.",
							"96");
				}
				autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");
			}

			// TODO: 12-06-2025 implemente reccuring payment
			if (is_reccuring) {
				is_first_trs = isFirstTransaction(linkRequestDto.getMerchantid(), cardnumber);

				// card uknown in system ==> first transaction
				autorisationService.logMessage(file, "is_first_trs : " + is_first_trs);
				autorisationService.logMessage(file, "reccurent_cvv_check_obligatory : " + reccurent_cvv_check_obligatory);

				if (is_first_trs) {
					if (!cvv_present) { // is the cvv present ?
						if (reccurent_cvv_check_obligatory) { // is the cvv obligatory ? national switch yes
							autorisationService.logMessage(file,"authorization 500 cvv not set , reccuring flag set to Y and first transaction is detected orderid:[" + linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");
							return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, cvv not set reccuring flag set to Y and first transaction is detected, please try again.",
									"17");
						} else {
							// cvv not obligatory in first transaction, international
							autorisationService.logMessage(file, "cvv not obligatory in first transaction, international");
							try {
								tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
										.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
										.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
										.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
										.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
										.withField(Tags.tag16, date).withField(Tags.tag17, heure)
										.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + linkRequestDto.getMerchantid())
										.withField(Tags.tag9, linkRequestDto.getMerchantid()).withField(Tags.tag66, rrn)
										.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
										.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
										.withField(Tags.tag168, xid).withField(Tags.tag601, "R111111111").encode();
							} catch (Exception err4) {
								dmd.setDemCvv("");
								demandePaiementService.save(dmd);
								autorisationService.logMessage(file,
										"authorization 500 Error during switch tlv buildup for given orderid:[" + linkRequestDto.getOrderid()
												+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err4));

								return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.",
										"96");
							}
							autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");
						}
					} else { // first transaction with cvv present, a normal transaction
						autorisationService.logMessage(file, "first transaction with cvv present, a normal transaction");
						try {
							tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
									.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
									.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
									.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
									.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
									.withField(Tags.tag16, date).withField(Tags.tag17, heure)
									.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + linkRequestDto.getMerchantid())
									.withField(Tags.tag9, linkRequestDto.getMerchantid()).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
									.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
									.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
									.withField(Tags.tag168, xid)/*.withField(Tags.tag601, "R111111111")*/.encode();
						} catch (Exception err4) {
							dmd.setDemCvv("");
							demandePaiementService.save(dmd);
							autorisationService.logMessage(file,
									"authorization 500 Error during switch tlv buildup for given orderid:[" + linkRequestDto.getOrderid()
											+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err4));

							return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.",
									"96");
						}
						autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");
					}

				} else { // reccuring
					autorisationService.logMessage(file, "trs already existe");
					try {
						first_auth = getFirstTransactionAuth(linkRequestDto.getMerchantid(), cardnumber);
						lrec_serie = getTransactionSerie(linkRequestDto.getMerchantid(), cardnumber);

					} catch (Exception e) {
						dmd.setDemCvv("");
						demandePaiementService.save(dmd);
						autorisationService.logMessage(file,"authorization 500 DB Error duing reccurent transations serie check orderid:[" + linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(e));
						return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.",
								"96");
					}

					lrec_serie = lrec_serie + 1;
					rec_serie = String.format("%03d", lrec_serie);
					autorisationService.logMessage(file, "lrec_serie + 1 : " + lrec_serie);
					autorisationService.logMessage(file, "rec_serie : " + rec_serie);

					try {
						tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
								.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
								.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
								.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
								.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
								.withField(Tags.tag16, date).withField(Tags.tag17, heure)
								.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + linkRequestDto.getMerchantid())
								.withField(Tags.tag9, linkRequestDto.getMerchantid()).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
								.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
								.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
								.withField(Tags.tag168, xid).withField(Tags.tag601, "R" + rec_serie + first_auth)
								.encode();
					} catch (Exception err4) {
						dmd.setDemCvv("");
						demandePaiementService.save(dmd);
						autorisationService.logMessage(file,"authorization 500 Error during switch tlv buildup for given orderid orderid:[" + linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err4));
						return Util.getMsgError(folder, file, linkRequestDto, "The current transaction was not successful, your account will not be debited, please try again.",
								"96");
					}
					autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");
				}
			}

			autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

			String resp_tlv = "";
//			SwitchTCPClient sw = SwitchTCPClient.getInstance();
			int port = 0;
			String sw_s = "", s_port = "";
			int switch_ko = 0;
			try {

				s_port = portSwitch;
				sw_s = ipSwitch;

				port = Integer.parseInt(s_port);

				autorisationService.logMessage(file, "Switch TCP client V2 Connecting ...");

				SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

				boolean s_conn = switchTCPClient.isConnected();

				if (!s_conn) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file, "Payment failed, the Switch is down.");

					return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
				}

				if (s_conn) {
					autorisationService.logMessage(file, "Switch Connected.");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					autorisationService.logMessage(file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction UnknownHostException !!!" + Util.formatException(e));

				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");

			} catch (java.net.ConnectException e) {
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction ConnectException !!!" + Util.formatException(e));
				switch_ko = 1;
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			} catch (Exception e) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction Exception!!!" + Util.formatException(e));
				switch_ko = 1;
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				autorisationService.logMessage(file, "authorization 500 Error Switch null response" + "switch ip:["
						+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
				autorisationService.logMessage(file, "authorization 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
			}

			autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

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
					tag66_resp = tlvp.getTag(Tags.tag66); // TODO: f1
					tag18_resp = tlvp.getTag(Tags.tag18);
					tag19_resp = tlvp.getTag(Tags.tag19); // TODO: f2
					tag23_resp = tlvp.getTag(Tags.tag23);
					tag20_resp = tlvp.getTag(Tags.tag20);
					tag21_resp = tlvp.getTag(Tags.tag21);
					tag22_resp = tlvp.getTag(Tags.tag22);
					tag80_resp = tlvp.getTag(Tags.tag80);
					tag98_resp = tlvp.getTag(Tags.tag98);

				} catch (Exception e) {
					autorisationService.logMessage(file, "Switch  malfunction tlv parsing !!!" + Util.formatException(e));
					switch_ko = 1;
					autorisationService.logMessage(file, "authorization 500 Error during tlv Switch response parse"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// TODO: controle switch
				if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
					autorisationService.logMessage(file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					autorisationService.logMessage(file,
							"authorization 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}
			}
			autorisationService.logMessage(file, "Switch TLV Respnose Processed");

			String tag20_resp_verified = "";
			String tag19_res_verified = "";
			String tag66_resp_verified = "";
			tag20_resp_verified = tag20_resp;
			tag19_res_verified = tag19_resp;
			tag66_resp_verified = tag66_resp;
			String s_status, pan_auto = "";

			// TODO: SWHistoAutoDto swhist = null;

			if (switch_ko == 1) {
				pan_auto = Util.formatagePan(cardnumber);
				autorisationService.logMessage(file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
						+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + linkRequestDto.getMerchantid());
			}

			HistoAutoGateDto hist = null;
			Integer Ihist_id = null;

			autorisationService.logMessage(file, "Insert into Histogate...");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

				s_status = "";
				try {
					CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
					autorisationService.logMessage(file, "" + codeReponseDto);
					if (codeReponseDto != null) {
						s_status = codeReponseDto.getRpcLibelle();
					}
				} catch (Exception ee) {
					autorisationService.logMessage(file, "authorization 500 Error codeReponseDto null" + Util.formatException(ee));
					// TODO: TODO: ee.printStackTrace();
				}

				autorisationService.logMessage(file, "get status Switch status : [" + s_status + "]");

				websiteid = dmd.getGalid();

				autorisationService.logMessage(file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);

				autorisationService.logMessage(file, "HistoAutoGate data filling start ...");

				autorisationService.logMessage(file, "websiteid : " + websiteid);

				Date current_date_1 = getDateWithoutTime(curren_date_hist);
				hist.setHatDatdem(current_date_1);

				hist.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
				hist.setHatMontant(Double.parseDouble(amount));
				hist.setHatNumcmr(linkRequestDto.getMerchantid());
				hist.setHatCoderep(tag20_resp_verified);
				tag20_resp = tag20_resp_verified;
				hist.setHatDevise(currency);
				hist.setHatBqcmr(acqcode);
				hist.setHatPorteur(pan_auto);
				hist.setHatMtfref1(s_status);
				hist.setHatNomdeandeur(websiteid);
				hist.setHatNautemt(tag19_res_verified); // TODO: f2
				tag19_resp = tag19_res_verified;
				if (tag22_resp != null)
					hist.setHatProcode(tag22_resp.charAt(0));
				else
					hist.setHatProcode('6');
				hist.setHatExpdate(expirydate);
				hist.setHatRepondeur(tag21_resp);
				hist.setHatTypmsg("3");
				hist.setHatRrn(tag66_resp_verified); // TODO: f1
				tag66_resp_verified = tag66_resp;
				hist.setHatEtat('E');
				if (websiteid.equals("")) {
					hist.setHatCodtpe("1");
				} else {
					hist.setHatCodtpe(websiteid);
				}
				hist.setHatMcc(merc_codeactivite);
				hist.setHatNumCommande(linkRequestDto.getOrderid());
				hist.setHatNumdem(new Long(numTransaction));

				if (checkCvvPresence(cvv)) {
					hist.setIsCvvVerified("Y");
				} else {
					hist.setIsCvvVerified("N");
				}

				hist.setIs3ds("N");
				hist.setIsAddcard("N");
				hist.setIsWhitelist("N");
				hist.setIsWithsave("N");
				hist.setIsTokenized("N");

				if (linkRequestDto.getRecurring() != null && linkRequestDto.getRecurring().equalsIgnoreCase("Y"))
					hist.setIsCof("Y");
				if (linkRequestDto.getRecurring() != null && linkRequestDto.getRecurring().equalsIgnoreCase("N"))
					hist.setIsCof("N");

				autorisationService.logMessage(file, "HistoAutoGate data filling end ...");

				autorisationService.logMessage(file, "HistoAutoGate Saving ...");

				hist = histoAutoGateService.save(hist);

				autorisationService.logMessage(file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

			} catch (Exception e) {
				autorisationService.logMessage(file,
						"authorization 500 Error during  insert in histoautogate for given orderid:[" + linkRequestDto.getOrderid() + "]"
								+ Util.formatException(e));
				try {
					autorisationService.logMessage(file, "2eme tentative : HistoAutoGate Saving ... ");
					hist = histoAutoGateService.save(hist);
				} catch (Exception ex) {
					autorisationService.logMessage(file,
							"2eme tentative : authorization 500 Error during  insert in histoautogate for given orderid:["
									+ linkRequestDto.getOrderid() + "]" + Util.formatException(ex));
				}
			}

			autorisationService.logMessage(file, "HistoAutoGate OK.");

			if (tag20_resp == null) {
				tag20_resp = "";
			}

			if (tag20_resp.equalsIgnoreCase("00"))

			{
				autorisationService.logMessage(file, "SWITCH RESONSE CODE :[00]");
				try {
					autorisationService.logMessage(file, "update etat demande : SW_PAYE ...");

					dmd.setEtatDemande("SW_PAYE");
					if (dmd.getTransactiontype().equals("0")) {
						dmd.setDemCvv("");
					}
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");
				} catch (Exception e) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
									+ linkRequestDto.getOrderid() + "]" + Util.formatException(e));
				}

			} else {

				autorisationService.logMessage(file, "transaction declined !!! ");
				autorisationService.logMessage(file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {

					autorisationService.logMessage(file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtatDemande("SW_REJET");
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					// TODO: old
					// TODO: hist.setHatEtat('A');
					// TODO: histoAutoGateService.save(hist);
				} catch (Exception e) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
									+ linkRequestDto.getOrderid() + "]" + Util.formatException(e));

					return Util.getMsgError(folder, file, linkRequestDto,
							"authorization 500 Error during  DemandePaiement update SW_REJET", tag20_resp);
				}

				autorisationService.logMessage(file, "update Demandepaiement status to SW_REJET OK.");
				// TODO: 2024-02-27
				try {
					if (hist.getId() == null) {
						// TODO: get histoauto check if exist
						HistoAutoGateDto histToAnnulle = histoAutoGateService
								.findByHatNumCommandeAndHatNumcmrV1(linkRequestDto.getOrderid(), linkRequestDto.getMerchantid());
						if (histToAnnulle != null) {
							autorisationService.logMessage(file,
									"transaction declinded ==> update HistoAutoGateDto etat to A ...");
							histToAnnulle.setHatEtat('A');
							histToAnnulle = histoAutoGateService.save(histToAnnulle);
						} else {
							hist.setHatEtat('A');
							hist = histoAutoGateService.save(hist);
						}
					} else {
						hist.setHatEtat('A');
						hist = histoAutoGateService.save(hist);
					}
				} catch (Exception err2) {
					autorisationService.logMessage(file,
							"authorization 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:["
									+ linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				}
				autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
				// TODO: 2024-02-27
			}

			// TODO: JSONObject jso = new JSONObject();

			autorisationService.logMessage(file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDemPan();
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"authorization 500 Error during authdata preparation orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(e));

				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500 Error during authdata preparation",
						tag20_resp);
			}

			// TODO: reccurent transaction processing
			// first time transaction insert into rec
			if (is_first_trs && is_reccuring && coderep.equalsIgnoreCase("00")) {

				ReccuringTransactionDto rec_1 = new ReccuringTransactionDto();
				try {
					rec_1.setAmount(amount);
					rec_1.setAuthorizationNumber(authnumber);
					rec_1.setCardnumber(cardnumber);
					rec_1.setCountry(linkRequestDto.getCountry());
					rec_1.setCurrency(currency);
					rec_1.setFirstTransaction("Y");
					rec_1.setFirstTransactionNumber(authnumber);
					rec_1.setMerchantid(linkRequestDto.getMerchantid());
					rec_1.setOrderid(linkRequestDto.getOrderid());
					rec_1.setPaymentid(paymentid);
					rec_1.setReccuringNumber(0);
					rec_1.setToken(linkRequestDto.getToken());
					rec_1.setTransactionid(linkRequestDto.getTransactionid());
					rec_1.setWebsiteid(websiteid.length() > 3 ? websiteid.substring(0,3) : websiteid);

					recService.save(rec_1);
					autorisationService.logMessage(file, "rec_1 " + rec_1.toString());
				} catch (Exception e) {
					autorisationService.logMessage(file,
							"authorization 500 Error during save in api_reccuring orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(e));
				}
			}

			// TODO: reccurent insert and update
			if (!is_first_trs && is_reccuring && coderep.equalsIgnoreCase("00")) {

				ReccuringTransactionDto rec_1 = new ReccuringTransactionDto();
				try {
					rec_1.setAmount(amount);
					rec_1.setAuthorizationNumber(authnumber);
					rec_1.setCardnumber(cardnumber);
					rec_1.setCountry(linkRequestDto.getCountry());
					rec_1.setCurrency(currency);
					rec_1.setFirstTransaction("N");
					rec_1.setFirstTransactionNumber(authnumber);
					rec_1.setMerchantid(linkRequestDto.getMerchantid());
					rec_1.setOrderid(linkRequestDto.getOrderid());
					rec_1.setPaymentid(paymentid);
					rec_1.setReccuringNumber(lrec_serie);
					rec_1.setToken(linkRequestDto.getToken());
					rec_1.setTransactionid(linkRequestDto.getTransactionid());
					rec_1.setWebsiteid(websiteid.length() > 3 ? websiteid.substring(0,3) : websiteid);

					recService.save(rec_1);
					autorisationService.logMessage(file, "rec_1 " + rec_1.toString());
				} catch (Exception e) {
				autorisationService.logMessage(file,
						"authorization 500 Error during save in api_reccuring orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(e));
				}
			}

			try {

				// TODO: Transaction info
				jso.put("statuscode", coderep);
				jso.put("status", motif);
				jso.put("etataut", "Y");
				jso.put("orderid", linkRequestDto.getOrderid());
				jso.put("amount", amount);
				jso.put("transactiondate", sdt);
				jso.put("transactiontime", stm);
				jso.put("authnumber", authnumber);
				jso.put("paymentid", paymentid);
				jso.put("transactionid", linkRequestDto.getTransactionid());

				// TODO: Merchant info
				jso.put("merchantid", merchnatidauth);
				jso.put("merchantname", linkRequestDto.getMerchantname());
				jso.put("websitename", linkRequestDto.getWebsitename());
				jso.put("websiteid", websiteid);

				// TODO: Card info
				jso.put("cardnumber", Util.formatCard(cardnumber));

				// TODO: Client info
				jso.put("fname", linkRequestDto.getFname());
				jso.put("lname", linkRequestDto.getLname());
				jso.put("email", linkRequestDto.getEmail());

			} catch (Exception jsouterr) {
				autorisationService.logMessage(file,
						"authorization 500 Error during jso out processing given authnumber:[" + authnumber + "]"
								+ jsouterr);
				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500 Error during jso out processing",
						tag20_resp);
			}

			logger.info("autorization api response frictionless :  [" + jso.toString() + "]");
			autorisationService.logMessage(file,
					"autorization api response frictionless :  [" + jso.toString() + "]");
			// TODO: fin
			// TODO: *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// TODO: ********************* Cas chalenge responseMPI equal C ou D
			// TODO: *********************
			autorisationService.logMessage(file, "****** Cas chalenge responseMPI equal C ou D ******");
			try {

				// TODO: Transaction info
				jso.put("statuscode", "00");
				jso.put("status", "Chalenge");
				jso.put("etataut", "C");
				jso.put("orderid", linkRequestDto.getOrderid());
				jso.put("amount", amount);
				jso.put("transactiondate", sdt);
				jso.put("transactiontime", stm);
				// TODO: jso.put("authnumber", authnumber);
				jso.put("paymentid", paymentid);
				jso.put("transactionid", linkRequestDto.getTransactionid());

				// TODO: Merchant info
				jso.put("merchantid", linkRequestDto.getMerchantid());
				jso.put("merchantname", linkRequestDto.getMerchantname());
				jso.put("websitename", linkRequestDto.getWebsitename());
				jso.put("websiteid", websiteid);

				// TODO: Card info
				jso.put("cardnumber", Util.formatCard(cardnumber));

				// TODO: Client info
				jso.put("fname", linkRequestDto.getFname());
				jso.put("lname", linkRequestDto.getLname());
				jso.put("email", linkRequestDto.getEmail());

				// TODO: Link ACS chalenge info
				jso.put("linkacs", linkChalenge + dmd.getTokencommande());

				// TODO: insertion htmlCreq dans la demandePaiement
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				if (threeDSServerTransID.equals("") || threeDSServerTransID == null) {
					threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
				}
				dmd.setDemxid(threeDSServerTransID);
				dmd.setEtatDemande("SND_TO_ACS");
				dmd.setIs3ds("Y");
				dmd = demandePaiementService.save(dmd);

				autorisationService.logMessage(file, "threeDSServerTransID : " + dmd.getDemxid());
				logger.info("linkChalenge " + linkChalenge + dmd.getTokencommande());
				autorisationService.logMessage(file, "linkChalenge " + linkChalenge + dmd.getTokencommande());

				logger.info("autorization api response chalenge :  [" + jso.toString() + "]");
				autorisationService.logMessage(file,
						"autorization api response chalenge :  [" + jso.toString() + "]");
			} catch (Exception ex) {
				autorisationService.logMessage(file, "authorization 500 Error during jso out processing " + Util.formatException(ex));

				return Util.getMsgError(folder, file, linkRequestDto, "authorization 500 Error during jso out processing ",
						null);
			}
		} else if (reponseMPI.equals("E")) {
			// TODO: ********************* Cas responseMPI equal E
			// TODO: *********************
			autorisationService.logMessage(file, "****** Cas responseMPI equal E ******");
			autorisationService.logMessage(file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
			dmd.setEtatDemande("MPI_DS_ERR");
			dmd.setDemCvv("");
			dmd.setDemxid(threeDSServerTransID);
			demandePaiementService.save(dmd);

			// TODO: Transaction info
			jso.put("statuscode", "96");
			jso.put("status",
					"La transaction en cours n’a pas abouti (Problème authentification 3DSecure), votre compte ne sera pas débité, merci de contacter votre banque .");
			jso.put("etataut", "N");
			jso.put("orderid", linkRequestDto.getOrderid());
			jso.put("amount", amount);
			jso.put("transactiondate", sdt);
			jso.put("transactiontime", stm);
			jso.put("transactionid", linkRequestDto.getTransactionid());

			// TODO: Merchant info
			jso.put("merchantid", linkRequestDto.getMerchantid());
			jso.put("merchantname", linkRequestDto.getMerchantname());
			jso.put("websitename", linkRequestDto.getWebsitename());
			jso.put("websiteid", websiteid);

			// TODO: Card info
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// TODO: Client info
			jso.put("fname", linkRequestDto.getFname());
			jso.put("lname", linkRequestDto.getLname());
			jso.put("email", linkRequestDto.getEmail());

			// TODO: Link ACS chalenge info :
			jso.put("linkacs", "");

			logger.info("autorization api response fail :  [" + jso.toString() + "]");
			autorisationService.logMessage(file, "autorization api response fail :  [" + jso.toString() + "]");
		} else {
			switch (errmpi) {
				case "COMMERCANT NON PARAMETRE":
					autorisationService.logMessage(file, "COMMERCANT NON PARAMETRE : " + idDemande);
					dmd.setDemxid(threeDSServerTransID);
					dmd.setDemCvv("");
					dmd.setEtatDemande("MPI_CMR_INEX");
					demandePaiementService.save(dmd);
					// TODO: externalContext.redirect("operationErreur.xhtml?Error=".concat("COMMERCANT
					// TODO: NON PARAMETRE"));
					return Util.getMsgError(folder, file, linkRequestDto, "COMMERCANT NON PARAMETRE", "15");
				case "BIN NON PARAMETRE":
					autorisationService.logMessage(file, "BIN NON PARAMETRE : " + idDemande);
					dmd.setEtatDemande("MPI_BIN_NON_PAR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "BIN NON PARAMETREE", "96");
				case "DIRECTORY SERVER":
					autorisationService.logMessage(file, "DIRECTORY SERVER : " + idDemande);
					dmd.setEtatDemande("MPI_DS_ERR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "MPI_DS_ERR", "96");
				case "CARTE ERRONEE":
					autorisationService.logMessage(file, "CARTE ERRONEE : " + idDemande);
					dmd.setEtatDemande("MPI_CART_ERROR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "CARTE ERRONEE", "96");
				case "CARTE NON ENROLEE":
					autorisationService.logMessage(file, "CARTE NON ENROLEE : " + idDemande);
					dmd.setEtatDemande("MPI_CART_NON_ENR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "CARTE NON ENROLLE", "96");
			}
		}
		autorisationService.logMessage(file, "*********** End authorization() ************** ");
		logger.info("*********** End authorization() ************** ");
		return jso.toString();

	}

	@PostMapping(value = "/napspayment/linkpayment", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String getLink(@RequestHeader MultiValueMap<String, String> header, @RequestBody String linkP,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_LINK_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start getLink() ************** ");
		logger.info("*********** Start getLink() ************** ");

		autorisationService.logMessage(file, "getLink api call start ...");
		autorisationService.logMessage(file, "getLink : [" + linkP + "]");

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;

		LinkRequestDto linkRequestDto;

		try {
			linkRequestDto = new ObjectMapper().readValue(linkP, LinkRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "getLink 500 malformed json expression " + linkP + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "getLink 500 malformed json expression", null);
		}

		String amount, transactiondate, transactiontime, idDemande, lname ="", fname ="", websiteid ="", id_client = "", token ="";

		String url = "", status = "", statuscode = "";
		JSONObject jso = new JSONObject();

		try {
			Double montant = 0.00;
			amount = Util.sanitizeAmount(linkRequestDto.getAmount());
			montant = Double.valueOf(amount);
			if (montant < 5) {
				url = "";
				statuscode = "17";
				status = "The amount must be greater than or equal to 5dh";

				jso.put("statuscode", statuscode);
				jso.put("status", status);
				jso.put("orderid", linkRequestDto.getOrderid());
				jso.put("amount", amount);
				jso.put("url", url);
				autorisationService.logMessage(file, "The amount must be greater than or equal to 5dh");
				return jso.toString();
			}
		} catch (Exception e) {
			autorisationService.logMessage(file, "The amount must be greater than or equal to 5dh" + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "The amount must be greater than or equal to 5dh" + e.getMessage(),
					null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(linkRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(e));

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"getLink 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		GalerieDto galerie = null;

		try {
			galerie = galerieService.findByCodeCmr(linkRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"getLink 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid() + "] and merchantid:["
							+ linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Galerie misconfigured in DB or not existing",
					"15");
		}

		if (galerie == null) {
			autorisationService.logMessage(file,
					"getLink 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid() + "] and merchantid:["
							+ linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Galerie misconfigured in DB or not existing",
					"15");
		}
		if (!linkRequestDto.getWebsiteid().equals(galerie.getCodeGal())) {
			websiteid = galerie.getCodeGal();
		} else {
			websiteid = linkRequestDto.getWebsiteid();
		}

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(linkRequestDto.getOrderid(), linkRequestDto.getMerchantid());

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"getLink 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err1));

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null && check_dmd.getEtatDemande().equals("SW_PAYE")) {
			autorisationService.logMessage(file,
					"getLink 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Error Already exist in PaiementRequest", "16");
		}

		try {
			String tokencommande = "";
			if (check_dmd != null) {
				// TODO: generer token
				tokencommande = Util.genTokenCom(check_dmd.getCommande(), check_dmd.getComid());
				String sucs = check_dmd.getSuccessURL() == null ? "" : check_dmd.getSuccessURL();
				if(sucs.contains("epay.naps.ma") || sucs.contains("epayapi.naps.ma")
						|| check_dmd.getComid().equals("2200121")) {
					autorisationService.logMessage(file,"site epaylink continue");
					url = linkSuccess + check_dmd.getTokencommande();
					statuscode = "00";
					status = "OK";
					idDemande = String.valueOf(check_dmd.getIddemande());
				} else {
					url = "";
					statuscode = "17";
					status = "PaiementRequest Already exist orderid:[" + linkRequestDto.getOrderid() + "]";
					idDemande = String.valueOf(check_dmd.getIddemande());
				}
			} else {
				dmd = new DemandePaiementDto();

				dmd.setComid(linkRequestDto.getMerchantid());
				dmd.setCommande(linkRequestDto.getOrderid());
				dmd.setGalid(websiteid);
				dmd.setSuccessURL(linkRequestDto.getSuccessURL());
				dmd.setFailURL(linkRequestDto.getFailURL());
				dmd.setCallbackURL(linkRequestDto.getCallbackurl());
				amount = Util.sanitizeAmount(amount);
				dmd.setMontant(Double.parseDouble(amount));
				lname = linkRequestDto.getLname();
				if (lname.length() > 25) {
					lname = lname.substring(0, 25);
				}
				dmd.setNom(lname);
				fname = linkRequestDto.getFname();
				if (fname.length() > 20) {
					fname = fname.substring(0, 20);
				}
				dmd.setPrenom(fname);
				dmd.setEmail(linkRequestDto.getEmail());
				dmd.setTel(linkRequestDto.getPhone());
				dmd.setAddress(linkRequestDto.getAddress());
				dmd.setCity(linkRequestDto.getCity());
				dmd.setCountry(linkRequestDto.getCountry());
				dmd.setState(linkRequestDto.getState());
				dmd.setPostcode(linkRequestDto.getZipcode());
				dmd.setLangue("E");
				dmd.setEtatDemande("INIT");

				formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
				formatter_2 = new SimpleDateFormat("HH:mm:ss");
				trsdate = new Date();
				transactiondate = formatter_1.format(trsdate);
				transactiontime = formatter_2.format(trsdate);
				dmd.setDemDateTime(dateFormat.format(new Date()));

				id_client = linkRequestDto.getId_client() == null ? "" : linkRequestDto.getId_client();
				token = linkRequestDto.getToken() == null ? "" : linkRequestDto.getToken();

				dmd.setIdClient(id_client);
				dmd.setToken(token);

				if (!id_client.equalsIgnoreCase("") || !token.equalsIgnoreCase("")) {
					dmd.setIsCof("Y");
				} else {
					dmd.setIsCof("N");
				}
				dmd.setIsAddcard("N");
				dmd.setIsTokenized("N");
				dmd.setIsWhitelist("N");
				dmd.setIsWithsave("N");

				// TODO: generer token
				tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
				dmd.setTokencommande(tokencommande);

				if(linkRequestDto.getTransactiontype() == null) {
					linkRequestDto.setTransactiontype("0");
				}
				if(linkRequestDto.getTransactiontype() != null && linkRequestDto.getTransactiontype().isEmpty()) {
					linkRequestDto.setTransactiontype("0");
				}
				dmd.setTransactiontype(linkRequestDto.getTransactiontype());

				dmdSaved = demandePaiementService.save(dmd);

				url = linkSuccess + dmdSaved.getTokencommande();
				statuscode = "00";
				status = "OK";
				idDemande = String.valueOf(dmdSaved.getIddemande());
			}

		} catch (Exception err1) {
			url = "";
			statuscode = "";
			status = "KO";
			idDemande = "";
			autorisationService.logMessage(file,
					"getLink 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(err1));

			return Util.getMsgError(folder, file, linkRequestDto, "The current operation was not successful, please try again .", null);
		}

		try {
			// TODO: Transaction info
			jso.put("statuscode", statuscode);
			jso.put("status", status);
			jso.put("orderid", linkRequestDto.getOrderid());
			jso.put("amount", amount);
			jso.put("idDemande", idDemande);
			jso.put("url", url);

			// TODO: Merchant info
			jso.put("merchantid", linkRequestDto.getMerchantid());

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
			logger.info("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			autorisationService.logMessage(file,
					"getLink 500 Error during jso out processing given orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(err8));

			return Util.getMsgError(folder, file, linkRequestDto, "getLink 500 Error during jso out processing", null);
		}

		autorisationService.logMessage(file, "*********** End getLink() ************** ");
		logger.info("*********** End getLink() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/createtocken24", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String generateToken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String token24,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_TOKEN24_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start generateToken() ************** ");
		logger.info("*********** Start generateToken() ************** ");

		autorisationService.logMessage(file, "token24 api call start ...");
		autorisationService.logMessage(file, "token24 : [" + token24 + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(token24);
		} catch (JSONException jserr) {
			autorisationService.logMessage(file, "token24 500 malformed json expression " + token24 + jserr);
			JSONObject jso = new JSONObject();
			jso.put("statuscode", "17");
			jso.put("status", "token24 500 malformed json expression");
			return jso.toString();
		}

		String cx_user, cx_password, institution_id, cx_reason, error_msg, error_code, mac_value = "";
		try {
			// TODO: Merchant info
			cx_user = (String) jsonOrequest.get("cx_user");
			cx_password = (String) jsonOrequest.get("cx_password");
			cx_reason = (String) jsonOrequest.get("cx_reason");
			mac_value = (String) jsonOrequest.get("mac_value");
			institution_id = (String) jsonOrequest.get("institution_id");

		} catch (Exception jerr) {
			autorisationService.logMessage(file, "token24 500 malformed json expression " + token24 + Util.formatException(jerr));
			JSONObject jso = new JSONObject();
			jso.put("statuscode", "17");
			jso.put("status", "token24 500 malformed json expression");
			return jso.toString();
		}

		// TODO: pour tester la generation du tocken
		JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
		error_code = "";
		JSONObject jso = new JSONObject();
		JSONObject jsoVerified = new JSONObject();
		String token = "";

		if (mac_value.equals("")) {
			autorisationService.logMessage(file, "the token generation failed, mac_value is empty");

			jsoVerified.put("error_msg", "the token generation failed, mac_value is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			autorisationService.logMessage(file, "jsoVerified : " + jsoVerified.toString());
			logger.info("jsoVerified : " + jsoVerified.toString());

			autorisationService.logMessage(file, "*********** End generateToken() ************** ");

			return jsoVerified.toString();
		}
		if (cx_user.equals("")) {
			autorisationService.logMessage(file, "the token generation failed, cx_user is empty");

			jsoVerified.put("error_msg", "the token generation failed, cx_user is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			autorisationService.logMessage(file, "jsoVerified : " + jsoVerified.toString());
			logger.info("jsoVerified : " + jsoVerified.toString());

			autorisationService.logMessage(file, "*********** End generateToken() ************** ");

			return jsoVerified.toString();
		}
		if (cx_password.equals("")) {
			autorisationService.logMessage(file, "the token generation failed, cx_password is empty");

			jsoVerified.put("error_msg", "the token generation failed, cx_password is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			autorisationService.logMessage(file, "jsoVerified : " + jsoVerified.toString());
			logger.info("jsoVerified : " + jsoVerified.toString());

			autorisationService.logMessage(file, "*********** End generateToken() ************** ");

			return jsoVerified.toString();
		}
		if (institution_id.equals("")) {
			autorisationService.logMessage(file, "the token generation failed, institution_id is empty");

			jsoVerified.put("error_msg", "the token generation failed, institution_id is empty");
			jsoVerified.put("error_code", "17");
			jsoVerified.put("securtoken_24", "");
			jsoVerified.put("cx_user", cx_user);
			jsoVerified.put("mac_value", "");
			jsoVerified.put("institution_id", institution_id);
			autorisationService.logMessage(file, "jsoVerified : " + jsoVerified.toString());
			logger.info("jsoVerified : " + jsoVerified.toString());

			autorisationService.logMessage(file, "*********** End generateToken() ************** ");

			return jsoVerified.toString();
		}

		try {
			// TODO: generate by jwtTokenValidity configured in app properties
			token = jwtTokenUtil.generateToken(cx_user, cx_password, jwtTokenValidity);

			// TODO: verification expiration token
			jso = verifieToken(token, file);

			if (jso != null && !jso.get("statuscode").equals("00")) {
				jsoVerified.put("error_msg", "the token generation failed");
				jsoVerified.put("error_code", jso.get("statuscode"));
				jsoVerified.put("securtoken_24", "");
				jsoVerified.put("cx_user", cx_user);
				jsoVerified.put("mac_value", mac_value);
				jsoVerified.put("institution_id", institution_id);
				autorisationService.logMessage(file, "jsoVerified : " + jsoVerified.toString());
				logger.info("jsoVerified : " + jsoVerified.toString());
				autorisationService.logMessage(file, "*********** End generateToken() ************** ");
				logger.info("*********** End generateToken() ************** ");
				return jsoVerified.toString();
			} else {
				error_msg = "the token successfully generated";
				error_code = "00";
			}

		} catch (Exception ex) {
			autorisationService.logMessage(file, "the token generation failed");
			error_msg = "the token generation failed";
			error_code = "17";
		}

		jsoVerified.put("error_msg", error_msg);
		jsoVerified.put("error_code", error_code);
		jsoVerified.put("securtoken_24", token);
		jsoVerified.put("cx_user", cx_user);
		jsoVerified.put("mac_value", mac_value);

		autorisationService.logMessage(file, "json res : [" + jsoVerified.toString() + "]");
		logger.info("json res : [" + jsoVerified.toString() + "]");

		// TODO: fin
		logger.info("*********** End generateToken() ************** ");
		autorisationService.logMessage(file, "*********** End generateToken() ************** ");

		return jsoVerified.toString();
	}

	@RequestMapping(value = "/napspayment/chalenge/token/{token}", method = RequestMethod.GET)
	@SuppressWarnings("all")
	public String chalengeapi(@PathVariable(value = "token") String token, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CHALENGE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start chalengeapi ************** ");
		logger.info("*********** Start chalengeapi ************** ");

		String page = "chalenge";

		autorisationService.logMessage(file, "findByTokencommande token : " + token);
		logger.info("findByTokencommande token : " + token);

		DemandePaiementDto current_dem = demandePaiementService.findByTokencommande(token);
		String msgRefus = "An error has occurred, please try again later.";

		if (current_dem != null) {
			autorisationService.logMessage(file, "current_dem is exist OK");
			logger.info("current_dem is exist OK");
			if (current_dem.getEtatDemande().equals("SW_PAYE") || current_dem.getEtatDemande().equals("PAYE")) {
				msgRefus = "The current transaction has already been completed, your account will not be debited.";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else if (current_dem.getEtatDemande().equals("SW_REJET")) {
				msgRefus = "The current transaction was not successful, your account will not be debited, please try again.";
				current_dem.setMsgRefus(msgRefus);
				model.addAttribute("demandeDto", current_dem);
				page = "error";
			} else {
				page = "chalenge";

				if (current_dem.getCreq().equals("")) {
					msgRefus = "The current transaction was not successful, your account will not be debited, please try again.";
					current_dem.setMsgRefus(msgRefus);
					autorisationService.logMessage(file, "Le lien de chalence acs est null !!!");

					model.addAttribute("demandeDto", current_dem);
					page = "error";
				} else {
					logger.info("current_dem htmlCreq : " + current_dem.getCreq());
					autorisationService.logMessage(file, "current_dem htmlCreq : " + current_dem.getCreq());

					model.addAttribute("demandeDto", current_dem);

					try {
						// TODO: 2024-07-05
						// TODO: autre façon de faire la soumission automatique de formulaires ACS via le
						// TODO: HttpServletResponse.

						String creq = "";
						String acsUrl = "";
						String response3DS = current_dem.getCreq();
						//Pattern pattern = Pattern.compile("action='(.*?)'.*value='(.*?)'");
						Pattern pattern = Pattern.compile("action='([^']*)'.*?value='([^']*)'");
						Matcher matcher = pattern.matcher(response3DS);

						// TODO: Si une correspondance est trouvée
						if (matcher.find()) {
							acsUrl = matcher.group(1);
							creq = matcher.group(2);
							logger.info("L'URL ACS est : " + acsUrl);
							logger.info("La valeur de creq est : " + creq);
							autorisationService.logMessage(file, "L'URL ACS est : " + acsUrl);
							autorisationService.logMessage(file, "La valeur de creq est : " + creq);

							String decodedCreq = new String(Base64.decodeBase64(creq.getBytes()));
							logger.info("La valeur de decodedCreq est : " + decodedCreq);
							autorisationService.logMessage(file, "La valeur de decodedCreq est : " + decodedCreq);

							// TODO: URL de feedback après soumission ACS
							String feedbackUrl = request.getContextPath() + "/acsFeedback";

							// TODO: Afficher le formulaire HTML dans la réponse
							response.setContentType("text/html");
							response.setCharacterEncoding("UTF-8");
							response.getWriter().println("<html><body>");
							response.getWriter()
									.println("<form id=\"acsForm\" action=\"" + acsUrl + "\" method=\"post\">");
							response.getWriter()
									.println("<input type=\"hidden\" name=\"creq\" value=\"" + creq + "\">");
							response.getWriter().println("</form>");
							response.getWriter()
									.println("<script>document.getElementById('acsForm').submit();</script>");
							response.getWriter().println("</body></html>");

							System.out
									.println("Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");
							autorisationService.logMessage(file,
									"Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");

							return null; // TODO: Terminer le traitement ici après avoir envoyé le formulaire
						} else {
							logger.info(
									"Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
							autorisationService.logMessage(file,
									"Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
							page = "error"; // TODO: Définir la page d'erreur appropriée
						}

						// TODO: 2024-07-05
					} catch (Exception ex) {
						DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
						autorisationService.logMessage(file,
								"Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML " + Util.formatException(ex));
						demandeDtoMsg.setMsgRefus(
								"The current transaction was not successful, your account will not be debited, please try again.");
						model.addAttribute("demandeDto", demandeDtoMsg);
						current_dem.setDemCvv("");
						demandePaiementService.save(current_dem);
						page = "result";
						return page;
					}
				}
			}
		} else {
			DemandePaiementDto demande = new DemandePaiementDto();
			msgRefus = "Votre commande est introuvable ";
			demande.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demande);
			autorisationService.logMessage(file, "current_dem not found ");
			logger.info("current_dem null ");
			page = "error";
		}

		logger.info("return to " + page + ".html");

		autorisationService.logMessage(file, "*********** End chalengeapi ************** ");
		logger.info("*********** End chalengeapi ************** ");

		return page;
	}

	@PostMapping(value = "/napspayment/status", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String status(@RequestHeader MultiValueMap<String, String> header, @RequestBody String status,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_STATUS_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start status() ************** ");
		logger.info("*********** Start status() ************** ");

		autorisationService.logMessage(file, "status api call start ...");
		autorisationService.logMessage(file, "status : [" + status + "]");

		TransactionRequestDto trsRequestDto;

		try {
			trsRequestDto = new ObjectMapper().readValue(status, TransactionRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "status 500 malformed json expression " + status + Util.formatException(e));
			return Util.getMsgErrorV2(folder, file, null, "status 500 malformed json expression", null);
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		autorisationService.logMessage(file, "status_" + trsRequestDto.getOrderid() + timeStamp);

		String status_ = "Uknown";
		String statuscode_ = "06";

		DemandePaiementDto current_dmd = null;

		String dcurrent_dmd, dtpattern, tmpattern, respcode = "", s_respcode = "";
		SimpleDateFormat sfdt, sftm = null;
		Date datdem, datetlc = null;
		Character E = '\0';

		JSONObject jso = new JSONObject();
		// TODO: verification expiration token
		jso = verifieToken(trsRequestDto.getSecurtoken24(), file);
		if (!jso.get("statuscode").equals("00")) {
			autorisationService.logMessage(file, "jso : " + jso.toString());
			logger.info("jso : " + jso.toString());
			autorisationService.logMessage(file, "*********** End status() ************** ");
			logger.info("*********** End status() ************** ");
			return jso.toString();
		}

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid());

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"status 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err1));
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			autorisationService.logMessage(file, "status 500 PaiementRequest not found orderid:[" + trsRequestDto.getOrderid()
					+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		if (trsRequestDto.getAuthnumber() != null && trsRequestDto.getAuthnumber().length() < 1) {
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid());
			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"status 500 Error during HistoAutoGate findLastByHatNumCommandeAndHatNumcmr orderid:[" + trsRequestDto.getOrderid()
								+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Error during find HistoAutoGate", null);
			}
		} else {

			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmr(trsRequestDto.getOrderid(), trsRequestDto.getAuthnumber(),
						trsRequestDto.getMerchantid());

			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr orderid:[" + trsRequestDto.getOrderid()
								+ "] + and authnumber:[" + trsRequestDto.getAuthnumber() + "]" + "and merchantid:[" + trsRequestDto.getAuthnumber() + "]"
								+ Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Error during find HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			String dmd_etat = "";
			if (current_dmd.getEtatDemande() != null) {
				dmd_etat = current_dmd.getEtatDemande();
			}
			if (dmd_etat.equalsIgnoreCase("PAYE")) {
				autorisationService.logMessage(file,
						"Transaction not found for authnumber and DemandePaiement is PAYE status"
								+ "HistoAutoGate not found for authnumber:[" + trsRequestDto.getAuthnumber() + "] and merchantid:["
								+ trsRequestDto.getMerchantid() + "]");
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Transaction not found", null);
			} else {

				E = 'X';

			}
		}

		char pr = '\0';

		try {

			dcurrent_dmd = current_dmd.getDemDateTime();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			if (current_hist != null) {
				datdem = current_hist.getHatDatdem();
				datetlc = current_hist.getHatdatetlc();
				E = current_hist.getHatEtat();
				pr = current_hist.getHatProcode() == null ? '\0' : current_hist.getHatProcode();
				respcode = current_hist.getHatCoderep() == null ? "" : current_hist.getHatCoderep();
			} else {
				datdem = null;
				datetlc = null;
			}
			s_respcode = "";
		} catch (Exception err2) {
			autorisationService.logMessage(file, "status 500 Error during status processing for given authnumber"
					+ " :[" + trsRequestDto.getAuthnumber() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Error during status processing", null);
		}

		String sdt = "", stm = "", sdt1 = "", stm1 = "", sdt0 = "", stm0 = "";

		String rep_auto = "";
		if (current_hist != null)
			rep_auto = current_hist.getHatCoderep();

		if (E == 'E') {
			if (rep_auto.equalsIgnoreCase("00")) {
				if (current_dmd.getTransactiontype().equals("0")) {
					status_ = "Paid";
				} else if (current_dmd.getTransactiontype().equals("P")) {
					status_ = "Prepaid";
				} else {
					status_ = "Paid";
				}
				statuscode_ = "00";

				String spr = pr + "";
				if (spr.equalsIgnoreCase("4")) {
					status_ = "Refunded";
					statuscode_ = "07";
				}
			}

			if (!rep_auto.equalsIgnoreCase("00")) {
				status_ = "Declinded";
				statuscode_ = "01";

				String spr = pr + "";
				if (spr.equalsIgnoreCase("4")) {
					status_ = "Refund_Declined";
					statuscode_ = "08";
				}
			}
		} else if (E == 'X') {
			// TODO: E == 'X' if trs not approuved (repauto != 00)
			status_ = "Declinded : " + current_dmd.getEtatDemande();
			statuscode_ = "01";
		} else if (E == 'A') {
			// TODO: E == 'X' if trs canceled
			status_ = "Canceled";
			statuscode_ = "04";
		}

		String trs_state = "";
		TransactionDto trs_check = null;
		List<TransactionDto> listTrs_check = null;

		if (E == 'T') {
			listTrs_check = transactionService.findListByTrsNumautAndTrsNumcmrAndTrsCommande(trsRequestDto.getAuthnumber(), trsRequestDto.getMerchantid(),
					trsRequestDto.getOrderid());

			if(listTrs_check != null && listTrs_check.size() > 0) {
				autorisationService.logMessage(file,"listTrs_check nbrs : " + listTrs_check.size());
				trs_check = listTrs_check.get(0);
			} else {
				try {
					trs_check = transactionService.findByTrsnumautAndTrsnumcmrAndTrscommande(trsRequestDto.getAuthnumber(), trsRequestDto.getMerchantid(),
							trsRequestDto.getOrderid());

				} catch (Exception err4) {
					autorisationService.logMessage(file,
							"status 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber" + " :["
									+ trsRequestDto.getAuthnumber() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err4));

					return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Error during Transaction", null);
				}
			}

			if (trs_check != null) {
				// TODO: E == 'T' if trs captured
				status_ = "Captured";
				statuscode_ = "02";

				trs_state = trs_check.getTrsEtat();

				if (trs_state.equalsIgnoreCase("N")) {
					status_ = "Captured";
					statuscode_ = "02";
				}

				if (trs_state.equalsIgnoreCase("E")) {
					status_ = "Settled";
					statuscode_ = "03";
				}
			}
			if (trs_state == null) {
				status_ = "Uknown";
				statuscode_ = "06";
			}
		}

		String dmd_status = current_dmd.getEtatDemande();
		if (dmd_status.equalsIgnoreCase("R")) {
			status_ = "Refunded";
			statuscode_ = "07";

		}

		try {

			jso.put("status", status_);
			// TODO: jso.put("status", "Declined");
			// TODO: jso.put("status", "Captured");
			// TODO: jso.put("status", "Settled");
			// TODO: jso.put("status", "NotExist");
			// TODO: jso.put("status", "Canceled");
			jso.put("statuscode", statuscode_);
			// TODO: jso.put("statuscode", "01");
			// TODO: jso.put("statuscode", "02");
			// TODO: jso.put("statuscode", "03");
			// TODO: jso.put("statuscode", "04");
			// TODO: jso.put("statuscode", "05");

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

					SimpleDateFormat parser = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
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
			jso.put("authnumber", trsRequestDto.getAuthnumber());
			jso.put("transactionid", trsRequestDto.getTransactionid());
			jso.put("merchantid", trsRequestDto.getMerchantid());

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
			logger.info("json res : [" + jso.toString() + "]");

		} catch (Exception err3) {
			// TODO: TODO: err3.printStackTrace();
			autorisationService.logMessage(file, "status 500 Error during jso out processing for given authnumber "
					+ " :[" + trsRequestDto.getAuthnumber() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err3));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "status 500 Error during jso out processing", null);
		}

		autorisationService.logMessage(file, "*********** End status() ************** ");
		logger.info("*********** End status() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/capture", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String capture(@RequestHeader MultiValueMap<String, String> header, @RequestBody String capture,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CAPTURE_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start capture() ************** ");
		logger.info("*********** Start capture() ************** ");

		autorisationService.logMessage(file, "capture api call start ...");
		autorisationService.logMessage(file, "capture : [" + capture + "]");

		TransactionRequestDto trsRequestDto;

		try {
			trsRequestDto = new ObjectMapper().readValue(capture, TransactionRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "capture 500 malformed json expression " + capture + Util.formatException(e));
			return Util.getMsgErrorV2(folder, file, null, "capture 500 malformed json expression", null);
		}

		String websiteid, cardnumber = "";

		JSONObject jso = new JSONObject();
		// TODO: verification expiration token
		jso = verifieToken(trsRequestDto.getSecurtoken24(), file);
		if (!jso.get("statuscode").equals("00")) {
			autorisationService.logMessage(file, "jso : " + jso.toString());
			logger.info("jso : " + jso.toString());
			autorisationService.logMessage(file, "*********** End capture() ************** ");
			logger.info("*********** End capture() ************** ");
			return jso.toString();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		autorisationService.logMessage(file, "capture_" + trsRequestDto.getOrderid() + timeStamp);
		// TODO: get demandepaiement id , check if exist

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid());

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"capture 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err1));
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			autorisationService.logMessage(file, "captue 500 PaiementRequest not found for orderid:[" + trsRequestDto.getOrderid()
					+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "captue 500 PaiementRequest not found", null);
		}

		if (current_dmd.getEtatDemande() != null) {
			if (current_dmd.getEtatDemande().equals("P")) {
				return Util.getMsgErrorV2(folder, file, trsRequestDto,
						"capture 500, use .../cpautorisation api for capture pre-auto", null);
			}
		}

		// TODO: get histoauto check if exist

		HistoAutoGateDto current_hist = null;

		if(trsRequestDto.getAuthnumber() == null || trsRequestDto.getAuthnumber().equals("")) {
			cardnumber = current_dmd.getDemPan();
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmrAndHatPorteur(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid(), cardnumber);
			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"capture 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrAndHatPorteur orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during HistoAutoGate", null);
			}
		} else {
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(trsRequestDto.getOrderid(),
						trsRequestDto.getAuthnumber(), trsRequestDto.getMerchantid(), "00");

			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"capture 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			autorisationService.logMessage(file,
					"capture 500 Transaction not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + trsRequestDto.getAuthnumber() + "] and merchantid:["
							+ trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Transaction not found", null);
		}

		if (current_hist.getHatEtat().equals('A') || current_hist.getHatEtat() == 'A') {
			autorisationService.logMessage(file,
					"You can't make the capture because this transaction is already cancelled");
			return Util.getMsgErrorV2(folder, file, null,
					"You can't make the capture because this transaction is already cancelled", null);
		}

		if (current_hist.getHatEtat().equals('T') || current_hist.getHatEtat() == 'T') {
			autorisationService.logMessage(file,
					"You can't make the cature because this transaction is already captured");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You can't make the capture because this transaction is already captured", null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(trsRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"capture 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		// TODO: check if already telecollected

		TransactionDto trs_check = null;

		/*
		 * try { trs_check =
		 * transactionService.findByTrsnumautAndTrsnumcmrAndTrscommande(authnumber,
		 * merchantid, orderid); } catch (Exception err4) {
		 * autorisationService.logMessage(file,
		 * "capture 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber:["
		 * + authnumber + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "capture 500 Error during Transaction", null); }
		 * 
		 * if (trs_check != null) { autorisationService.logMessage(file,
		 * "capture 500 Transaction already captured  for given " + "authnumber:[" +
		 * authnumber + "] and merchantid:[" + merchantid + "]");
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "500 Transaction already captured", null); }
		 */

		Double montantAuto = 0.00;
		Double montantCapture = 0.00;
		Double montantReste = 0.00;
		Double totalMontantCapture = 0.00;
		Double totalMontantAnnul = 0.00;

		try {
			montantAuto = current_hist.getHatMontant();
			montantCapture = Double.valueOf(trsRequestDto.getAmount());
			totalMontantCapture = current_hist.getHatMontantCapture() == null ? 0.00
					: current_hist.getHatMontantCapture() + montantCapture;
			totalMontantAnnul = current_hist.getHatMontantAnnul() == null ? 0.00 : current_hist.getHatMontantAnnul();
			montantReste = montantAuto - totalMontantCapture - totalMontantAnnul;
			autorisationService.logMessage(file, "totalMontantAnnul : " + totalMontantAnnul);
			autorisationService.logMessage(file, "totalMontantCapture : " + totalMontantCapture);
			autorisationService.logMessage(file, "montantReste : " + montantReste);
		} catch (Exception err3) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during date formatting", null);
		}

		// TODO: 2024-11-27 controle sur le montant à telecollecter

		if (montantCapture > montantAuto) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You can't make the capture because the amount sent is greater than the transaction amount.", null);
		}

		if (montantReste == 0.00 && current_hist.getHatEtat().equals('T')) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Transaction already captured", null);
		}

		if (montantReste < 0.00) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You cannot capture because the amount sent is greater than the uncaptured amount of the transaction.",
					null);
		}

		try {
			trsRequestDto.setAuthnumber(current_hist.getHatNautemt());
			cardnumber = current_dmd.getDemPan();
			websiteid = trsRequestDto.getWebsiteid() == null ? "" : trsRequestDto.getWebsiteid();
			if (websiteid.equals("") || websiteid == null) {
				websiteid = current_dmd.getGalid();
			}
			if (!websiteid.equals(current_dmd.getGalid())) {
				websiteid = current_dmd.getGalid();
			}
		} catch (Exception err3) {
			autorisationService.logMessage(file,
					"capture 500 Error during cardnumber formatting for given orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err3));
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during cardnumber formatting", null);
		}

		Date current_date = null;
		current_date = new Date();
		long lidtelc = 0;
		Integer idtelc = null;
		TelecollecteDto tlc = null;

		try {
			TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(trsRequestDto.getMerchantid());
			if (n_tlc == null) {
				autorisationService.logMessage(file, "getMAXTLC_N n_tlc = null");
				idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
				autorisationService.logMessage(file, "getMAX_ID idtelc : " + idtelc);

				if (idtelc != null) {
					lidtelc = idtelc.longValue() + 1;
				} else {
					lidtelc = 1;
				}
				tlc = new TelecollecteDto();
				tlc.setTlcNumtlcolcte(lidtelc);
				tlc.setTlcNumtpe(current_hist.getHatCodtpe());

				tlc.setTlcDatcrtfich(current_date);
				tlc.setTlcNbrtrans(new Double(1));
				tlc.setTlcGest("N");

				tlc.setTlcDatremise(current_date);
				tlc.setTlcNumremise(new Double(lidtelc));
				// TODO: tlc.setTlc_numfich(new Double(0));
				String tmpattern = "HH:mm";
				SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
				String stm = sftm.format(current_date);
				tlc.setTlcHeuremise(stm);

				tlc.setTlcCodbq(acqcode);
				tlc.setTlcNumcmr(trsRequestDto.getMerchantid());
				tlc.setTlcNumtpe(websiteid);

				autorisationService.logMessage(file, tlc.toString());

				telecollecteService.save(tlc);
			} else {
				lidtelc = n_tlc.getTlcNumtlcolcte();
				double nbr_trs = n_tlc.getTlcNbrtrans();
				autorisationService.logMessage(file, "n_tlc !=null lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
				nbr_trs = nbr_trs + 1;
				n_tlc.setTlcNbrtrans(nbr_trs);
				autorisationService.logMessage(file, "increment lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
				telecollecteService.save(n_tlc);
			}

		} catch (DataIntegrityViolationException ex) {
			autorisationService.logMessage(file,"Conflit détecté lors de l'insertion de telecollecte, première tentative échouée." + Util.formatException(ex));
			autorisationService.logMessage(file,"Pause de 2 secondes avant la deuxième tentative");

			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				autorisationService.logMessage(file,"Thread interrompu pendant le délai d'attente" + ie);
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 , operation failed please try again", null);
			}

			try {
				idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
				if (idtelc != null) {
					lidtelc = idtelc + 1;
				} else {
					lidtelc = 1;
				}
				autorisationService.logMessage(file,"Deuxième tentative lidtelc : " + lidtelc);
				tlc.setTlcNumtlcolcte(lidtelc);

				autorisationService.logMessage(file, tlc.toString());

				telecollecteService.save(tlc);

			} catch (DataIntegrityViolationException ex2) {
				autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + Util.formatException(ex2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 , operation failed please try again", null);
			}
		} catch (Exception err5) {
			autorisationService.logMessage(file,
					"capture 500 Error during insert into telec for given authnumber:[" + trsRequestDto.getAuthnumber()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err5));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 , operation failed please try again", null);
		}
		/*
		 * } else { lidtelc = n_tlc.getTlc_numtlcolcte(); double nbr_trs =
		 * n_tlc.getTlc_nbrtrans(); nbr_trs = nbr_trs + 1;
		 * n_tlc.setTlcNbrtrans(nbr_trs); try { telecollecteService.save(n_tlc); }
		 * catch (Exception err55) { autorisationService.logMessage(file,
		 * "capture 500 Error during update telec for given authnumber:[" + authnumber +
		 * "] and merchantid:[" + merchantid + "]" + Util.formatException(err55));
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "capture 500 Error during update telecollecte", null); } }
		 */

		TransactionDto trs = null;
		String frmt_cardnumber = "";
		double dmnt = 0;
		Integer idtrs = null;
		long lidtrs = 0;

		// TODO: insert into transaction
		try {
			trs = new TransactionDto();
			trs.setTrsNumcmr(trsRequestDto.getMerchantid());
			trs.setTrsNumtlcolcte(Double.valueOf(lidtelc));
			frmt_cardnumber = Util.formatagePan(cardnumber);
			trs.setTrsCodporteur(frmt_cardnumber);
			dmnt = Double.parseDouble(trsRequestDto.getAmount());
			trs.setTrsMontant(dmnt);
			current_date = new Date();
			Date current_date_1 = getDateWithoutTime(current_date);
			Date trs_date = dateFormatSimple.parse(current_dmd.getDemDateTime());
			Date trs_date_1 = getDateWithoutTime(trs_date);

			trs.setTrsDattrans(current_date_1);
			trs.setTrsNumaut(trsRequestDto.getAuthnumber());
			trs.setTrsEtat("N");
			trs.setTrsDevise(current_hist.getHatDevise());
			trs.setTrsCertif("N");
			idtrs = transactionService.getMAX_ID();
			lidtrs = idtrs.longValue() + 1;
			trs.setTrsId(lidtrs);
			trs.setTrsCommande(trsRequestDto.getOrderid());

			trs.setTrsProcod("0");
			trs.setTrsGroupe(websiteid);
			trs.setTrsCodtpe(0.0);
			trs.setTrsNumbloc(0.0);
			trs.setTrsNumfact(0.0);
			transactionService.save(trs);

		} catch (Exception e) {
			autorisationService.logMessage(file,
					"capture 500 Error during insert into transaction for given authnumber:[" + trsRequestDto.getAuthnumber()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 , operation failed please try again", null);
		}

		try {
			// TODO: 2024-11-27 controle sur le montant à telecollecter
			if (montantReste > 0.00) {
				current_hist.setHatMontantCapture(totalMontantCapture);
				current_hist.setHatdatetlc(current_date);
				current_hist.setOperateurtlc("mxplusapi");
				// TODO: on garde etat = E initial
				histoAutoGateService.save(current_hist);
			} else if (montantReste == 0.00) {
				current_hist.setHatMontantCapture(totalMontantCapture);
				current_hist.setHatEtat('T');
				current_hist.setHatdatetlc(current_date);
				current_hist.setOperateurtlc("mxplusapi");
				current_hist = histoAutoGateService.save(current_hist);
			}

		} catch (Exception err7) {
			autorisationService.logMessage(file,
					"capture 500 Error during histoauto_gate update for given authnumber:[" + trsRequestDto.getAuthnumber()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err7));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during histoauto_gate update", null);
		}

		String capture_id = "", dtpattern, sdt = "", tmpattern, stm = "", uuid_captureid = "", operation_id ="";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;

		try {
			uuid_captureid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			capture_id = uuid_captureid.substring(uuid_captureid.length() - 22);
			operation_id = capture_id;
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
		} catch (Exception err8) {
			autorisationService.logMessage(file,
					"capture 500 Error during jso data preparationfor given authnumber:[" + trsRequestDto.getAuthnumber()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err8));
		}

		try {
			jso.put("orderid", trsRequestDto.getAuthnumber());
			jso.put("amount", trsRequestDto.getAmount());
			jso.put("capturedate", sdt);
			jso.put("capturetime", stm);
			jso.put("capture_id", capture_id);
			jso.put("capture_state", "Y");
			jso.put("capture_label", "Captured");
			jso.put("transactionid", trsRequestDto.getTransactionid());
			jso.put("operation_id", operation_id);
			jso.put("acquRefNbr", "11010");

			jso.put("merchantid", trsRequestDto.getMerchantid());
			jso.put("merchantname", trsRequestDto.getMerchantname());
			jso.put("websitename", trsRequestDto.getWebsiteName());
			jso.put("websiteid", websiteid);
			jso.put("cardnumber", Util.formatCard(trsRequestDto.getCardnumber()));
			jso.put("fname", trsRequestDto.getFname());
			jso.put("lname", trsRequestDto.getLname());
			jso.put("email", trsRequestDto.getEmail());

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
			logger.info("json res : [" + jso.toString() + "]");

		} catch (Exception err9) {
			autorisationService.logMessage(file, "capture 500 Error during jso out processing given "
					+ "authnumber:[" + trsRequestDto.getAuthnumber() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err9));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "capture 500 Error during jso out processing", null);
		}

		autorisationService.logMessage(file, "*********** End capture() ************** ");
		logger.info("*********** End capture() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/refund", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String refund(@RequestHeader MultiValueMap<String, String> header, @RequestBody String refund,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_REFUND_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start refund() ************** ");
		logger.info("*********** Start refund() ************** ");

		autorisationService.logMessage(file, "refund api call start ...");
		autorisationService.logMessage(file, "refund : [" + refund + "]");

		TransactionRequestDto trsRequestDto;

		try {
			trsRequestDto = new ObjectMapper().readValue(refund, TransactionRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "refund 500 malformed json expression " + refund + Util.formatException(e));
			return Util.getMsgErrorV2(folder, file, null, "refund 500 malformed json expression", null);
		}

		String websiteid, cardnumber = "";

		JSONObject jso = new JSONObject();
		// TODO: verification expiration token
		jso = verifieToken(trsRequestDto.getSecurtoken24(), file);
		if (!jso.get("statuscode").equals("00")) {
			autorisationService.logMessage(file, "jso : " + jso.toString());
			logger.info("jso : " + jso.toString());
			autorisationService.logMessage(file, "*********** End refund() ************** ");
			logger.info("*********** End refund() ************** ");
			return jso.toString();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		autorisationService.logMessage(file, "refund_" + trsRequestDto.getOrderid() + timeStamp);

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid());
		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"refund 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err1));
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Error during PaiementRequest", null);
		}

		if (current_dmd == null) {
			autorisationService.logMessage(file, "refund 500 PaiementRequest not found for given orderid" + trsRequestDto.getOrderid()
					+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Transaction not found", null);
		}

		HistoAutoGateDto current_hist = null;

		if(trsRequestDto.getAuthnumber() == null || trsRequestDto.getAuthnumber().equals("")) {
			cardnumber = current_dmd.getDemPan();
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmrAndHatPorteur(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid(), cardnumber);
			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"refund 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrAndHatPorteur orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Error during HistoAutoGate", null);
			}
		} else {
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(trsRequestDto.getOrderid(),
						trsRequestDto.getAuthnumber(), trsRequestDto.getMerchantid(), "00");

			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"refund 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Error during HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			autorisationService.logMessage(file, "refund 500 Transaction not found for authnumber:[" + trsRequestDto.getAuthnumber()
					+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Transaction not found", null);
		}

		if (current_hist.getHatEtat().equals('A') || current_hist.getHatEtat() == 'A') {
			autorisationService.logMessage(file, "refund 500 Transaction is already cancelled for authnumber:["
					+ trsRequestDto.getAuthnumber() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Transaction is already canceled", null);
		}

		TransactionDto trs_check = null;

		/*
		 * try { trs_check =
		 * transactionService.findByTrsnumautAndTrsnumcmrAndTrscommande(authnumber,
		 * merchantid, orderid); } catch (Exception err4) {
		 * autorisationService.logMessage(file,
		 * "refund 500 Error during Transaction findByTrsnumautAndTrsnumcmr for given authnumber:["
		 * + authnumber + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "refund 500 Error during Transaction", null); }
		 * 
		 * if (trs_check == null) { autorisationService.logMessage(file,
		 * "refund 500 Transaction not found for authnumber:[" + authnumber +
		 * "] and merchantid:[" + merchantid + "]");
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "refund 500 Transaction not found", null); }
		 * 
		 * String trs_procod = trs_check.getTrs_procod(); String trs_state =
		 * trs_check.getTrsEtat();
		 * 
		 * if (trs_procod == null) { autorisationService.logMessage(file,
		 * "refund 500 Transaction trs_procod null for authnumber:[" + authnumber +
		 * "] and merchantid:[" + merchantid + "]");
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "refund 500 Transaction trs_procod null", null); }
		 * 
		 * if (trs_state == null) { autorisationService.logMessage(file,
		 * "refund 500 Transaction trs_procod null for authnumber:[" + authnumber +
		 * "] and merchantid:[" + merchantid + "]");
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "refund 500 Transaction trs_procod null", null); }
		 * 
		 * if (!trs_procod.equalsIgnoreCase("0")) { Util.writeInFileTransaction(folder,
		 * file, "refund 500 Transaction trs_procod <> 0 for authnumber:[" + authnumber
		 * + "] and merchantid:[" + merchantid + "]");
		 * 
		 * return Util.getMsgErrorV2(folder, file, trsRequestDto,
		 * "refund 500 Transaction trs_procod <> 0", null); }
		 */

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";
		Double montantAuto = 0.00;
		Double montantRefund = 0.00;
		String numcarteTrs = "";
		Double montantReste = 0.00;
		Double totalMontantRefund = 0.00;

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat(FORMAT_DEFAUT);
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";
			websiteid = trsRequestDto.getWebsiteid() == null ? "" : trsRequestDto.getWebsiteid();
			if (websiteid.equals("") || websiteid == null) {
				websiteid = current_dmd.getGalid();
			}
			if (!websiteid.equals(current_dmd.getGalid())) {
				websiteid = current_dmd.getGalid();
			}
			trsRequestDto.setAuthnumber(current_hist.getHatNautemt());
			numcarteTrs = current_dmd.getDemPan();
			numcarteTrs = numcarteTrs.replace("???", "");
			cardnumber = trsRequestDto.getCardnumber() == null ? "" : trsRequestDto.getCardnumber();
			cardnumber = cardnumber.replace("???", "");
			if (!cardnumber.equals("") && cardnumber.length() <= 16) {
				if (!cardnumber.trim().equals(numcarteTrs.trim())) {
					return Util.getMsgErrorV2(folder, file, trsRequestDto,
							"refund 500 the card sent for the refund is not the same as the transaction.", null);
				}
			} else {
				cardnumber = current_dmd.getDemPan();
			}

			montantAuto = current_hist.getHatMontant();
			montantRefund = Double.valueOf(trsRequestDto.getAmount());
			totalMontantRefund = current_hist.getHatMontantRefund() == null ? 0.00
					: current_hist.getHatMontantRefund() + montantRefund;
			montantReste = montantAuto - totalMontantRefund;
			autorisationService.logMessage(file, "totalMontantRefund : " + totalMontantRefund);
			autorisationService.logMessage(file, "montantReste : " + montantReste);
		} catch (Exception err3) {
			autorisationService.logMessage(file, "refund 500 Error during date formatting for given orderid:["
					+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err3));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Error during date formatting", null);
		}

		if (montantRefund > montantAuto) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"refund 500 the amount sent is greater than the transaction amount.", null);
		}

		if (montantReste < 0.00) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You cannot make the refund because the amount sent is greater than the outstanding amount of the transaction.",
					null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(trsRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid() + "] and merchantid:["
							+ trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid() + "] and merchantid:["
							+ trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid() + "] and merchantid:["
							+ trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"refund 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid() + "] and merchantid:["
							+ trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		// TODO: offline processing

		String tag20_resp = "00"; // TODO: Accept all refund offline mode
		String s_status = "Refunded offline";

		autorisationService.logMessage(file, "Switch status : [" + s_status + "]");

		if (tag20_resp.equalsIgnoreCase("00")) {
			autorisationService.logMessage(file, "Switch CODE REP : [00]");

			autorisationService.logMessage(file, "inserting Telecollecte   ...");

			Date current_date = null;
			current_date = new Date();
			long lidtelc = 0;
			Integer idtelc = null;

			TelecollecteDto tlc = null;

			try {
				TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(trsRequestDto.getMerchantid());
				if (n_tlc == null) {
					autorisationService.logMessage(file, "getMAXTLC_N n_tlc = null");
					// TODO: insert into telec
					idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
					autorisationService.logMessage(file, "getMAX_ID idtelc : " + idtelc);

					if (idtelc != null) {
						lidtelc = idtelc.longValue() + 1;
					} else {
						lidtelc = 1;
					}

					tlc = new TelecollecteDto();
					tlc.setTlcNumtlcolcte(lidtelc);
					tlc.setTlcNumtpe(current_hist.getHatCodtpe());
					// TODO: tlc.setTlc_typentre("REFUND");
					tlc.setTlcDatcrtfich(current_date);
					tlc.setTlcNbrtrans(new Double(1));
					tlc.setTlcGest("N");

					tlc.setTlcDatremise(current_date);
					tlc.setTlcNumremise(new Double(lidtelc));
					// TODO: tlc.setTlc_numfich(new Double(0));
					String tmpattern = "HH:mm";
					SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
					String stm = sftm.format(current_date);
					tlc.setTlcHeuremise(stm);
					String acqcode = current_merchant.getCmrCodbqe();
					tlc.setTlcCodbq(acqcode);
					tlc.setTlcNumcmr(trsRequestDto.getMerchantid());
					tlc.setTlcNumtpe(websiteid);

					autorisationService.logMessage(file, tlc.toString());

					telecollecteService.save(tlc);

					autorisationService.logMessage(file, "inserting Telecollecte  OK.");

				} else {
					lidtelc = n_tlc.getTlcNumtlcolcte();
					double nbr_trs = n_tlc.getTlcNbrtrans();
					autorisationService.logMessage(file, "n_tlc !=null lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
					nbr_trs = nbr_trs + 1;
					n_tlc.setTlcNbrtrans(nbr_trs);
					autorisationService.logMessage(file, "increment lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
					telecollecteService.save(n_tlc);
				}

			} catch (DataIntegrityViolationException ex) {
				autorisationService.logMessage(file,"Conflit détecté lors de l'insertion de telecollecte, première tentative échouée." + Util.formatException(ex));
				autorisationService.logMessage(file,"Pause de 2 secondes avant la deuxième tentative");

				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					autorisationService.logMessage(file,"Thread interrompu pendant le délai d'attente" + ie);
					return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 , operation failed please try again", null);
				}

				try {
					idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
					if (idtelc != null) {
						lidtelc = idtelc + 1;
					} else {
						lidtelc = 1;
					}
					autorisationService.logMessage(file,"Deuxième tentative lidtelc : " + lidtelc);
					tlc.setTlcNumtlcolcte(lidtelc);

					autorisationService.logMessage(file, tlc.toString());

					telecollecteService.save(tlc);

				} catch (DataIntegrityViolationException ex2) {
					autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + Util.formatException(ex2));
					return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 , operation failed please try again", null);
				}
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"refund 500 Error during  Telecollecte insertion or Transaction insertion for given orderid:["
								+ trsRequestDto.getOrderid() + "]" + Util.formatException(e));

				return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500, the refund failed", null);
			}

			TransactionDto trs = null;
			String frmt_cardnumber = "";
			double dmnt = 0;
			Integer idtrs = null;
			long lidtrs = 0;
			try {
				// TODO: insert into transaction
				autorisationService.logMessage(file, "inserting Transaction   ...");
				trs = new TransactionDto();
				trs.setTrsNumcmr(trsRequestDto.getMerchantid());
				trs.setTrsNumtlcolcte(Double.valueOf(lidtelc));
				frmt_cardnumber = Util.formatagePan(cardnumber);
				trs.setTrsCodporteur(frmt_cardnumber);
				dmnt = Double.parseDouble(trsRequestDto.getAmount());
				trs.setTrsMontant(dmnt);
				current_date = new Date();
				Date current_date_1 = getDateWithoutTime(current_date);
				Date trs_date = dateFormatSimple.parse(current_dmd.getDemDateTime());
				Date trs_date_1 = getDateWithoutTime(trs_date);

				trs.setTrsDattrans(current_date_1);
				trs.setTrsNumaut("000000");// TODO: offline mode
				trs.setTrsEtat("N");
				trs.setTrsDevise(current_hist.getHatDevise());
				trs.setTrsCertif("N");
				idtrs = transactionService.getMAX_ID();

				if (idtrs != null) {
					lidtrs = idtrs.longValue() + 1;
				} else {
					lidtrs = 1;
				}

				trs.setTrsId(lidtrs);
				trs.setTrsCommande(trsRequestDto.getOrderid());
				trs.setTrsProcod("9");
				trs.setTrsGroupe(websiteid);
				trs.setTrsCodtpe(0.0);
				trs.setTrsNumbloc(0.0);
				trs.setTrsNumfact(0.0);
				transactionService.save(trs);

				autorisationService.logMessage(file, "inserting Transaction  OK.");
			} catch(Exception e) {
				autorisationService.logMessage(file,
						"refund 500 Error during insert into transaction for given authnumber:[" + trsRequestDto.getAuthnumber()
								+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

				return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 , operation failed please try again", null);
			}

			try {
				autorisationService.logMessage(file, "Setting DemandePaiement status R ...");
				current_dmd.setEtatDemande("R");
				demandePaiementService.save(current_dmd);

				// TODO: 2024-11-27 controle sur le montant à rembourser
				if (montantReste > 0.00) {
					current_hist.setHatMontantRefund(totalMontantRefund);
					current_hist.setOperateurtlc("mxplusapi");
					// TODO: on garde etat = E initial
					histoAutoGateService.save(current_hist);
				} else if (montantReste == 0.00) {
					current_hist.setHatMontantRefund(totalMontantRefund);
					current_hist.setHatEtat('T');
					current_hist.setOperateurtlc("mxplusapi");
					current_hist = histoAutoGateService.save(current_hist);
				}

			} catch (Exception e) {
				autorisationService.logMessage(file,
						"refund 500 Error during  demandepaiement update  R for given  orderid:[" + trsRequestDto.getOrderid() + "]" + Util.formatException(e));

				return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500, the refund failed", null);
			}

			autorisationService.logMessage(file, "Setting DemandePaiement status R OK.");

			autorisationService.logMessage(file, "Transaction refunded.");

		}

		String refund_id = "", dtpattern, sdt = "", tmpattern, stm = "", uuid_refundid = "", operation_id = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;

		try {
			uuid_refundid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			refund_id = uuid_refundid.substring(uuid_refundid.length() - 22);
			operation_id = refund_id;
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"refund 500 Error during  refund_id generation for given orderid:[" + trsRequestDto.getOrderid() + "]" + Util.formatException(e));
		}

		try {
			// TODO: Transaction info
			jso.put("statuscode", tag20_resp);
			jso.put("status", s_status);
			jso.put("orderid", trsRequestDto.getOrderid());
			jso.put("amount", trsRequestDto.getAmount());
			jso.put("refunddate", sdt);
			jso.put("refundtime", stm);
			jso.put("authnumber", trsRequestDto.getAuthnumber());
			jso.put("refundid", refund_id);
			jso.put("transactionid", trsRequestDto.getTransactionid());
			jso.put("operation_id", operation_id);
			jso.put("acquRefNbr", "11010");

			// TODO: Merchant info
			jso.put("merchantid", trsRequestDto.getMerchantid());
			jso.put("merchantname", trsRequestDto.getMerchantname());
			jso.put("websitename", trsRequestDto.getWebsiteName());
			jso.put("websiteid", trsRequestDto.getWebsiteid());
			jso.put("cardnumber", Util.formatCard(trsRequestDto.getCardnumber()));

			// TODO: Client info
			jso.put("fname", trsRequestDto.getFname());
			jso.put("lname", trsRequestDto.getLname());
			jso.put("email", trsRequestDto.getEmail());

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
			logger.info("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			autorisationService.logMessage(file, "refund 500 Error during jso out processing given authnumber"
					+ "authnumber:[" + trsRequestDto.getAuthnumber() + "]" + Util.formatException(err8));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "refund 500 Error during jso out processing", null);
		}

		autorisationService.logMessage(file, "*********** End refund() ************** ");
		logger.info("*********** End refund() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/reversal", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String reversal(@RequestHeader MultiValueMap<String, String> header, @RequestBody String reversal,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_REVERSAL_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start reversal() ************** ");
		logger.info("*********** Start reversal() ************** ");

		autorisationService.logMessage(file, "reversal api call start ...");
		autorisationService.logMessage(file, "reversal : [" + reversal + "]");

		TransactionRequestDto trsRequestDto;

		try {
			trsRequestDto = new ObjectMapper().readValue(reversal, TransactionRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "reversal 500 malformed json expression " + reversal + Util.formatException(e));
			return Util.getMsgErrorV2(folder, file, null, "reversal 500 malformed json expression", null);
		}

		String cardnumber = "";

		JSONObject jso = new JSONObject();
		// TODO: verification expiration token
		jso = verifieToken(trsRequestDto.getSecurtoken24(), file);
		if (!jso.get("statuscode").equals("00")) {
			autorisationService.logMessage(file, "jso : " + jso.toString());
			logger.info("jso : " + jso.toString());
			autorisationService.logMessage(file, "*********** End reversal() ************** ");
			logger.info("*********** End reversal() ************** ");
			return jso.toString();
		}

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		autorisationService.logMessage(file, "reversal_" + trsRequestDto.getOrderid() + timeStamp);
		// TODO: get demandepaiement id , check if exist

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid());
		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"reversal 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err1));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during PaiementRequest", null);
		}
		if (current_dmd == null) {
			autorisationService.logMessage(file, "reversal 500 PaiementRequest not found for given orderid:["
					+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 PaiementRequest not found", null);
		}

		HistoAutoGateDto current_hist = null;

		if(trsRequestDto.getAuthnumber() == null || trsRequestDto.getAuthnumber().equals("")) {
			cardnumber = current_dmd.getDemPan();
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmrAndHatPorteur(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid(), cardnumber);
			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"reversal 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrAndHatPorteur orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during HistoAutoGate", null);
			}
		} else {
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(trsRequestDto.getOrderid(),
						trsRequestDto.getAuthnumber(), trsRequestDto.getMerchantid(), "00");

			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"reversal 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			autorisationService.logMessage(file,
					"reversal 500 Transaction not found for authnumber and DemandePaiement is PAYE status"
							+ "HistoAutoGate not found for authnumber:[" + trsRequestDto.getAuthnumber() + "] and merchantid:["
							+ trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Transaction not found", null);
		}

		if (current_hist.getHatEtat().equals('T') || current_hist.getHatEtat() == 'T') {
			autorisationService.logMessage(file,
					"You can't make the cancel because this transaction is already captured");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You can't make the cancel because this transaction is already captured", null);
		}
		
		if (current_hist.getHatEtat().equals('A') || current_hist.getHatEtat() == 'A') {
			autorisationService.logMessage(file,
					"You can't make the cancel because this transaction is already cancelled");
			return Util.getMsgErrorV2(folder, file, null,
					"You can't make the cancel because this transaction is already cancelled", null);
		}

		Double montantAuto = 0.00;
		Double montantAnnul = 0.00;
		Double montantReste = 0.00;
		Double totalMontantAnnul = 0.00;
		Double totalMontantCapture = 0.00;
		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";
			trsRequestDto.setAuthnumber(current_hist.getHatNautemt());
			cardnumber = current_dmd.getDemPan();
			montantAuto = current_hist.getHatMontant();
			montantAnnul = Double.valueOf(trsRequestDto.getAmount());
			totalMontantAnnul = current_hist.getHatMontantAnnul() == null ? 0.00
					: current_hist.getHatMontantAnnul() + montantAnnul;
			totalMontantCapture = current_hist.getHatMontantCapture() == null ? 0.00
					: current_hist.getHatMontantCapture();
			montantReste = montantAuto - totalMontantAnnul - totalMontantCapture;
			autorisationService.logMessage(file, "totalMontantAnnul : " + totalMontantAnnul);
			autorisationService.logMessage(file, "totalMontantCapture : " + totalMontantCapture);
			autorisationService.logMessage(file, "montantReste : " + montantReste);
		} catch (Exception err3) {
			autorisationService.logMessage(file, "reversal 500 Error during date formatting for given orderid:["
					+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err3));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during date formatting", null);
		}
		// TODO: 2024-11-27 controle sur le montant à annuler

		if (montantAnnul > montantAuto) {
			autorisationService.logMessage(file,
					"You can't make the cancel because the amount sent is greater than the transaction amount.");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You can't make the cancel because the amount sent is greater than the transaction amount.", null);
		}

		if (montantReste == 0.00 && current_hist.getHatEtat().equals('T')) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Transaction already canceled", null);
		}

		if (montantReste < 0.00) {
			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"You cannot cancel because the amount sent is greater than the uncancelled amount of the transaction.",
					null);
		}

		String montanttrame = "";

		// TODO: 2024-03-05
		LinkRequestDto linkRequestDto = new LinkRequestDto();
		linkRequestDto.setMerchantid(trsRequestDto.getMerchantid());
		linkRequestDto.setOrderid(trsRequestDto.getOrderid());
		linkRequestDto.setAmount(trsRequestDto.getAmount());
		montanttrame = Util.formatMontantTrame(folder, file, trsRequestDto.getAmount(), trsRequestDto.getOrderid(), trsRequestDto.getMerchantid(), linkRequestDto);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(trsRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"reversal 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		autorisationService.logMessage(file, "Switch processing start ...");

		String tlv = "";
		autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

		// TODO: controls
		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		String mesg_type = "2";
		String merchant_name = trsRequestDto.getMerchantname();
		String acq_type = "0000";
		String processing_code = "0";
		String reason_code = "H";
		String transaction_condition = "6";
		String transactionnumber = trsRequestDto.getAuthnumber();
		merchant_name = Util.pad_merchant(trsRequestDto.getMerchantname(), 19, ' ');
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
					.withField(Tags.tag8, "0" + trsRequestDto.getMerchantid()).withField(Tags.tag9, trsRequestDto.getMerchantid())
					.withField(Tags.tag66, transactionnumber).withField(Tags.tag11, merchant_name)
					.withField(Tags.tag12, merchant_city).withField(Tags.tag13, "MAR")
					.withField(Tags.tag23, reason_code).withField(Tags.tag90, acqcode).withField(Tags.tag19, trsRequestDto.getAuthnumber())
					.encode();

		} catch (Exception err4) {
			autorisationService.logMessage(file, "reversal 500 Error during switch tlv buildu for given orderid:["
					+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err4));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
		}

		autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");

		autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

		autorisationService.logMessage(file, "Switch Connecting ...");

		String resp_tlv = "";
		SwitchTCPClient sw = SwitchTCPClient.getInstance();

		int port = 0;
		String sw_s = "", s_port = "";
		try {

			s_port = portSwitch;
			sw_s = ipSwitch;

			autorisationService.logMessage(file, "Switch IP / Switch PORT : " + sw_s + "/" + s_port);

			port = Integer.parseInt(s_port);

			boolean s_conn = sw.startConnection(sw_s, port);
			autorisationService.logMessage(file, "Switch Connecting ...");

			if (s_conn) {
				autorisationService.logMessage(file, "Switch Connected.");

				resp_tlv = sw.sendMessage(tlv);

				autorisationService.logMessage(file, "Switch TLV Request end.");
				sw.stopConnection();

			} else {

				autorisationService.logMessage(file, "Switch  malfunction !!!");

				return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
			}

		} catch (Exception e) {
			autorisationService.logMessage(file, "Switch  malfunction !!!" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
		}

		String resp = resp_tlv;
		if (resp == null) {
			autorisationService.logMessage(file, "Switch  malfunction !!!");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
		}

		if (resp.length() < 3)

		{

			autorisationService.logMessage(file, "reversal 500 Error Switch short response length() < 3 switch");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
		}

		autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

		// TODO: resp debug =
		// TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

		TLVParser tlvp = null;

		// TODO: resp debug =
		// TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

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
			tag66_resp = tlvp.getTag(Tags.tag66); // TODO: f1
			tag18_resp = tlvp.getTag(Tags.tag18);
			tag19_resp = tlvp.getTag(Tags.tag19); // TODO: f2
			tag23_resp = tlvp.getTag(Tags.tag23);
			tag20_resp = tlvp.getTag(Tags.tag20);
			tag21_resp = tlvp.getTag(Tags.tag21);
			tag22_resp = tlvp.getTag(Tags.tag22);
			tag80_resp = tlvp.getTag(Tags.tag80);
			tag98_resp = tlvp.getTag(Tags.tag98);

		} catch (Exception e) {
			autorisationService.logMessage(file, "Switch  malfunction !!!" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
		}

		// TODO: controle switch
		if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null || tag20_resp.length() < 1) {
			autorisationService.logMessage(file,
					"reversal 500 Error during tlv Switch response parse tag1_resp length tag  < 3 switch");

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal failed, the Switch is down.", "96");
		}

		autorisationService.logMessage(file, "Switch TLV Respnose Processed");


		if (tag20_resp.equalsIgnoreCase("00"))

		{
			autorisationService.logMessage(file, "Switch CODE REP : [00]");

			autorisationService.logMessage(file, "Transaction reversed.");

			try {
				autorisationService.logMessage(file, "Setting DemandePaiement status A ...");

				current_dmd.setEtatDemande("A");
				demandePaiementService.save(current_dmd);
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"reversal 500 Error during  demandepaiement update  A for given orderid:[" + trsRequestDto.getOrderid() + "]" + Util.formatException(e));

				return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during  demandepaiement update A",
						tag20_resp);
			}

			autorisationService.logMessage(file, "Setting DemandePaiement status OK.");

			try {
				// TODO: TODO: 2024-11-27 controle sur le montant à annuler
				autorisationService.logMessage(file, "totalMontantAnnul : " + totalMontantAnnul);
				autorisationService.logMessage(file, "montantReste : " + montantReste);
				if (montantReste > 0.00) {
					current_hist.setHatMontantAnnul(totalMontantAnnul);
					// TODO: TODO: on garde etat = E initial tanque montantReste > 0.00
					histoAutoGateService.save(current_hist);
				} else if (montantReste == 0.00) {
					current_hist.setHatMontantAnnul(totalMontantAnnul);
					current_hist.setHatEtat('A');
					histoAutoGateService.save(current_hist);
				}
				autorisationService.logMessage(file, "Setting HistoAutoGate status A ...");
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"reversal 500 Error during  HistoAutoGate update  A for given orderid:[" + trsRequestDto.getOrderid() + "]" + Util.formatException(e));

				return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during  HistoAutoGate update A",
						tag20_resp);
			}

			autorisationService.logMessage(file, "Setting HistoAutoGate status OK.");

		} else {

			autorisationService.logMessage(file, "Transaction reversal declined.");
			autorisationService.logMessage(file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		String s_status = "";
		try {
			CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp);
			autorisationService.logMessage(file, "codeReponseDto : " + codeReponseDto);
			if (codeReponseDto != null) {
				s_status = codeReponseDto.getRpcLibelle();
			}
		} catch (Exception ee) {
			autorisationService.logMessage(file, "authorization 500 Error codeReponseDto null" + Util.formatException(ee));
			// TODO: TODO: TODO: ee.printStackTrace();
		}

		autorisationService.logMessage(file, "Switch status : [" + s_status + "]");

		autorisationService.logMessage(file, "Generating reversalid");

		String uuid_reversalid, reversal_id = "", dtpattern, sdt = "", tmpattern, stm = "" , operation_id = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;
		try {
			uuid_reversalid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
			reversal_id = uuid_reversalid.substring(uuid_reversalid.length() - 22);
			operation_id = reversal_id;
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"reversal 500 Error during  reversalid generation for given orderid:[" + trsRequestDto.getOrderid() + "]" + Util.formatException(e));
		}

		try {
			// TODO: TODO: Transaction info
			jso.put("statuscode", tag20_resp);
			jso.put("status", s_status);
			jso.put("orderid", trsRequestDto.getOrderid());
			jso.put("amount", trsRequestDto.getAmount());
			jso.put("reversaldate", sdt);
			jso.put("reversaltime", stm);
			jso.put("authnumber", trsRequestDto.getAuthnumber());
			jso.put("reversalid", reversal_id);
			jso.put("transactionid", reversal_id);
			jso.put("operation_id", operation_id);
			jso.put("acquRefNbr", "11010");

			// TODO: TODO: Merchant info
			jso.put("merchantid", trsRequestDto.getMerchantid());
			jso.put("merchantname", trsRequestDto.getMerchantname());
			jso.put("websitename", trsRequestDto.getWebsiteName());
			jso.put("websiteid", trsRequestDto.getWebsiteid());
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// TODO: TODO: Client info
			jso.put("fname", trsRequestDto.getFname());
			jso.put("lname", trsRequestDto.getLname());
			jso.put("email", trsRequestDto.getEmail());

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
			logger.info("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			autorisationService.logMessage(file,
					"reversal 500 Error during jso out processing given authnumber:[" + trsRequestDto.getAuthnumber() + "]" + Util.formatException(err8));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "reversal 500 Error during jso out processing", tag20_resp);
		}
		autorisationService.logMessage(file, "*********** End reversal() ************** ");
		logger.info("*********** End reversal() ************** ");

		return jso.toString();

	}

	@PostMapping(value = "/napspayment/cardtoken", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String savingCardToken(@RequestHeader MultiValueMap<String, String> header,
			@RequestBody String savingcardtoken, HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_SAVINGCARD_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start savingCardToken() ************** ");
		logger.info("*********** Start savingCardToken() ************** ");

		autorisationService.logMessage(file, "savingcardtoken api call start ...");
		autorisationService.logMessage(file, "savingcardtoken : [" + savingcardtoken + "]");

		LinkRequestDto linkRequestDto;

		try {
			linkRequestDto = new ObjectMapper().readValue(savingcardtoken, LinkRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "savingcardtoken 500 malformed json expression " + savingcardtoken + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "savingcardtoken 500 malformed json expression", null);
		}

		String amount, websiteid, currency,cvv, cardnumber,expirydate, mesg_type, merc_codeactivite, transaction_condition,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transactiondate,
				transactiontime, date, rrn, heure, montanttrame, num_trs = "", transactiontype, securtoken24, etataut;

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		try {
			cardnumber = linkRequestDto.getCardnumber() == null ? "" : linkRequestDto.getCardnumber();
			expirydate = linkRequestDto.getExpirydate() == null ? "" : linkRequestDto.getExpirydate();
			cvv = linkRequestDto.getCvv() == null ? "" : linkRequestDto.getCvv();
			currency = linkRequestDto.getCurrency() == null ? "504" : linkRequestDto.getCurrency();
			websiteid = linkRequestDto.getWebsiteid() == null ? "" : linkRequestDto.getWebsiteid();
			securtoken24 = linkRequestDto.getSecurtoken24() == null ? "" : linkRequestDto.getSecurtoken24();
			Double montant = 0.00;
			amount = Util.sanitizeAmount(linkRequestDto.getAmount());
			montant = Double.valueOf(amount);
		} catch (Exception e) {
			autorisationService.logMessage(file, "Error formating data" + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "Error formating data" + e.getMessage(),
					null);
		}

		JSONObject jso = new JSONObject();
		try {
			if (cardnumber.isEmpty()) {
				autorisationService.logMessage(file, "cardnumber is empty");
				// TODO: Card info
				jso.put("token", "");
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, cardnumber is empty");
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				return jso.toString();
			}
			if (linkRequestDto.getMac_value() == null || linkRequestDto.getMac_value().equals("")) {
				autorisationService.logMessage(file, "mac_value is empty");
				// TODO: Card info
				jso.put("token", "");
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, mac_value is empty");
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				return jso.toString();
			}
			if (securtoken24.isEmpty()) {
				autorisationService.logMessage(file, "securtoken24 is empty");
				// TODO: Card info
				jso.put("token", "");
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "saving token failed, securtoken24 is empty");
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				return jso.toString();
			}
			int i_card_valid = Util.isCardValid(cardnumber);

			if (i_card_valid == 1) {
				autorisationService.logMessage(file, "Error 500 Card number length is incorrect");
				// TODO: Card info
				jso.put("token", "");
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 Card number length is incorrect");
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				return jso.toString();
			}

			if (i_card_valid == 2) {
				autorisationService.logMessage(file, "Error 500 Card number  is not valid incorrect luhn check");
				// TODO: Card info
				jso.put("token", "");
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 Card number  is not valid incorrect luhn check");
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				return jso.toString();
			}
			// TODO: verification expiration token
			jso = verifieToken(securtoken24, file);
			if (!jso.get("statuscode").equals("00")) {
				// TODO: Card info
				jso.put("token", "");
				autorisationService.logMessage(file, "jso : " + jso.toString());
				logger.info("jso : " + jso.toString());
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				logger.info("*********** End savingCardToken() ************** ");
				return jso.toString();
			}
			int dateint = Integer.valueOf(expirydate);

			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();

			autorisationService.logMessage(file, "cardtokenDto expirydate input : " + expirydate);
			String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
			// TODO: get year from date
			String year = anne.substring(0, 2) + expirydate.substring(0, 2);
			String moi = expirydate.substring(2, expirydate.length());
			// TODO: format date to FORMAT_DEFAUT
			String expirydateFormated = year + "-" + moi + "-" + "01";
			logger.info("cardtokenDto expirydate : " + expirydateFormated);
			autorisationService.logMessage(file, "cardtokenDto expirydate formated : " + expirydateFormated);
			Date dateExp = dateFormatSimple.parse(expirydateFormated);

			if (dateExp.before(dateToken)) {
				// TODO: Card info
				jso.put("token", "");

				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status",
						"saving token failed, invalid expiry date (expiry date must be greater than system date)");
				autorisationService.logMessage(file, "jso : " + jso.toString());
				logger.info("jso : " + jso.toString());
				autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
				logger.info("*********** End savingCardToken() ************** ");
				return jso.toString();
			}

			CommercantDto current_merchant = null;
			try {
				current_merchant = commercantService.findByCmrNumcmr(linkRequestDto.getMerchantid());
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + linkRequestDto.getMerchantid()
								+ "]");

				return Util.getMsgError(folder, file, linkRequestDto,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "15");
			}

			if (current_merchant == null) {
				autorisationService.logMessage(file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + linkRequestDto.getMerchantid()
								+ "]");

				return Util.getMsgError(folder, file, linkRequestDto,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "15");
			}

			if (current_merchant.getCmrCodactivite() == null) {
				autorisationService.logMessage(file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + linkRequestDto.getMerchantid()
								+ "]");

				return Util.getMsgError(folder, file, linkRequestDto,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "15");
			}

			if (current_merchant.getCmrCodbqe() == null) {
				autorisationService.logMessage(file,
						"savingCardToken 500 Merchant misconfigured in DB or not existing merchantid:[" + linkRequestDto.getMerchantid()
								+ "]");

				return Util.getMsgError(folder, file, linkRequestDto,
						"savingCardToken 500 Merchant misconfigured in DB or not existing", "");
			}

			GalerieDto galerie = null;

			try {
				galerie = galerieService.findByCodeCmr(linkRequestDto.getMerchantid());
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"authorization 500 Galerie misconfigured in DB or not existing");

				return Util.getMsgError(folder, file, linkRequestDto,
						"authorization 500 Galerie misconfigured in DB or not existing", "15");
			}

			if (galerie == null) {
				autorisationService.logMessage(file,
						"authorization 500 Galerie misconfigured in DB or not existing");

				return Util.getMsgError(folder, file, linkRequestDto,
						"authorization 500 Galerie misconfigured in DB or not existing", "15");
			}
			if (!websiteid.equals(galerie.getCodeGal())) {
				websiteid = galerie.getCodeGal();
			}

			int i_card_type = Util.getCardIss(cardnumber);

			DemandePaiementDto dmd = null;
			DemandePaiementDto dmdSaved = null;
			amount = "10";
			currency = "504";
			try {

				dmd = new DemandePaiementDto();

				dmd.setComid(linkRequestDto.getMerchantid());
				// TODO: generer commande
				String orderid = Util.genCommande(linkRequestDto.getMerchantid());
				dmd.setCommande(orderid);
				dmd.setDemPan(cardnumber);
				dmd.setDemCvv(cvv);
				dmd.setGalid(websiteid);
				dmd.setSuccessURL(linkRequestDto.getSuccessURL());
				dmd.setFailURL(linkRequestDto.getFailURL());
				dmd.setTypeCarte(i_card_type + "");
				amount = Util.sanitizeAmount(amount);
				dmd.setMontant(Double.parseDouble(amount));
				dmd.setNom(linkRequestDto.getLname());
				dmd.setPrenom(linkRequestDto.getFname());
				dmd.setEmail(linkRequestDto.getEmail());
				dmd.setTel("");
				dmd.setAddress("");
				dmd.setCity("");
				dmd.setCountry("");
				dmd.setState("");
				dmd.setPostcode("");
				dmd.setLangue("E");
				dmd.setEtatDemande("INIT");

				formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
				formatter_2 = new SimpleDateFormat("HH:mm:ss");
				trsdate = new Date();
				transactiondate = formatter_1.format(trsdate);
				transactiontime = formatter_2.format(trsdate);
				// TODO: dmd.setDemDateTime(transactiondate + transactiontime);
				dmd.setDemDateTime(dateFormat.format(new Date()));
				dmd.setIsCof("Y");
				dmd.setIsAddcard("Y");
				dmd.setIsTokenized("Y");
				dmd.setIsWhitelist("Y");
				dmd.setIsWithsave("Y");

				// TODO: generer token
				String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
				dmd.setTokencommande(tokencommande);
				// TODO: set transctiontype
				dmd.setTransactiontype("0");
				dmd.setDateexpnaps(expirydate);

				dmdSaved = demandePaiementService.save(dmd);
				dmdSaved.setExpery(expirydate);

			} catch (Exception err1) {
				autorisationService.logMessage(file,
						"savingCardToken 500 Error during DEMANDE_PAIEMENT insertion for given merchantid:["
								+ linkRequestDto.getMerchantid() + "]" + Util.formatException(err1));

				return Util.getMsgError(folder, file, linkRequestDto,
						"The current operation was not successful, your account will not be debited, please try again .", null);
			}

			try {
				formatheure = new SimpleDateFormat("HHmmss");
				formatdate = new SimpleDateFormat("ddMMyy");
				date = formatdate.format(new Date());
				heure = formatheure.format(new Date());
				rrn = Util.getGeneratedRRN();

			} catch (Exception err2) {
				dmdSaved.setDemCvv("");
				demandePaiementService.save(dmdSaved);
				autorisationService.logMessage(file,
						"savingCardToken 500 Error during  date formatting for given orderid:[" + linkRequestDto.getOrderid()
								+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err2));

				return Util.getMsgError(folder, file, linkRequestDto, "savingCardToken 500 Error during  date formatting",
						null);
			}

			ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();
			// TODO: appel 3DSSecure ***********************************************************

			/**
			 * dans la preprod les tests Coca sans 3DSS on commente l'appel 3DSS et on mj
			 * reponseMPI="Y"
			 */
			autorisationService.logMessage(file, "environement : " + environement);
			if (environement.equals("PREPROD")) {
				threeDsecureResponse.setReponseMPI("Y");
			} else {
				threeDsecureResponse = autorisationService.preparerAeqThree3DSS(dmdSaved, folder, file);
			}

			// TODO: fin 3DSSecure ***********************************************************

			/*
			 * ------------ DEBUT MPI RESPONSE PARAMS ------------
			 */
			String reponseMPI = "";
			String eci = "";
			String cavv = "";
			String threeDSServerTransID = "";
			String xid = "";
			String errmpi = "";
			// TODO: String idDemande = "";
			String idDemande = String.valueOf(dmdSaved.getIddemande() == null ? "" : dmdSaved.getIddemande());
			String expiry = ""; // TODO: YYMM

			reponseMPI = threeDsecureResponse.getReponseMPI();

			threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();

			eci = threeDsecureResponse.getEci() == null ? "" : threeDsecureResponse.getEci();

			cavv = threeDsecureResponse.getCavv() == null ? "" : threeDsecureResponse.getCavv();

			errmpi = threeDsecureResponse.getErrmpi() == null ? "" : threeDsecureResponse.getErrmpi();

			expiry = threeDsecureResponse.getExpiry() == null ? "" : threeDsecureResponse.getExpiry();

			if (idDemande == null || idDemande.equals("")) {
				autorisationService.logMessage(file, "received idDemande from MPI is Null or Empty");
				dmdSaved.setEtatDemande("MPI_KO");
				dmdSaved.setDemCvv("");
				demandePaiementService.save(dmdSaved);
				autorisationService.logMessage(file,
						"demandePaiement after update MPI_KO idDemande null : " + dmdSaved.toString());
				return Util.getMsgError(folder, file, linkRequestDto, "AUTO INVALIDE DEMANDE MPI_KO", "96");
			}

			dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

			if (dmd == null) {
				dmdSaved.setDemCvv("");
				demandePaiementService.save(dmdSaved);
				autorisationService.logMessage(file,
						"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
								+ idDemande);
				return Util.getMsgError(folder, file, linkRequestDto, "AUTO INVALIDE DEMANDE NOT FOUND", "96");
			}

			if (reponseMPI == null || reponseMPI.equals("")) {
				dmd.setEtatDemande("MPI_KO");
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file,
						"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
				autorisationService.logMessage(file, "Response 3DS is null");
				return Util.getMsgError(folder, file, linkRequestDto, "Response 3DS is null", "96");
			}

			if (reponseMPI.equals("Y")) {
				autorisationService.logMessage(file,
						"********************* Cas frictionless responseMPI equal Y *********************");
				if (threeDSServerTransID != null && !threeDSServerTransID.equals("")) {
					dmd.setDemxid(threeDSServerTransID);
					dmd.setIs3ds("N");
					dmd = demandePaiementService.save(dmd);
				}
				// TODO: Si la transaction frictionless on debit le client avec 0dh
				amount = "0";

				// TODO: 2024-03-05
				montanttrame = Util.formatMontantTrame(folder, file, amount, linkRequestDto.getOrderid(), linkRequestDto.getMerchantid(), linkRequestDto);

				merc_codeactivite = current_merchant.getCmrCodactivite();
				acqcode = current_merchant.getCmrCodbqe();
				merchant_name = Util.pad_merchant(linkRequestDto.getMerchantname(), 19, ' ');

				merchant_city = "MOROCCO        ";

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
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"savingcardtoken 500 Error during  date formatting for given merchantid:[" + linkRequestDto.getMerchantid()
									+ "]" + Util.formatException(err2));

					return Util.getMsgError(folder, file, linkRequestDto,
							"savingcardtoken 500 Error during  date formatting", null);
				}

				boolean cvv_present = checkCvvPresence(cvv);
				boolean is_first_trs = true;

				String first_auth = "";
				long lrec_serie = 0;

				// TODO: controls
				autorisationService.logMessage(file, "Switch processing start ...");

				String tlv = "";
				autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

				if (!cvv_present) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"savingcardtoken 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

					return Util.getMsgError(folder, file, linkRequestDto,
							"savingcardtoken 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
							"82");
				}

				// TODO: not reccuring , normal
				if (cvv_present) {
					autorisationService.logMessage(file, "not reccuring , normal cvv_present && !is_reccuring");
					try {

						tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
								.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
								.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
								.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
								.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
								.withField(Tags.tag16, date).withField(Tags.tag17, heure)
								.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + linkRequestDto.getMerchantid())
								.withField(Tags.tag9, linkRequestDto.getMerchantid()).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
								.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
								.withField(Tags.tag90, acqcode).encode();

					} catch (Exception err4) {
						dmd.setDemCvv("");
						demandePaiementService.save(dmd);
						autorisationService.logMessage(file,
								"savingcardtoken 500 Error during switch tlv buildup for given merchantid:["
										+ linkRequestDto.getMerchantid() + "]" + Util.formatException(err4));

						return Util.getMsgError(folder, file, linkRequestDto, "savingcardtoken failed, the Switch is down.",
								"96");
					}

					autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");

				}

				autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

				String resp_tlv = "";
				int port = 0;
				String sw_s = "", s_port = "";
				int switch_ko = 0;
				try {

					s_port = portSwitch;
					sw_s = ipSwitch;

					port = Integer.parseInt(s_port);

					autorisationService.logMessage(file, "Switch TCP client V2 Connecting ...");

					SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

					boolean s_conn = switchTCPClient.isConnected();

					if (!s_conn) {
						dmd.setDemCvv("");
						dmd.setEtatDemande("SW_KO");
						demandePaiementService.save(dmd);
						autorisationService.logMessage(file, "Switch  malfunction cannot connect!!!");

						return Util.getMsgError(folder, file, linkRequestDto, "savingcardtoken failed, the Switch is down.",
								"96");
					}

					if (s_conn) {
						autorisationService.logMessage(file, "Switch Connected.");

						resp_tlv = switchTCPClient.sendMessage(tlv);

						autorisationService.logMessage(file, "Switch TLV Request end.");
						switchTCPClient.shutdown();
					}

				} catch (Exception e) {
					dmd.setDemCvv("");
					dmd.setEtatDemande("SW_KO");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file, "Switch  malfunction Exception!!!" + Util.formatException(e));
					switch_ko = 1;
					return Util.getMsgError(folder, file, linkRequestDto, "savingcardtoken failed, the Switch is down.",
							"96");
				}

				// TODO: resp debug =
				// TODO: resp_tlv =
				// TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

				String resp = resp_tlv;

				if (switch_ko == 0 && resp == null) {
					dmd.setDemCvv("");
					dmd.setEtatDemande("SW_KO");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
					switch_ko = 1;
					autorisationService.logMessage(file, "savingcardtoken 500 Error Switch null response"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					return Util.getMsgError(folder, file, linkRequestDto, "savingcardtoken failed, the Switch is down.",
							"96");
				}

				if (switch_ko == 0 && resp.length() < 3) {
					switch_ko = 1;

					autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
					autorisationService.logMessage(file,
							"savingcardtoken 500 Error Switch short response length() < 3 " + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

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
						tag66_resp = tlvp.getTag(Tags.tag66); // TODO: f1
						tag18_resp = tlvp.getTag(Tags.tag18);
						tag19_resp = tlvp.getTag(Tags.tag19); // TODO: f2
						tag23_resp = tlvp.getTag(Tags.tag23);
						tag20_resp = tlvp.getTag(Tags.tag20);
						tag21_resp = tlvp.getTag(Tags.tag21);
						tag22_resp = tlvp.getTag(Tags.tag22);
						tag80_resp = tlvp.getTag(Tags.tag80);
						tag98_resp = tlvp.getTag(Tags.tag98);

					} catch (Exception e) {
						autorisationService.logMessage(file, "Switch  malfunction tlv parsing !!!" + Util.formatException(e));
						switch_ko = 1;
						autorisationService.logMessage(file,
								"savingcardtoken 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					}

					// TODO: controle switch
					if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
						autorisationService.logMessage(file, "Switch  malfunction !!! tag1_resp == null");
						switch_ko = 1;
						autorisationService.logMessage(file,
								"savingcardtoken 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
										+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
										+ resp_tlv + "]");
					}
				}
				autorisationService.logMessage(file, "Switch TLV Respnose Processed");

				String tag20_resp_verified = "";
				String tag19_res_verified = "";
				String tag66_resp_verified = "";
				tag20_resp_verified = tag20_resp;
				tag19_res_verified = tag19_resp;
				tag66_resp_verified = tag66_resp;
				String s_status, pan_auto = "";

				if (switch_ko == 1) {
					pan_auto = Util.formatagePan(cardnumber);
					autorisationService.logMessage(file, "getSWHistoAuto pan_auto/rrn/amount/date/merchantid : "
							+ pan_auto + "/" + rrn + "/" + amount + "/" + date + "/" + linkRequestDto.getMerchantid());
				}

				s_status = "";
				try {
					CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
					autorisationService.logMessage(file, "" + codeReponseDto);
					if (codeReponseDto != null) {
						s_status = codeReponseDto.getRpcLibelle();
					}
				} catch (Exception ee) {
					autorisationService.logMessage(file, "savingcardtoken 500 Error codeReponseDto null" + Util.formatException(ee));
					// TODO: TODO: ee.printStackTrace();
				}
				
				autorisationService.logMessage(file, "get status Switch status : [" + s_status + "]");
				
				websiteid = dmd.getGalid();

				HistoAutoGateDto hist = null;
				Integer Ihist_id = null;

				autorisationService.logMessage(file, "Insert into Histogate...");

				try {

					hist = new HistoAutoGateDto();
					Date curren_date_hist = new Date();
					int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

					autorisationService.logMessage(file, "formatting pan...");

					pan_auto = Util.formatagePan(cardnumber);

					autorisationService.logMessage(file, "HistoAutoGate data filling start ...");

					autorisationService.logMessage(file, "websiteid : " + websiteid);

					Date current_date_1 = getDateWithoutTime(curren_date_hist);
					hist.setHatDatdem(current_date_1);

					hist.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
					hist.setHatMontant(Double.parseDouble(amount));
					hist.setHatNumcmr(linkRequestDto.getMerchantid());
					hist.setHatCoderep(tag20_resp_verified);
					tag20_resp = tag20_resp_verified;
					hist.setHatDevise(currency);
					hist.setHatBqcmr(acqcode);
					hist.setHatPorteur(pan_auto);
					hist.setHatMtfref1(s_status);
					hist.setHatNomdeandeur(websiteid);
					hist.setHatNautemt(tag19_res_verified); // TODO: f2
					tag19_resp = tag19_res_verified;
					if (tag22_resp != null)
						hist.setHatProcode(tag22_resp.charAt(0));
					else
						hist.setHatProcode('6');
					hist.setHatExpdate(expirydate);
					hist.setHatRepondeur(tag21_resp);
					hist.setHatTypmsg("3");
					hist.setHatRrn(tag66_resp_verified); // TODO: f1
					tag66_resp_verified = tag66_resp;
					hist.setHatEtat('E');
					if (websiteid.equals("")) {
						hist.setHatCodtpe("1");
					} else {
						hist.setHatCodtpe(websiteid);
					}
					hist.setHatMcc(merc_codeactivite);
					hist.setHatNumCommande(linkRequestDto.getOrderid());
					hist.setHatNumdem(new Long(numTransaction));

					if (checkCvvPresence(cvv)) {

						hist.setIsCvvVerified("Y");
					} else {

						hist.setIsCvvVerified("N");
					}

					hist.setIs3ds("N");
					hist.setIsAddcard("N");
					hist.setIsWhitelist("N");
					hist.setIsWithsave("N");
					hist.setIsTokenized("N");

					hist.setIsCof("N");

					autorisationService.logMessage(file, "HistoAutoGate data filling end ...");

					autorisationService.logMessage(file, "HistoAutoGate Saving ...");

					hist = histoAutoGateService.save(hist);

					autorisationService.logMessage(file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

				} catch (Exception e) {
					autorisationService.logMessage(file,
							"savingcardtoken 500 Error during  insert in histoautogate for given orderid:[" + linkRequestDto.getOrderid()
									+ "]" + Util.formatException(e));
					try {
						autorisationService.logMessage(file, "2eme tentative : HistoAutoGate Saving ... ");
						hist = histoAutoGateService.save(hist);
					} catch (Exception ex) {
						autorisationService.logMessage(file,
								"2eme tentative : savingcardtoken 500 Error during  insert in histoautogate for given orderid:["
										+ linkRequestDto.getOrderid() + "]" + Util.formatException(ex));
					}
				}

				autorisationService.logMessage(file, "HistoAutoGate OK.");

				if (tag20_resp == null) {
					tag20_resp = "";
				}

				autorisationService.logMessage(file, "Generating paymentid...");

				String uuid_paymentid, paymentid = "";
				try {
					uuid_paymentid = String.format("%040d",
							new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
					paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
				} catch (Exception e) {
					autorisationService.logMessage(file,
							"savingcardtoken 500 Error during  paymentid generation for given orderid:[" + linkRequestDto.getOrderid() + "]"
									+ Util.formatException(e));
				}

				autorisationService.logMessage(file, "Generating paymentid OK");
				autorisationService.logMessage(file, "paymentid :[" + paymentid + "]");

				// TODO: JSONObject jso = new JSONObject();

				autorisationService.logMessage(file, "Preparing autorization api response");

				String authnumber, coderep, motif, merchnatidauth, dtdem = "";

				try {
					authnumber = hist.getHatNautemt();
					coderep = hist.getHatCoderep();
					motif = hist.getHatMtfref1();
					merchnatidauth = hist.getHatNumcmr();
					dtdem = dmd.getDemPan();
				} catch (Exception e) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"savingcardtoken 500 Error during authdata preparation orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(e));

					return Util.getMsgError(folder, file, linkRequestDto,
							"savingcardtoken 500 Error during authdata preparation", tag20_resp);
				}

				if (tag20_resp.equalsIgnoreCase("00")) {
					autorisationService.logMessage(file, "SWITCH RESONSE CODE :[00]");

					try {
						autorisationService.logMessage(file, "update etat demande : SW_PAYE ...");

						dmd.setEtatDemande("SW_PAYE");
						dmd.setDemCvv("");
						dmd = demandePaiementService.save(dmd);

					} catch (Exception e) {
						autorisationService.logMessage(file,
								"savingcardtoken 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
										+ linkRequestDto.getOrderid() + "]" + Util.formatException(e));
					}

					autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");
					// TODO: insert new cardToken
					CardtokenDto cardtokenDto = new CardtokenDto();
					String tokencard = Util.generateCardToken(linkRequestDto.getMerchantid());

					// TODO: test if token not exist in DB
					// TODO: CardtokenDto checkCardToken =
					// TODO: cardtokenService.findByIdMerchantAndTokenAndExprDate(linkRequestDto.getMerchantid(), tokencard,
					// TODO: dateExp);
					CardtokenDto checkCardToken = cardtokenService.findByIdMerchantAndToken(linkRequestDto.getMerchantid(), tokencard);

					while (checkCardToken != null) {
						tokencard = Util.generateCardToken(linkRequestDto.getMerchantid());
						logger.info("checkCardToken exist => generate new tokencard : " + tokencard);
						autorisationService.logMessage(file,
								"checkCardToken exist => generate new tokencard : " + tokencard);
						checkCardToken = cardtokenService.findByIdMerchantAndToken(linkRequestDto.getMerchantid(), tokencard);
					}
					autorisationService.logMessage(file, "tokencard : " + tokencard);

					cardtokenDto.setToken(tokencard);
					String tokenid = UUID.randomUUID().toString();
					cardtokenDto.setIdToken(tokenid);
					cardtokenDto.setExprDate(dateExp);
					String dateTokenStr = dateFormat.format(dateToken);
					Date dateTokenFormated = dateFormat.parse(dateTokenStr);
					cardtokenDto.setTokenDate(dateTokenFormated);
					cardtokenDto.setCardNumber(cardnumber);
					cardtokenDto.setIdMerchant(linkRequestDto.getMerchantid());
					cardtokenDto.setIdMerchantClient(linkRequestDto.getMerchantid());
					cardtokenDto.setFirstName(linkRequestDto.getFname());
					cardtokenDto.setLastName(linkRequestDto.getLname());
					cardtokenDto.setHolderName(linkRequestDto.getHoldername());
					cardtokenDto.setMcc(linkRequestDto.getMerchantid());

					CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

					autorisationService.logMessage(file, "Saving CARDTOKEN OK");

					// TODO: Card info
					jso.put("token", cardtokenSaved.getToken());
					jso.put("cardnumber", Util.formatCard(cardnumber));

					// TODO: Transaction info
					jso.put("statuscode", "00");
					jso.put("status", "saving token successfully");
					jso.put("etataut", "Y");
					jso.put("orderid", linkRequestDto.getOrderid());
					jso.put("amount", amount);
					jso.put("transactiondate", date);
					jso.put("transactiontime", heure);
					jso.put("authnumber", authnumber);
					jso.put("paymentid", paymentid);
					jso.put("linkacs", "");

					// TODO: Merchant info
					jso.put("merchantid", linkRequestDto.getMerchantid());
					jso.put("merchantname", linkRequestDto.getMerchantname());
					jso.put("websitename", linkRequestDto.getWebsitename());
					jso.put("websiteid", websiteid);

					// TODO: Client info
					jso.put("fname", linkRequestDto.getFname());
					jso.put("lname", linkRequestDto.getLname());
					jso.put("email", linkRequestDto.getEmail());
				} else {
					autorisationService.logMessage(file, "transaction declined !!! ");
					autorisationService.logMessage(file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

					try {

						autorisationService.logMessage(file,
								"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

						dmd.setEtatDemande("SW_REJET");
						dmd.setDemCvv("");
						dmd = demandePaiementService.save(dmd);
					} catch (Exception e) {
						dmd.setDemCvv("");
						demandePaiementService.save(dmd);
						autorisationService.logMessage(file,
								"savingcardtoken 500 Error during  DemandePaiement update SW_REJET for given orderid:["
										+ linkRequestDto.getOrderid() + "]" + Util.formatException(e));

						return Util.getMsgError(folder, file, linkRequestDto,
								"savingcardtoken 500 Error during  DemandePaiement update SW_REJET", tag20_resp);
					}

					autorisationService.logMessage(file, "update Demandepaiement status to SW_REJET OK.");

					try {
						if (hist.getId() == null) {
							// TODO: get histoauto check if exist
							HistoAutoGateDto histToAnnulle = histoAutoGateService
									.findByHatNumCommandeAndHatNumcmrV1(linkRequestDto.getOrderid(), linkRequestDto.getMerchantid());
							if (histToAnnulle != null) {
								autorisationService.logMessage(file,
										"transaction declinded ==> update HistoAutoGateDto etat to A ...");
								histToAnnulle.setHatEtat('A');
								histToAnnulle = histoAutoGateService.save(histToAnnulle);
							} else {
								hist.setHatEtat('A');
								hist = histoAutoGateService.save(hist);
							}
						} else {
							hist.setHatEtat('A');
							hist = histoAutoGateService.save(hist);
						}
					} catch (Exception err2) {
						autorisationService.logMessage(file,
								"saving token 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:["
										+ linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err2));
					}
					autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");

					autorisationService.logMessage(file,
							"saving token failed, coderep : [" + tag20_resp + "]" + "motif : [" + s_status + "]");
					// TODO: Card info
					jso.put("token", "");
					jso.put("cardnumber", Util.formatCard(cardnumber));

					// TODO: Transaction info
					if (tag20_resp.equals("")) {
						jso.put("statuscode", "17");
					} else {
						jso.put("statuscode", tag20_resp);
					}
					jso.put("status",
							"saving token failed, coderep : [" + tag20_resp + "]" + "motif : [" + s_status + "]");
					jso.put("etataut", "Y");
					jso.put("orderid", linkRequestDto.getOrderid());
					jso.put("amount", amount);
					jso.put("transactiondate", date);
					jso.put("transactiontime", heure);
					jso.put("authnumber", authnumber);
					jso.put("paymentid", paymentid);
					jso.put("linkacs", "");

					// TODO: Merchant info
					jso.put("merchantid", linkRequestDto.getMerchantid());
					jso.put("merchantname", linkRequestDto.getMerchantname());
					jso.put("websitename", linkRequestDto.getWebsitename());
					jso.put("websiteid", websiteid);

					// TODO: Client info
					jso.put("fname", linkRequestDto.getFname());
					jso.put("lname", linkRequestDto.getLname());
					jso.put("email", linkRequestDto.getEmail());
				}
			} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
				// TODO: ********************* Cas chalenge responseMPI equal C ou D
				// TODO: *********************
				autorisationService.logMessage(file, "****** Cas chalenge responseMPI equal C ou D ******");
				try {

					// TODO: Transaction info
					jso.put("etataut", "C");
					jso.put("orderid", linkRequestDto.getOrderid());
					jso.put("amount", amount);
					jso.put("transactiondate", date);
					jso.put("transactiontime", heure);

					// TODO: Merchant info
					jso.put("merchantid", linkRequestDto.getMerchantid());
					jso.put("merchantname", linkRequestDto.getMerchantname());
					jso.put("websitename", linkRequestDto.getWebsitename());
					jso.put("websiteid", websiteid);

					// TODO: Card info
					jso.put("cardnumber", Util.formatCard(cardnumber));
					jso.put("token", "");
					jso.put("statuscode", "00");
					jso.put("status", "Challenge");

					// TODO: Client info
					jso.put("fname", linkRequestDto.getFname());
					jso.put("lname", linkRequestDto.getLname());
					jso.put("email", linkRequestDto.getEmail());

					// TODO: Link ACS chalenge info
					jso.put("linkacs", linkChalenge + dmd.getTokencommande());

					// TODO: insertion htmlCreq dans la demandePaiement
					dmd.setCreq(threeDsecureResponse.getHtmlCreq());
					dmd.setDemxid(threeDSServerTransID);
					dmd.setEtatDemande("SND_TO_ACS");
					dmd.setIs3ds("Y");
					dmd = demandePaiementService.save(dmd);

					logger.info("linkChalenge " + linkChalenge + dmd.getTokencommande());
					autorisationService.logMessage(file,
							"linkChalenge " + linkChalenge + dmd.getTokencommande());

					logger.info("savingcardtoken api response chalenge :  [" + jso.toString() + "]");
					autorisationService.logMessage(file,
							"savingcardtoken api response chalenge :  [" + jso.toString() + "]");
				} catch (Exception ex) {
					autorisationService.logMessage(file,
							"savingcardtoken 500 Error during jso out processing " + Util.formatException(ex));

					return Util.getMsgError(folder, file, linkRequestDto,
							"savingcardtoken 500 Error during jso out processing ", null);
				}
			} else if (reponseMPI.equals("E")) {
				// TODO: ********************* Cas responseMPI equal E
				// TODO: *********************
				autorisationService.logMessage(file, "****** Cas responseMPI equal E ******");
				autorisationService.logMessage(file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
				dmd.setEtatDemande("MPI_DS_ERR");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				dmd = demandePaiementService.save(dmd);

				// TODO: Transaction info
				jso.put("statuscode", "96");
				jso.put("status",
						"La transaction en cours n’a pas abouti (Problème authentification 3DSecure), votre compte ne sera pas débité, merci de contacter votre banque .");
				jso.put("etataut", "N");
				jso.put("orderid", linkRequestDto.getOrderid());
				jso.put("amount", amount);
				jso.put("transactiondate", date);
				jso.put("transactiontime", heure);
				jso.put("transactionid", "123456");

				// TODO: Merchant info
				jso.put("merchantid", linkRequestDto.getMerchantid());
				jso.put("merchantname", linkRequestDto.getMerchantname());
				jso.put("websitename", linkRequestDto.getWebsitename());
				jso.put("websiteid", websiteid);

				// TODO: Card info
				jso.put("cardnumber", Util.formatCard(cardnumber));
				jso.put("token", "");

				// TODO: Client info
				jso.put("fname", linkRequestDto.getFname());
				jso.put("lname", linkRequestDto.getLname());
				jso.put("email", linkRequestDto.getEmail());

				// TODO: Link ACS chalenge info :
				jso.put("linkacs", "");

				logger.info("savingcardtoken api response fail :  [" + jso.toString() + "]");
				autorisationService.logMessage(file,
						"savingcardtoken api response fail :  [" + jso.toString() + "]");
			} else {
				switch (errmpi) {
				case "COMMERCANT NON PARAMETRE":
					autorisationService.logMessage(file, "COMMERCANT NON PARAMETRE : " + idDemande);
					dmd.setDemxid(threeDSServerTransID);
					dmd.setDemCvv("");
					dmd.setEtatDemande("MPI_CMR_INEX");
					demandePaiementService.save(dmd);
					// TODO: externalContext.redirect("operationErreur.xhtml?Error=".concat("COMMERCANT
					// TODO: NON PARAMETRE"));
					return Util.getMsgError(folder, file, linkRequestDto, "COMMERCANT NON PARAMETRE", "15");
				case "BIN NON PARAMETRE":
					autorisationService.logMessage(file, "BIN NON PARAMETRE : " + idDemande);
					dmd.setEtatDemande("MPI_BIN_NON_PAR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "BIN NON PARAMETREE", "96");
				case "DIRECTORY SERVER":
					autorisationService.logMessage(file, "DIRECTORY SERVER : " + idDemande);
					dmd.setEtatDemande("MPI_DS_ERR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "MPI_DS_ERR", "96");
				case "CARTE ERRONEE":
					autorisationService.logMessage(file, "CARTE ERRONEE : " + idDemande);
					dmd.setEtatDemande("MPI_CART_ERROR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "CARTE ERRONEE", "96");
				case "CARTE NON ENROLEE":
					autorisationService.logMessage(file, "CARTE NON ENROLEE : " + idDemande);
					dmd.setEtatDemande("MPI_CART_NON_ENR");
					dmd.setDemCvv("");
					dmd.setDemxid(threeDSServerTransID);
					demandePaiementService.save(dmd);
					return Util.getMsgError(folder, file, linkRequestDto, "CARTE NON ENROLLE", "96");
				}
			}
		} catch (Exception ex) {
			autorisationService.logMessage(file,
					"cardtoken 500 Error during CARDTOKEN Saving : " + ex.getMessage());
			// TODO: Card info
			jso.put("token", "");

			// TODO: Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "saving token failed ");

			autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
			logger.info("*********** End savingCardToken() ************** ");

			return jso.toString();
		}

		autorisationService.logMessage(file, "*********** End savingCardToken() ************** ");
		logger.info("*********** End savingCardToken() ************** ");

		return jso.toString();
	}

	@PostMapping(value = "/napspayment/deleteCardtoken", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String deleteCardTken(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cardtoken,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_DELETECRDTKN_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start deleteCardTken() ************** ");
		logger.info("*********** Start deleteCardTken() ************** ");

		autorisationService.logMessage(file, "deleteCardTken api call start ...");
		autorisationService.logMessage(file, "deleteCardTken : [" + cardtoken + "]");

		LinkRequestDto linkRequestDto;

		try {
			linkRequestDto = new ObjectMapper().readValue(cardtoken, LinkRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "deleteCardTken 500 malformed json expression " + cardtoken + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "deleteCardTken 500 malformed json expression", null);
		}

		String merchantid, cardnumber, token;

		try {
			cardnumber = linkRequestDto.getCardnumber() == null ? "" : linkRequestDto.getCardnumber();
			token = linkRequestDto.getToken() == null ? "" : linkRequestDto.getToken();
			merchantid = linkRequestDto.getMerchantid() == null ? "" : linkRequestDto.getMerchantid();
		} catch (Exception e) {
			autorisationService.logMessage(file, "The amount must be greater than or equal to 5dh" + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "The amount must be greater than or equal to 5dh" + e.getMessage(),
					null);
		}

		JSONObject jso = new JSONObject();
		try {
			if (merchantid.equals("")) {
				autorisationService.logMessage(file, "merchantid is empty");
				// TODO: Card info
				jso.put("token", token);

				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "delete token failed, merchantid is empty");

				autorisationService.logMessage(file, "*********** End deleteCardTken() ************** ");

				return jso.toString();
			}
			if (token.equals("")) {
				autorisationService.logMessage(file, "token is empty");
				// TODO: Card info
				jso.put("token", "");

				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "delete token failed, token is empty");

				autorisationService.logMessage(file, "*********** End deleteCardTken() ************** ");

				return jso.toString();
			}

			CardtokenDto cardTokenTodelete = cardtokenService.findByIdMerchantAndToken(merchantid, token);

			cardtokenService.delete(cardTokenTodelete);

			// TODO: Card info
			jso.put("token", token);

			// TODO: Transaction info
			jso.put("statuscode", "00");
			jso.put("status", "delete token successfully ");

			autorisationService.logMessage(file, "Delete CARDTOKEN OK");

		} catch (Exception ex) {
			autorisationService.logMessage(file, "Error during delete token : " + Util.formatException(ex));
			// TODO: Card info
			jso.put("token", token);

			// TODO: Transaction info
			jso.put("statuscode", "17");
			jso.put("status", "delete token failed");
		}

		autorisationService.logMessage(file, "*********** End deleteCardTken() ************** ");
		logger.info("*********** End deleteCardTken() ************** ");

		return jso.toString();
	}

	@SuppressWarnings("all")
	@PostMapping(value = "/napspayment/histo/exportexcel", consumes = "application/json", produces = "application/json")
	public void exportToExcel(@RequestHeader MultiValueMap<String, String> header, @RequestBody String req,
			HttpServletResponse response) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_EXPORTE_EXCEL_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start exportToExcel() ************** ");
		logger.info("*********** Start exportToExcel() ************** ");

		autorisationService.logMessage(file, "exportToExcel api call start ...");
		autorisationService.logMessage(file, "exportToExcel : [" + req + "]");

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
			autorisationService.logMessage(file, "exportToExcel 500 malformed json expression " + req + jserr);
		}

		String merchantid = "", orderid = "", dateDem = "";
		if (jsonOrequest != null) {
			try {
				// TODO: Transaction info
				merchantid = (String) jsonOrequest.get("merchantid");

				orderid = (String) jsonOrequest.get("orderid");
				dateDem = (String) jsonOrequest.get("dateDem");

			} catch (JSONException jserr) {
				autorisationService.logMessage(file, "exportToExcel 500 malformed json expression " + req + jserr);
			}
		} else {
			autorisationService.logMessage(file, "exportToExcel 500 JSON parsing failed, jsonOrequest is null.");
		}


		try {

			// TODO: List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findAll();
			List<HistoAutoGateDto> listHistoGate = histoAutoGateService.findByHatNumcmr(merchantid);

			GenerateExcel excelExporter = new GenerateExcel(listHistoGate);

			excelExporter.export(response);

		} catch (Exception e) {
			autorisationService.logMessage(file, "exportToExcel 500 merchantid:[" + merchantid + "]");
			autorisationService.logMessage(file, "exportToExcel 500 exception" + Util.formatException(e));
		}

		autorisationService.logMessage(file, "*********** End exportToExcel ***********");
		logger.info("*********** End exportToExcel ***********");
	}

	@PostMapping(value = "/napspayment/cpautorisation", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String cpautorisation(@RequestHeader MultiValueMap<String, String> header, @RequestBody String cpauths,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_CPAUTORISATION_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start cpautorisation() ************** ");
		logger.info("*********** Start cpautorisation() ************** ");

		logger.info("cpautorisation api call start ...");

		autorisationService.logMessage(file, "cpautorisation api call start ...");

		autorisationService.logMessage(file, "cpautorisation : [" + cpauths + "]");

		TransactionRequestDto trsRequestDto;

		try {
			trsRequestDto = new ObjectMapper().readValue(cpauths, TransactionRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "cpautorisation 500 malformed json expression " + cpauths + Util.formatException(e));
			return Util.getMsgErrorV2(folder, file, null, "cpautorisation 500 malformed json expression", null);
		}

		String cvv, acqcode, acq_type, date, rrn, heure, authnumber, websiteid, cardnumber = "", currency, recurring,expirydate,
				transactiontype, promoCode, mesg_type, merc_codeactivite, merchant_city, processing_code, reason_code,merchant_name,
				transaction_condition, transactiondate, transactiontime, montanttrame, num_trs = "", operation_id = "";

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;


		String timeStamp = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS).format(new Date());

		autorisationService.logMessage(file, "cpautorisation_" + trsRequestDto.getOrderid() + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(trsRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Merchant misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"cpautorisation 500 Merchant misconfigured in DB or not existing", "");
		}

		DemandePaiementDto check_dmd = null;
		HistoAutoGateDto current_hist = null;
		// TODO: get demandepaiement id , check if exist
		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid());
		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err1));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Error during PaiementRequest", null);
		}
		if (check_dmd == null) {
			autorisationService.logMessage(file,
					"cpautorisation 500 PaiementRequest misconfigured in DB or not existing orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]");

			return Util.getMsgErrorV2(folder, file, trsRequestDto,
					"cpautorisation 500 PaiementRequest misconfigured in DB or not existing", "15");
		}

		if(trsRequestDto.getAuthnumber() == null || trsRequestDto.getAuthnumber().equals("")) {
			cardnumber = check_dmd.getDemPan();
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmrAndHatPorteur(trsRequestDto.getOrderid(), trsRequestDto.getMerchantid(), cardnumber);
			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"cpautorisation 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrAndHatPorteur orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Error during HistoAutoGate", null);
			}
		} else {
			try {
				// TODO: get histoauto check if exist
				current_hist = histoAutoGateService.findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(trsRequestDto.getOrderid(),
						trsRequestDto.getAuthnumber(), trsRequestDto.getMerchantid(), "00");

			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"cpautorisation 500 Error during HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep orderid:["
								+ trsRequestDto.getOrderid() + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Error during HistoAutoGate", null);
			}
		}

		if (current_hist == null) {
			autorisationService.logMessage(file, "cpautorisation 500 Transaction not found orderid:[" + trsRequestDto.getOrderid()
					+ "] + and authnumber:[" + trsRequestDto.getAuthnumber() + "]" + "and merchantid:[" + trsRequestDto.getMerchantid() + "]");
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Transaction not found orderid:["
					+ trsRequestDto.getOrderid() + "] + and authnumber:[" + trsRequestDto.getAuthnumber() + "]" + "and merchantid:[" + trsRequestDto.getMerchantid() + "]",
					null);
		}

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Error during  date formatting for given orderid:[" + trsRequestDto.getOrderid()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err2));
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Error during  date formatting", null);
		}

		JSONObject jso = new JSONObject();
		String codrep = "", motif, merchnatidauth, dtdem = "";
		Double montantPreAuto = 0.00;
		Double montantCfr = 0.00;
		Double montantComplent = 0.00;
		String montantComplentStr = "0";
		String authnumberComplent = "";
		Double totalMontantCapture = 0.00;
		Double montantReste = 0.00;
		Double totalMontantAnnul = 0.00;
		try {
			acqcode = current_merchant.getCmrCodbqe();
			acq_type = "0000";
			codrep = current_hist.getHatCoderep();
			motif = current_hist.getHatMtfref1() != null ? current_hist.getHatMtfref1() : "Approved";
			merchnatidauth = current_hist.getHatNumcmr();
			dtdem = check_dmd.getDemPan();
			montantPreAuto = current_hist.getHatMontant() == null ? 0.00 : current_hist.getHatMontant();
			montantCfr = Double.valueOf(trsRequestDto.getAmount());
			totalMontantCapture = current_hist.getHatMontantCapture() == null ? 0.00
					: current_hist.getHatMontantCapture() + montantCfr;
			totalMontantAnnul = current_hist.getHatMontantAnnul() == null ? 0.00 : current_hist.getHatMontantAnnul();
			montantReste = montantPreAuto - totalMontantCapture - totalMontantAnnul;
			autorisationService.logMessage(file, "totalMontantAnnul : " + totalMontantAnnul);
			autorisationService.logMessage(file, "totalMontantCapture : " + totalMontantCapture);
			autorisationService.logMessage(file, "montantReste : " + montantReste);

			trsRequestDto.setAuthnumber(current_hist.getHatNautemt());
			cardnumber = check_dmd.getDemPan();
			websiteid = trsRequestDto.getWebsiteid() == null ? "" : trsRequestDto.getWebsiteid();
			if (websiteid.equals("") || websiteid == null) {
				websiteid = check_dmd.getGalid();
			}
			if (!websiteid.equals(check_dmd.getGalid())) {
				websiteid = check_dmd.getGalid();
			}
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Error during authdata preparation orderid:[" + trsRequestDto.getOrderid() + "]" + Util.formatException(e));

			return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Error during authdata preparation",
					codrep);
		}
		if (current_hist.getHatEtat().equals('A') || current_hist.getHatEtat() == 'A') {
			autorisationService.logMessage(file,
					"You can't make the capture because pre-auth is already canceled");
			codrep = "96";
			motif = "You can't make the capture because pre-auth is already canceled";
		} else if (current_hist.getHatEtat().equals('T') || current_hist.getHatEtat() == 'T') {
			autorisationService.logMessage(file,
					"You can't make the cature because this transaction is already captured");
			motif = "You can't make the capture because this transaction is already captured";
			codrep = "96";
		} else {
			autorisationService.logMessage(file, "montantPreAuto : " + montantPreAuto);
			autorisationService.logMessage(file, "montantCfr : " + montantCfr);
			// TODO: toujours on fait la telecollecte auto dans la confirmation pre-auto (capture=
			// TODO: "Y")

			autorisationService.logMessage(file, "confirmer telecollecte montantPreAuto");
			if (codrep.equalsIgnoreCase("00")) {
				String capture_status = "N";
				int exp_flag = 0;

				Date current_date = null;
				current_date = new Date();
				autorisationService.logMessage(file, "Automatic capture start...");

				autorisationService.logMessage(file, "Getting authnumber");

				authnumber = current_hist.getHatNautemt();
				autorisationService.logMessage(file, "authnumber : [" + authnumber + "]");

				autorisationService.logMessage(file, "Getting authnumber");
				TransactionDto trs_check = null;

				try {
					trs_check = transactionService.findByTrsnumautAndTrsnumcmrAndTrscommande(authnumber, trsRequestDto.getMerchantid(),
							trsRequestDto.getOrderid());
				} catch (Exception ee) {

					autorisationService.logMessage(file,
							"trs_check trs_check exception e : [" + ee.toString() + "]");
				}

				if (trs_check != null && current_hist.getHatEtat().equals('T')) {
					// TODO: do nothing
					autorisationService.logMessage(file, "trs_check != null do nothing for now ...");
					motif = "Transaction already captured";
				} else {

					if (montantCfr <= montantPreAuto) {
						autorisationService.logMessage(file, "if (montantCfr <= montantPreAuto)");
						
						montantPreAuto = montantCfr;
						
						autorisationService.logMessage(file, "inserting into telec start ...");
						Integer idtelc = null;
						TelecollecteDto tlc = null;
						long lidtelc = 0;
						try {
							TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(trsRequestDto.getMerchantid());
							if (n_tlc == null) {
								// TODO: insert into telec
								autorisationService.logMessage(file, "getMAXTLC_N n_tlc = null");
								idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
								autorisationService.logMessage(file, "getMAX_ID idtelc : " + idtelc);

								if (idtelc != null) {
									lidtelc = idtelc.longValue() + 1;
								} else {
									lidtelc = 1;
								}
								tlc = new TelecollecteDto();
								tlc.setTlcNumtlcolcte(lidtelc);
								tlc.setTlcNumtpe(current_hist.getHatCodtpe());
								tlc.setTlcDatcrtfich(current_date);
								tlc.setTlcNbrtrans(new Double(1));
								tlc.setTlcGest("N");
								tlc.setTlcDatremise(current_date);
								tlc.setTlcNumremise(new Double(lidtelc));
								// TODO: tlc.setTlcNumfich(new Double(0));
								String tmpattern = "HH:mm";
								SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
								String stm = sftm.format(current_date);
								tlc.setTlcHeuremise(stm);
								tlc.setTlcCodbq(acqcode);
								tlc.setTlcNumcmr(trsRequestDto.getMerchantid());
								tlc.setTlcNumtpe(websiteid);

								autorisationService.logMessage(file, tlc.toString());

								tlc = telecollecteService.save(tlc);

								autorisationService.logMessage(file,
										"cpautorisation dateremise : " + tlc.getTlcDatremise());
								autorisationService.logMessage(file, "inserting into telec ok");
							} else {
								lidtelc = n_tlc.getTlcNumtlcolcte();
								double nbr_trs = n_tlc.getTlcNbrtrans();
								autorisationService.logMessage(file, "n_tlc !=null lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
								nbr_trs = nbr_trs + 1;
								n_tlc.setTlcNbrtrans(nbr_trs);
								autorisationService.logMessage(file, "increment lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
								telecollecteService.save(n_tlc);
							}
						} catch (DataIntegrityViolationException ex) {
							autorisationService.logMessage(file,"Conflit détecté lors de l'insertion de telecollecte, première tentative échouée." + Util.formatException(ex));
							autorisationService.logMessage(file,"Pause de 2 secondes avant la deuxième tentative");

							try {
								Thread.sleep(2000);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								autorisationService.logMessage(file,"Thread interrompu pendant le délai d'attente" + ie);
								return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 , operation failed please try again", null);
							}

							try {
								idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
								if (idtelc != null) {
									lidtelc = idtelc + 1;
								} else {
									lidtelc = 1;
								}
								autorisationService.logMessage(file,"Deuxième tentative lidtelc : " + lidtelc);
								tlc.setTlcNumtlcolcte(lidtelc);

								autorisationService.logMessage(file, tlc.toString());

							} catch (DataIntegrityViolationException ex2) {
								autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + Util.formatException(ex2));
								exp_flag = 1;
								autorisationService.logMessage(file, "inserting into telec ko..do nothing " + Util.formatException(ex2));
								codrep = "96";
								motif = "cpautorisation failed";							}
						} catch (Exception e) {
							exp_flag = 1;
							autorisationService.logMessage(file, "inserting into telec ko..do nothing " + Util.formatException(e));
							codrep = "96";
							motif = "cpautorisation failed";
						}
						if(!codrep.equals("96")) {
							try {
								// TODO: insert into transaction
								TransactionDto trs = new TransactionDto();
								trs.setTrsNumcmr(trsRequestDto.getMerchantid());
								trs.setTrsNumtlcolcte(Double.valueOf(lidtelc));

								String frmt_cardnumber = Util.formatagePan(cardnumber);
								trs.setTrsCodporteur(frmt_cardnumber);
								trs.setTrsMontant(montantPreAuto);

								current_date = new Date();
								Date current_date_1 = getDateWithoutTime(current_date);
								Date trs_date = dateFormatSimple.parse(check_dmd.getDemDateTime());
								Date trs_date_1 = getDateWithoutTime(trs_date);

								trs.setTrsDattrans(current_date_1);
								trs.setTrsNumaut(authnumber);
								trs.setTrsEtat("N");
								trs.setTrsDevise(current_hist.getHatDevise());
								trs.setTrsCertif("N");
								Integer idtrs = transactionService.getMAX_ID();
								long lidtrs = idtrs.longValue() + 1;
								trs.setTrsId(lidtrs);
								trs.setTrsCommande(trsRequestDto.getOrderid());
								trs.setTrsProcod("0");
								trs.setTrsGroupe(websiteid);
								trs.setTrsCodtpe(0.0);
								trs.setTrsNumbloc(0.0);
								trs.setTrsNumfact(0.0);
								transactionService.save(trs);

								// TODO: 2024-11-27 controle sur le montant à telecollecter
								if (montantReste > 0.00) {
									current_hist.setHatMontantCapture(totalMontantCapture);
									// TODO: on garde etat = E initial
									//current_hist.setHatEtat('T');
									current_hist.setHatdatetlc(current_date);
									current_hist.setOperateurtlc("mxplusapi");
									current_hist = histoAutoGateService.save(current_hist);
								} else if (montantReste == 0.00) {
									current_hist.setHatMontantCapture(totalMontantCapture);
									current_hist.setHatEtat('T');
									current_hist.setHatdatetlc(current_date);
									current_hist.setOperateurtlc("mxplusapi");
									current_hist = histoAutoGateService.save(current_hist);
								}

								motif = motif + " and captured";

								autorisationService.logMessage(file, "inserting into Trs ok");
								capture_status = "Y";
							} catch (Exception e) {
								autorisationService.logMessage(file,
										"cpautorisation 500 Error during insert into transaction for given authnumber:[" + trsRequestDto.getAuthnumber()
												+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

								return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 , operation failed please try again", null);
							}
						} else {
							codrep = "96";
							motif = "cpautorisation failed";
						}
					}

					// TODO: Check si le montant de la confirmation est supperieur au montant de la
					// TODO: pre-auto
					// TODO: Si le cas on doit debiter le client par le complement

					if (montantCfr > montantPreAuto) {

						autorisationService.logMessage(file, "if(montantCfr > montantPreAuto)");

						montantComplent = montantCfr - montantPreAuto;
						montantComplentStr = String.valueOf(montantComplent);
						Date trsdate = null;
						autorisationService.logMessage(file,
								"montantComplent => (montantCfr - montantPreAuto) : " + montantComplent);

						String orderidToDebite = Util.genCommande(trsRequestDto.getMerchantid());
						autorisationService.logMessage(file,
								"generate new commande To debite : " + orderidToDebite);

						int i_card_type = Util.getCardIss(cardnumber);
						DemandePaiementDto dmd = null;
						DemandePaiementDto dmdSaved = null;

						acq_type = "0000";
						reason_code = "H";
						transaction_condition = "6";
						mesg_type = "0";
						processing_code = "0";
						String xid = "";
						transactiontype = "0"; // TODO: 0 payment , P preauto
						currency = "504";
						cvv = check_dmd.getDemCvv() == null ? "" : check_dmd.getDemCvv();
						recurring = "N";
						if (transactiontype.equals("P")) {
							processing_code = "P";
						}
						expirydate = check_dmd.getDateexpnaps();

						try {
							dmd = new DemandePaiementDto();

							dmd.setComid(trsRequestDto.getMerchantid());
							dmd.setCommande(orderidToDebite);
							dmd.setDemPan(cardnumber);
							dmd.setDemCvv(cvv);
							dmd.setGalid(websiteid);
							dmd.setSuccessURL(check_dmd.getSuccessURL());
							dmd.setFailURL(check_dmd.getFailURL());
							dmd.setTypeCarte(i_card_type + "");
							if (montantComplentStr.equals("") || montantComplentStr == null) {
								montantComplentStr = "0";
							}
							if (montantComplentStr.contains(",")) {
								montantComplentStr = montantComplentStr.replace(",", ".");
							}
							dmd.setMontant(Double.parseDouble(montantComplentStr));
							dmd.setNom(check_dmd.getNom());
							dmd.setPrenom(check_dmd.getPrenom());
							dmd.setEmail(check_dmd.getEmail());
							dmd.setTel(check_dmd.getTel());
							dmd.setAddress(check_dmd.getAddress());
							dmd.setCity(check_dmd.getCity());
							dmd.setCountry(check_dmd.getCountry());
							dmd.setState(check_dmd.getState());
							dmd.setPostcode(check_dmd.getPostcode());
							// TODO: dmd.setDateexpnaps(expirydate);
							dmd.setLangue("E");
							dmd.setEtatDemande("INIT");

							formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
							formatter_2 = new SimpleDateFormat("HH:mm:ss");
							trsdate = new Date();
							transactiondate = formatter_1.format(trsdate);
							transactiontime = formatter_2.format(trsdate);
							// TODO: dmd.setDemDateTime(transactiondate + transactiontime);
							dmd.setDemDateTime(dateFormat.format(new Date()));
							if (recurring.equalsIgnoreCase("Y"))
								dmd.setIsCof("Y");
							if (recurring.equalsIgnoreCase("N"))
								dmd.setIsCof("N");

							dmd.setIsAddcard("N");
							dmd.setIsTokenized("N");
							dmd.setIsWhitelist("N");
							dmd.setIsWithsave("N");

							// TODO: generer token
							String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
							dmd.setTokencommande(tokencommande);
							// TODO: set transctiontype
							dmd.setTransactiontype(transactiontype);

							dmdSaved = demandePaiementService.save(dmd);
						} catch (Exception err1) {
							autorisationService.logMessage(file,
									"cpautorisation 500 Error during DEMANDE_PAIEMENT insertion for given orderid:["
											+ trsRequestDto.getOrderid() + "]" + Util.formatException(err1));

							return Util.getMsgErrorV2(folder, file, trsRequestDto,
									"The current operation was not successful, your account will not be debited, please try again .", null);
						}

						// TODO: 2024-03-05
						LinkRequestDto linkRequestDto = new LinkRequestDto();
						linkRequestDto.setMerchantid(trsRequestDto.getMerchantid());
						linkRequestDto.setOrderid(trsRequestDto.getOrderid());
						linkRequestDto.setAmount(trsRequestDto.getAmount());
						montanttrame = Util.formatMontantTrame(folder, file, montantComplentStr, orderidToDebite, trsRequestDto.getMerchantid(),
								linkRequestDto);

						merc_codeactivite = current_merchant.getCmrCodactivite();
						acqcode = current_merchant.getCmrCodbqe();
						merchant_name = Util.pad_merchant(trsRequestDto.getMerchantname(), 19, ' ');

						merchant_city = "MOROCCO        ";

						// TODO: ajout cavv (cavv+eci) xid dans la trame
						String champ_cavv = "";
						/*
						 * xid = threeDSServerTransID; if (cavv == null || eci == null) { champ_cavv =
						 * null; autorisationService.logMessage(file,
						 * "cavv == null || eci == null"); } else if (cavv != null && eci != null) {
						 * champ_cavv = cavv + eci; autorisationService.logMessage(file,
						 * "cavv != null && eci != null"); autorisationService.logMessage(file,
						 * "champ_cavv : [" + champ_cavv + "]"); } else {
						 * autorisationService.logMessage(file, "champ_cavv = null"); champ_cavv =
						 * null; }
						 */

						boolean cvv_present = checkCvvPresence(cvv);
						cvv_present = true; // TODO: a revoir
						String first_auth = "";
						long lrec_serie = 0;

						// TODO: controls
						autorisationService.logMessage(file, "Switch processing start ...");

						String tlv = "";
						autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

						if (!cvv_present) {
							autorisationService.logMessage(file,
									"cpautorisation 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

							return Util.getMsgErrorV2(folder, file, trsRequestDto,
									"cpautorisation 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
									"82");
						}

						// TODO: not reccuring , normal
						if (cvv_present) {
							autorisationService.logMessage(file,
									"not reccuring , normal cvv_present && !is_reccuring");
							try {
								tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
										.withField(Tags.tag3, processing_code)
										.withField(Tags.tag22, transaction_condition).withField(Tags.tag49, acq_type)
										.withField(Tags.tag14, montanttrame).withField(Tags.tag15, currency)
										.withField(Tags.tag23, reason_code).withField(Tags.tag18, "761454")
										.withField(Tags.tag42, expirydate).withField(Tags.tag16, date)
										.withField(Tags.tag17, heure).withField(Tags.tag10, merc_codeactivite)
										.withField(Tags.tag8, "0" + trsRequestDto.getMerchantid()).withField(Tags.tag9, trsRequestDto.getMerchantid())
										.withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
										.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
										.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
										.withField(Tags.tag168, xid).encode();

							} catch (Exception err4) {
								autorisationService.logMessage(file,
										"cpautorisation 500 Error during switch tlv buildup for given orderid:["
												+ orderidToDebite + "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err4));

								return Util.getMsgErrorV2(folder, file, trsRequestDto,
										"cpautorisation failed, the Switch is down.", "96");
							}
							autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");
						}

						autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

						String resp_tlv = "";
						int port = 0;
						String sw_s = "", s_port = "";
						int switch_ko = 0;
						try {
							s_port = portSwitch;
							sw_s = ipSwitch;

							port = Integer.parseInt(s_port);

							autorisationService.logMessage(file, "Switch TCP client V2 Connecting ...");

							SwitchTCPClientV2 switchTCPClient = new SwitchTCPClientV2(sw_s, port);

							boolean s_conn = switchTCPClient.isConnected();

							if (!s_conn) {
								autorisationService.logMessage(file, "Switch  malfunction cannot connect!!!");

								// TODO: return Util.getMsgErrorV2(folder, file, trsRequestDto,
								// TODO: "cpautorisation 500 Error Switch communication s_conn false", "96");
								motif = "cpautorisation pre-autorisation approved, but supplement amount failed";
								codrep = "96";
							}

							if (s_conn) {
								autorisationService.logMessage(file, "Switch Connected.");

								resp_tlv = switchTCPClient.sendMessage(tlv);

								autorisationService.logMessage(file, "Switch TLV Request end.");
								switchTCPClient.shutdown();
							}

						} catch (Exception e) {
							autorisationService.logMessage(file, "Switch  malfunction Exception!!!" + Util.formatException(e));
							switch_ko = 1;
							// TODO: return Util.getMsgErrorV2(folder, file, trsRequestDto,
							// TODO: "cpautorisation 500 Error Switch communication General Exception", "96");
							motif = "cpautorisation pre-autorisation approved, but supplement amount failed";
							codrep = "96";
						}

						String resp = resp_tlv;

						if (switch_ko == 0 && resp == null) {
							autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
							switch_ko = 1;
							autorisationService.logMessage(file,
									"cpautorisation 500 Error Switch null response" + "switch ip:[" + sw_s
											+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
							// TODO: return Util.getMsgErrorV2(folder, file, trsRequestDto, "Switch malfunction resp
							// TODO: null!!!", "96");
							motif = "cpautorisation pre-autorisation approved, but supplement amount failed";
							codrep = "96";
						}

						if (switch_ko == 0 && resp.length() < 3) {
							switch_ko = 1;

							autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
							autorisationService.logMessage(file,
									"cpautorisation 500 Error Switch short response length() < 3 " + "switch ip:["
											+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						}

						autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

						TLVParser tlvp = null;

						String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null, tag9_resp = null,
								tag14_resp = null, tag15_resp = null, tag16_resp = null, tag17_resp = null,
								tag66_resp = null, tag18_resp = null, tag19_resp = null, tag23_resp = null,
								tag20_resp = null, tag21_resp = null, tag22_resp = null, tag80_resp = null,
								tag98_resp = null;

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
								tag66_resp = tlvp.getTag(Tags.tag66); // TODO: f1
								tag18_resp = tlvp.getTag(Tags.tag18);
								tag19_resp = tlvp.getTag(Tags.tag19); // TODO: f2
								tag23_resp = tlvp.getTag(Tags.tag23);
								tag20_resp = tlvp.getTag(Tags.tag20);
								tag21_resp = tlvp.getTag(Tags.tag21);
								tag22_resp = tlvp.getTag(Tags.tag22);
								tag80_resp = tlvp.getTag(Tags.tag80);
								tag98_resp = tlvp.getTag(Tags.tag98);

							} catch (Exception e) {
								autorisationService.logMessage(file, "Switch  malfunction tlv parsing !!!" + Util.formatException(e));
								switch_ko = 1;
								autorisationService.logMessage(file,
										"cpautorisation 500 Error during tlv Switch response parse" + "switch ip:["
												+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
												+ "]");
							}

							// TODO: controle switch
							if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
								autorisationService.logMessage(file, "Switch  malfunction !!! tag1_resp == null");
								switch_ko = 1;
								autorisationService.logMessage(file,
										"cpautorisation 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
												+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
												+ resp_tlv + "]");
							}
						}
						autorisationService.logMessage(file, "Switch TLV Respnose Processed");

						String tag20_resp_verified = "";
						String tag19_res_verified = "";
						String tag66_resp_verified = "";
						tag20_resp_verified = tag20_resp;
						tag19_res_verified = tag19_resp;
						tag66_resp_verified = tag66_resp;
						String s_status, pan_auto = "";

						if (switch_ko == 1) {
							pan_auto = Util.formatagePan(cardnumber);
							autorisationService.logMessage(file,
									"getSWHistoAuto pan_auto/rrn/montantComplent/date/merchantid : " + pan_auto + "/"
											+ rrn + "/" + montantComplent + "/" + date + "/" + trsRequestDto.getMerchantid());
						}

						HistoAutoGateDto histComlement = null;
						Integer Ihist_id = null;
						if (tag20_resp == null) {
							tag20_resp = "";
						}
						if (tag20_resp.equalsIgnoreCase("00")) {

							autorisationService.logMessage(file, "SWITCH RESONSE CODE :[00]");
							try {
								autorisationService.logMessage(file, "update etat demande : SW_PAYE ...");

								dmdSaved.setEtatDemande("SW_PAYE");
								dmdSaved.setDemCvv("");
								demandePaiementService.save(dmdSaved);
							} catch (Exception e) {
								autorisationService.logMessage(file,
										"cpautorisation 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
												+ orderidToDebite + "]" + Util.formatException(e));
							}
							autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");

							autorisationService.logMessage(file, "inserting currauto into telec start ...");
							long lidtelc = 0;
							Integer idtelc = null;
							TelecollecteDto tlc = null;
							try {
								TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(trsRequestDto.getMerchantid());
								if (n_tlc == null) {
									// TODO: insert into telec
									idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
									autorisationService.logMessage(file, "getMAX_ID idtelc : " + idtelc);

									if (idtelc != null) {
										lidtelc = idtelc.longValue() + 1;
									} else {
										lidtelc = 1;
									}
									tlc = new TelecollecteDto();
									tlc.setTlcNumtlcolcte(lidtelc);
									tlc.setTlcNumtpe(current_hist.getHatCodtpe());
									tlc.setTlcDatcrtfich(current_date);
									tlc.setTlcNbrtrans(new Double(1));
									tlc.setTlcGest("N");
									tlc.setTlcDatremise(current_date);
									tlc.setTlcNumremise(new Double(lidtelc));
									// TODO: tlc.setTlc_numfich(new Double(0));
									String tmpattern = "HH:mm";
									SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
									String stm = sftm.format(current_date);
									tlc.setTlcHeuremise(stm);
									tlc.setTlcCodbq(acqcode);
									tlc.setTlcNumcmr(trsRequestDto.getMerchantid());
									tlc.setTlcNumtpe(websiteid);

									autorisationService.logMessage(file, tlc.toString());

									tlc = telecollecteService.save(tlc);

									autorisationService.logMessage(file,
											"cpautorisation dateremise : " + tlc.getTlcDatremise());
									autorisationService.logMessage(file, "inserting into telec ok");
								} else {
									lidtelc = n_tlc.getTlcNumtlcolcte();
									double nbr_trs = n_tlc.getTlcNbrtrans();
									autorisationService.logMessage(file, "n_tlc !=null lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
									nbr_trs = nbr_trs + 1;
									n_tlc.setTlcNbrtrans(nbr_trs);
									autorisationService.logMessage(file, "increment lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
									telecollecteService.save(n_tlc);
								}
							} catch (DataIntegrityViolationException ex) {
								autorisationService.logMessage(file,"Conflit détecté lors de l'insertion de telecollecte, première tentative échouée." + Util.formatException(ex));
								autorisationService.logMessage(file,"Pause de 2 secondes avant la deuxième tentative");

								try {
									Thread.sleep(2000);
								} catch (InterruptedException ie) {
									Thread.currentThread().interrupt();
									autorisationService.logMessage(file,"Thread interrompu pendant le délai d'attente" + ie);
									autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + ie);
									exp_flag = 1;
									codrep = "96";
									motif = "cpautorisation failed";
								}

								try {
									idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
									if (idtelc != null) {
										lidtelc = idtelc + 1;
									} else {
										lidtelc = 1;
									}
									autorisationService.logMessage(file,"Deuxième tentative lidtelc : " + lidtelc);
									tlc.setTlcNumtlcolcte(lidtelc);

									autorisationService.logMessage(file, tlc.toString());

								} catch (DataIntegrityViolationException ex2) {
									autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + Util.formatException(ex2));
									exp_flag = 1;
									autorisationService.logMessage(file, "inserting into telec ko..do nothing " + Util.formatException(ex2));
									codrep = "96";
									motif = "cpautorisation failed";							}
							}  catch (Exception e) {
								exp_flag = 1;
								autorisationService.logMessage(file, "inserting into telec ko..do nothing " + Util.formatException(e));
								codrep = "96";
								motif = "cpautorisation failed";
							}

							if(!codrep.equals("96")) {
								try {
									// TODO: insert into transaction
									TransactionDto trs = new TransactionDto();
									trs.setTrsNumcmr(trsRequestDto.getMerchantid());
									trs.setTrsNumtlcolcte(Double.valueOf(lidtelc));

									String frmt_cardnumber = Util.formatagePan(cardnumber);
									trs.setTrsCodporteur(frmt_cardnumber);
									trs.setTrsMontant(montantPreAuto);

									current_date = new Date();
									Date current_date_1 = getDateWithoutTime(current_date);
									Date trs_date = dateFormatSimple.parse(check_dmd.getDemDateTime());
									Date trs_date_1 = getDateWithoutTime(trs_date);

									trs.setTrsDattrans(current_date_1);
									trs.setTrsNumaut(authnumber);
									trs.setTrsEtat("N");
									trs.setTrsDevise(current_hist.getHatDevise());
									trs.setTrsCertif("N");
									Integer idtrs = transactionService.getMAX_ID();
									long lidtrs = idtrs.longValue() + 1;
									trs.setTrsId(lidtrs);
									trs.setTrsCommande(trsRequestDto.getOrderid());
									trs.setTrsProcod("0");
									trs.setTrsGroupe(websiteid);
									trs.setTrsCodtpe(0.0);
									trs.setTrsNumbloc(0.0);
									trs.setTrsNumfact(0.0);
									transactionService.save(trs);

									current_hist.setHatMontantCapture(montantPreAuto);
									current_hist.setHatEtat('T');
									current_hist.setHatdatetlc(current_date);
									current_hist.setOperateurtlc("mxplusapi");
									current_hist = histoAutoGateService.save(current_hist);

									motif = motif + " and captured";

									autorisationService.logMessage(file, "inserting into Trs ok");
									capture_status = "Y";
								} catch (Exception e) {
									autorisationService.logMessage(file,
											"cpautorisation 500 Error during insert into transaction for given authnumber:[" + trsRequestDto.getAuthnumber()
													+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

									return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 , operation failed please try again", null);
								}

								autorisationService.logMessage(file, "Insert into Histogate...");
							} else {
								autorisationService.logMessage(file, "cpautorisation pre-autorisation failed to insert into HistoautoGate");
								codrep = "96";
								motif = "cpautorisation failed";
							}

							try {
								histComlement = new HistoAutoGateDto();
								Date curren_date_hist = new Date();
								int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

								s_status = "";
								try {
									CodeReponseDto codeReponseDto = codeReponseService
											.findByRpcCode(tag20_resp_verified);
									autorisationService.logMessage(file, "" + codeReponseDto);
									if (codeReponseDto != null) {
										s_status = codeReponseDto.getRpcLibelle();
									}
								} catch (Exception ee) {
									autorisationService.logMessage(file,
											"cpautorisation 500 Error codeReponseDto null" + Util.formatException(ee));
								}

								autorisationService.logMessage(file,
										"get status Switch status : [" + s_status + "]");

								autorisationService.logMessage(file, "formatting pan...");

								pan_auto = Util.formatagePan(cardnumber);

								autorisationService.logMessage(file, "HistoAutoGate data filling start ...");

								autorisationService.logMessage(file, "websiteid : " + websiteid);

								Date current_date_1 = getDateWithoutTime(curren_date_hist);
								histComlement.setHatDatdem(current_date_1);

								histComlement.setHatHerdem(new SimpleDateFormat("HH:mm").format(curren_date_hist));
								histComlement.setHatMontant(montantComplent);
								histComlement.setHatNumcmr(trsRequestDto.getMerchantid());
								histComlement.setHatCoderep(tag20_resp_verified);
								tag20_resp = tag20_resp_verified;
								histComlement.setHatDevise(currency);
								histComlement.setHatBqcmr(acqcode);
								histComlement.setHatPorteur(pan_auto);
								histComlement.setHatMtfref1(s_status);
								histComlement.setHatNomdeandeur(websiteid);
								histComlement.setHatNautemt(tag19_res_verified); // TODO: f2
								tag19_resp = tag19_res_verified;
								if (tag22_resp != null)
									histComlement.setHatProcode(tag22_resp.charAt(0));
								else
									histComlement.setHatProcode('6');
								histComlement.setHatExpdate(expirydate);
								histComlement.setHatRepondeur(tag21_resp);
								histComlement.setHatTypmsg("3");
								histComlement.setHatRrn(tag66_resp_verified); // TODO: f1
								tag66_resp_verified = tag66_resp;
								histComlement.setHatEtat('E');
								if (websiteid.equals("")) {
									histComlement.setHatCodtpe("1");
								} else {
									histComlement.setHatCodtpe(websiteid);
								}
								histComlement.setHatMcc(merc_codeactivite);
								histComlement.setHatNumCommande(orderidToDebite);
								histComlement.setHatNumdem(new Long(numTransaction));

								if (checkCvvPresence(cvv)) {
									histComlement.setIsCvvVerified("Y");
								} else {
									histComlement.setIsCvvVerified("N");
								}

								histComlement.setIs3ds("N");
								histComlement.setIsAddcard("N");
								histComlement.setIsWhitelist("N");
								histComlement.setIsWithsave("N");
								histComlement.setIsTokenized("N");

								if (recurring.equalsIgnoreCase("Y"))
									histComlement.setIsCof("Y");
								if (recurring.equalsIgnoreCase("N"))
									histComlement.setIsCof("N");

								autorisationService.logMessage(file, "HistoAutoGate data filling end ...");

								autorisationService.logMessage(file, "HistoAutoGate Saving ...");

								histComlement = histoAutoGateService.save(histComlement);

								autorisationService.logMessage(file,
										"hatNomdeandeur : " + histComlement.getHatNomdeandeur());

								autorisationService.logMessage(file, "HistoAutoGate OK.");

							} catch (Exception e) {
								autorisationService.logMessage(file,
										"cpautorisation 500 Error during  insert in histoautogate for given orderid:["
												+ orderidToDebite + "]" + Util.formatException(e));
								codrep = "96";
								motif = "cpautorisation failed";
							}


							current_date = new Date();
							autorisationService.logMessage(file, "Automatic capture start...");

							autorisationService.logMessage(file, "Getting authnumberComplent");

							authnumberComplent = histComlement.getHatNautemt();
							autorisationService.logMessage(file,
									"authnumberComplent : [" + authnumberComplent + "]");

							autorisationService.logMessage(file, "Getting authnumberComplent");

							autorisationService.logMessage(file, "inserting complement into telec start ...");
							lidtelc = 0;
							idtelc = null;
							tlc = null;

							if(!codrep.equals("96")) {
								try {
									TelecollecteDto n_tlc = telecollecteService.getMAXTLC_N(trsRequestDto.getMerchantid());
									if (n_tlc == null) {
										// TODO: insert into telec
										idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
										autorisationService.logMessage(file, "getMAX_ID idtelc : " + idtelc);

										if (idtelc != null) {
											lidtelc = idtelc.longValue() + 1;
										} else {
											lidtelc = 1;
										}
										tlc = new TelecollecteDto();
										tlc.setTlcNumtlcolcte(lidtelc);

										tlc.setTlcNumtpe(histComlement.getHatCodtpe());

										tlc.setTlcDatcrtfich(current_date);
										tlc.setTlcNbrtrans(new Double(1));
										tlc.setTlcGest("N");

										tlc.setTlcDatremise(current_date);
										tlc.setTlcNumremise(new Double(lidtelc));
										// TODO: tlc.setTlc_numfich(new Double(0));
										String tmpattern = "HH:mm";
										SimpleDateFormat sftm = new SimpleDateFormat(tmpattern);
										String stm = sftm.format(current_date);
										tlc.setTlcHeuremise(stm);

										tlc.setTlcCodbq(acqcode);
										tlc.setTlcNumcmr(trsRequestDto.getMerchantid());
										tlc.setTlcNumtpe(websiteid);

										autorisationService.logMessage(file, tlc.toString());

										telecollecteService.save(tlc);
									} else {
										lidtelc = n_tlc.getTlcNumtlcolcte();
										double nbr_trs = n_tlc.getTlcNbrtrans();
										autorisationService.logMessage(file, "n_tlc !=null lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
										nbr_trs = nbr_trs + 1;
										n_tlc.setTlcNbrtrans(nbr_trs);
										autorisationService.logMessage(file, "increment lidtelc/nbr_trs " + lidtelc + "/" + nbr_trs);
										telecollecteService.save(n_tlc);
									}
								} catch (DataIntegrityViolationException ex) {
									autorisationService.logMessage(file,"Conflit détecté lors de l'insertion de telecollecte, première tentative échouée." + Util.formatException(ex));
									autorisationService.logMessage(file,"Pause de 2 secondes avant la deuxième tentative");

									try {
										Thread.sleep(2000);
									} catch (InterruptedException ie) {
										Thread.currentThread().interrupt();
										autorisationService.logMessage(file,"Thread interrompu pendant le délai d'attente" + ie);
										autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + ie);
										exp_flag = 1;
										codrep = "96";
										motif = "cpautorisation failed";
									}

									try {
										idtelc = telecollecteService.getMAX_ID(trsRequestDto.getMerchantid());
										if (idtelc != null) {
											lidtelc = idtelc + 1;
										} else {
											lidtelc = 1;
										}
										autorisationService.logMessage(file,"Deuxième tentative lidtelc : " + lidtelc);
										tlc.setTlcNumtlcolcte(lidtelc);

										autorisationService.logMessage(file, tlc.toString());

									} catch (DataIntegrityViolationException ex2) {
										autorisationService.logMessage(file,"Conflit persistant lors de la deuxième tentative d'insertion de telecollecte." + Util.formatException(ex2));
										exp_flag = 1;
										autorisationService.logMessage(file, "inserting into telec ko..do nothing " + Util.formatException(ex2));
										codrep = "96";
										motif = "cpautorisation failed";							}
								} catch (Exception e) {
									exp_flag = 1;
									autorisationService.logMessage(file, "inserting into telec ko..do nothing " + Util.formatException(e));
									codrep = "96";
									motif = "cpautorisation pre-autorisation failed";
								}
								if(!codrep.equals("96")) {
									try {
										// TODO: insert into transaction for complement (montantComplent)
										TransactionDto trs = new TransactionDto();
										trs.setTrsNumcmr(trsRequestDto.getMerchantid());
										trs.setTrsNumtlcolcte(Double.valueOf(lidtelc));

										String frmt_cardnumber = Util.formatagePan(cardnumber);
										trs.setTrsCodporteur(frmt_cardnumber);
										trs.setTrsMontant(montantComplent);

										current_date = new Date();
										Date current_date_1 = getDateWithoutTime(current_date);
										trs.setTrsDattrans(current_date_1);

										trs.setTrsNumaut(authnumberComplent);
										trs.setTrsEtat("N");
										trs.setTrsDevise(histComlement.getHatDevise());
										trs.setTrsCertif("N");
										Integer idtrs = transactionService.getMAX_ID();
										long lidtrs = idtrs.longValue() + 1;
										trs.setTrsId(lidtrs);
										trs.setTrsCommande(orderidToDebite);
										trs.setTrsProcod("0");
										trs.setTrsGroupe(websiteid);
										trs.setTrsCodtpe(0.0);
										trs.setTrsNumbloc(0.0);
										trs.setTrsNumfact(0.0);
										transactionService.save(trs);

										histComlement.setHatMontantCapture(montantComplent);
										histComlement.setHatEtat('T');
										histComlement.setHatdatetlc(current_date);
										histComlement.setOperateurtlc("mxplusapi");
										histComlement = histoAutoGateService.save(histComlement);

										motif = motif + " and captured";

										autorisationService.logMessage(file, "inserting into telec ok");
										capture_status = "Y";
									} catch (Exception e) {
										autorisationService.logMessage(file,
												"cpautorisation 500 Error during insert into transaction for given authnumber:[" + trsRequestDto.getAuthnumber()
														+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(e));

										return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 , operation failed please try again", null);
									}
								} else {
									autorisationService.logMessage(file,
											"cpautorisation pre-autorisation failed to insert into transaction");
									codrep = "96";
									motif = "cpautorisation pre-autorisation failed";
								}

							} else {
								autorisationService.logMessage(file, "cpautorisation pre-autorisation failed to insert into histComlement ");
								codrep = "96";
								motif = "cpautorisation pre-autorisation failed";
							}

						} else {
							codrep = tag20_resp;
							autorisationService.logMessage(file, "transaction declined !!! ");
							autorisationService.logMessage(file, "SWITCH RESONSE CODE :[" + codrep + "]");
							motif = "cpautorisation pre-autorisation failed";
						}
					}
				}
				if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
					capture_status = "N";

				autorisationService.logMessage(file, "Automatic capture end.");
			} else {
				autorisationService.logMessage(file, "transaction declined !!! ");
				autorisationService.logMessage(file, "SWITCH RESONSE CODE :[" + codrep + "]");
			}
		}
		String uuid_captureid = "", capture_id = "";
		String dtpattern, sdt = "", tmpattern, stm = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;

		try {
			uuid_captureid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			capture_id = uuid_captureid.substring(uuid_captureid.length() - 22);
			operation_id = capture_id;
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
		} catch (Exception err8) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Error during jso data preparationfor given authnumber:[" + trsRequestDto.getAuthnumber()
							+ "] and merchantid:[" + trsRequestDto.getMerchantid() + "]" + Util.formatException(err8));
		}

		autorisationService.logMessage(file, "Preparing cpautorisation api response");
		try {

			// TODO: Transaction info
			jso.put("statuscode", codrep);
			jso.put("status", motif);
			jso.put("etataut", "Y");
			jso.put("orderid", trsRequestDto.getOrderid());
			jso.put("amount", trsRequestDto.getAmount());
			jso.put("transactiondate", sdt);
			jso.put("transactiontime", stm);
			jso.put("authnumber", trsRequestDto.getAuthnumber());
			jso.put("paymentid", trsRequestDto.getPaymentid());
			jso.put("capture_id", capture_id);
			jso.put("transactionid", trsRequestDto.getTransactionid());
			jso.put("operation_id", operation_id);
			jso.put("acquRefNbr", "11010");

			// TODO: Merchant info
			jso.put("merchantid", merchnatidauth);
			jso.put("merchantname", trsRequestDto.getMerchantname());
			jso.put("websitename", trsRequestDto.getWebsiteName());
			jso.put("websiteid", websiteid);

			// TODO: Card info
			jso.put("cardnumber", Util.formatCard(cardnumber));

			// TODO: Client info
			jso.put("fname", trsRequestDto.getFname());
			jso.put("lname", trsRequestDto.getLname());
			jso.put("email", trsRequestDto.getEmail());

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");

		} catch (Exception jsouterr) {
			autorisationService.logMessage(file,
					"cpautorisation 500 Error during jso out processing given authnumber:[" + trsRequestDto.getAuthnumber() + "]"
							+ jsouterr);
			return Util.getMsgErrorV2(folder, file, trsRequestDto, "cpautorisation 500 Error during jso out processing",
					codrep);
		}

		autorisationService.logMessage(file, "cpautorisation api response :  [" + jso.toString() + "]");

		autorisationService.logMessage(file, "*********** End cpautorisation() ************** ");

		return jso.toString();
	}

	@SuppressWarnings("all")
	public JSONObject verifieToken(String securtoken24, String file) {
		JSONObject jso = new JSONObject();
		if (!securtoken24.equals("")) {
			JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
			String token = securtoken24;
			try {
				String userFromToken = jwtTokenUtil.getUsernameFromToken(token, secret);
				Date dateExpiration = jwtTokenUtil.getExpirationDateFromToken(token, secret);
				Boolean isTokenExpired = jwtTokenUtil.isTokenExpired(token, secret);

				autorisationService.logMessage(file, "userFromToken generated : " + userFromToken);
				String dateSysStr = dateFormat.format(new Date());
				autorisationService.logMessage(file, "dateSysStr : " + dateSysStr);
				autorisationService.logMessage(file, "dateExpiration : " + dateExpiration);
				String dateExpirationStr = dateFormat.format(dateExpiration);
				autorisationService.logMessage(file, "dateExpirationStr : " + dateExpirationStr);
				String condition = isTokenExpired == false ? "NO" : "YES";
				autorisationService.logMessage(file, "token is expired : " + condition);
				if (condition.equalsIgnoreCase("YES")) {
					autorisationService.logMessage(file, "Error 500 securtoken24 is expired");

					// TODO: Transaction info
					jso.put("statuscode", "17");
					jso.put("status", "Error 500 securtoken24 is expired");

					return jso;
				} else {
					jso.put("statuscode", "00");
				}
			} catch (Exception ex) {
				// TODO: Transaction info
				jso.put("statuscode", "17");
				jso.put("status", "Error 500 securtoken24 " + ex.getMessage());
				logger.info(jso.get("status"));

				return jso;
			}
		}
		return jso;
	}

	private boolean isReccuringCheck(String recurring) {
		return recurring.equalsIgnoreCase("Y");
	}

	private boolean checkCvvPresence(String cvv) {
		return cvv != null && cvv.length() == 3;
	}

	private long getTransactionSerie(String merchantID, String cardNumber) {
		ReccuringTransactionDto recTrs = recService.findLastRecByCardNumberAndMerchantID(cardNumber, merchantID);
		return recTrs == null ? 0 : recTrs.getReccuringNumber();
	}

	private String getFirstTransactionAuth(String merchantID, String cardNumber) {
		ReccuringTransactionDto recTrs = recService.findFirstByCardNumberAndMerchantID(cardNumber, merchantID);
		return recTrs == null ? "" : recTrs.getFirstTransactionNumber();
	}

	private boolean isFirstTransaction(String merchantID, String cardNumber) {
		ReccuringTransactionDto recTrs = recService.findFirstByCardNumberAndMerchantID(cardNumber, merchantID);
		return recTrs == null;
	}

	@SuppressWarnings("deprecation")
	private Date getDateWithoutTime(Date d) {

		if (d == null)
			d = new Date();
		Date dNotime = null;
		try {

			dNotime = new Date(d.getYear(), d.getMonth(), d.getDate());

			dNotime.setHours(0);
			dNotime.setMinutes(0);
			dNotime.setSeconds(0);

		} catch (Exception e) {
			return d; // TODO: leave it as it is if not null
		}
		return dNotime;

	}

}
