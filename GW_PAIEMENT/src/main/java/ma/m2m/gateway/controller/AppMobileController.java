package ma.m2m.gateway.controller;

import static ma.m2m.gateway.Utils.StringUtils.isNullOrEmpty;
import static ma.m2m.gateway.config.FlagActivation.ACTIVE;
import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.Cartes;
import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.dto.MonthDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.risk.GWRiskAnalysis;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.ControlRiskCmrService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.EmetteurService;
import ma.m2m.gateway.service.GalerieService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.service.TelecollecteService;
import ma.m2m.gateway.service.TransactionService;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.CRes;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-03
 */

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class AppMobileController {

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
	
	@Value("${key.FRAIS_CCB}")
	private String fraisCCB;
	
	@Value("${key.TIMEOUT}")
	private int timeout;

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
	private InfoCommercantService infoCommercantService;

	@Autowired
	TelecollecteService telecollecteService;

	@Autowired
	CardtokenService cardtokenService;

	@Autowired
	private ControlRiskCmrService controlRiskCmrService;

	@Autowired
	private EmetteurService emetteurService;

	@Autowired
	GalerieService galerieService;

	@Autowired
	CodeReponseService codeReponseService;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd");

	public AppMobileController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "MB_CCB" + randomWithSplittableRandom;
		// date of folder logs
		dateF = LocalDateTime.now(ZoneId.systemDefault());
		folder = dateF.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	@PostMapping("/napspayment/ccb/acs")
	public String processRequestMobile(HttpServletRequest request, HttpServletResponse response, Model model, HttpSession session)
			throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "MB_R_ACS_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "Start processRequestMobile ()");
		System.out.println("Start processRequestMobile ()");
		CRes cleanCres = new CRes();
		String msgRefus = "";
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		String page = "index";
		try {
			String encodedCres = request.getParameter("cres");
			System.out.println("ACSController RETOUR ACS =====> encodedCres : " + encodedCres);
			Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> encodedCres : " + encodedCres);

			String decodedCres = "";

			decodedCres = new String(Base64.decodeBase64(encodedCres.getBytes()));
			if (decodedCres.indexOf("}") != -1) {
				decodedCres = decodedCres.substring(0, decodedCres.indexOf("}") + 1);
			}
			Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> decodedCres : " + decodedCres);
			System.out.println("ACSController RETOUR ACS =====> decodedCres : " + decodedCres);

			cleanCres = gson.fromJson(decodedCres, CRes.class);
			Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> cleanCres : " + cleanCres);

			Util.writeInFileTransaction(folder, file, "transStatus/threeDSServerTransID : " + cleanCres.getTransStatus()
					+ "/" + cleanCres.getThreeDSServerTransID());

			// just for test
			// cleanCres.setTransStatus("N");

			if (cleanCres.getTransStatus().equals("Y") || cleanCres.getTransStatus().equals("N")) {
				System.out.println("ACSController RETOUR ACS =====> callThree3DSSAfterACS ");
				Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> callThree3DSSAfterACS ");

				ThreeDSecureResponse threeDsecureResponse = autorisationService.callThree3DSSAfterACS(decodedCres,
						folder, file);

				DemandePaiementDto dmd = new DemandePaiementDto();
				JSONObject jso = new JSONObject();
				String[] mm;
				String[] m;
				SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;

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
				String processing_code = "";
				String acq_type = "";
				String merchant_city = "";
				String reason_code = "";
				String transaction_condition = "";
				String mesg_type = "";
				String currency = "504";

				String capture = "";
				String orderid = "";
				String recurring = "";
				String amount = "";
				String promoCode = "";
				String transactionid = "";
				String capture_id = "";
				String merchantid = "";
				String merchantname = "";
				String websiteName = "";
				String websiteid = "";
				String callbackUrl = "";
				String cardnumber = "";
				String token = "";
				String expirydate = "";
				String holdername = "";
				String cvv = "";
				String fname = "";
				String lname = "";
				String email = "";
				String country = "";
				String phone = "";
				String city = "";
				String state = "";
				String zipcode = "";
				String address = "";
				String merc_codeactivite = "";
				String acqcode = "";
				String merchant_name = "";
				String transactiondate = "";
				String transactiontime = "";
				String date = "";
				String rrn = "";
				String heure = "";
				String montanttrame = "";
				String montantRechgtrame = "", cartenaps = "", dateExnaps = "";
				String num_trs = "";
				String successURL = "";
				String failURL;

				if (threeDsecureResponse != null && threeDsecureResponse.getEci() != null) {
					if (threeDsecureResponse.getEci().equals("05") || threeDsecureResponse.getEci().equals("02")
							|| threeDsecureResponse.getEci().equals("06")
							|| threeDsecureResponse.getEci().equals("01")) {
						
						Util.writeInFileTransaction(folder, file,
								"if(eci=05) || eci=02 || eci=06 || eci=01) : continue le processus");
						
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
							Util.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
							Util.writeInFileTransaction(folder, file,
									"demandePaiement after update MPI_KO idDemande null");
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (MPI_KO), votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

						if (dmd == null) {
							Util.writeInFileTransaction(folder, file,
									"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
											+ idDemande);
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}
						
						// 2024-06-04
						// gestion expiration de la session on recupere la date en millisecond
						if(dmd.getTimeoutURL() != null) {
							Long paymentStartTime = Long.parseLong(dmd.getTimeoutURL());
							Util.writeInFileTransaction(folder, file, "paymentStartTime : " + paymentStartTime);
						    if (paymentStartTime != null) {
						        long currentTime = System.currentTimeMillis();
						        long elapsedTime = currentTime - paymentStartTime;
						        Util.writeInFileTransaction(folder, file, "currentTime : " + currentTime);
						        Util.writeInFileTransaction(folder, file, "elapsedTime : " + elapsedTime);
						        // Check if more than 5 minutes (300000 milliseconds) have passed
						        int timeoutF = timeout;
						        if (elapsedTime > timeoutF) {
									Util.writeInFileTransaction(folder, file, "Page expirée Time > 5min");
									demandeDtoMsg.setMsgRefus("Votre session de paiement a expiré. Veuillez réessayer.");
									demandeDtoMsg.setIddemande(dmd.getIddemande());
									session.setAttribute("idDemande", dmd.getIddemande());								
									model.addAttribute("demandeDto", demandeDtoMsg);
									dmd.setEtat_demande("TimeOut");
									dmd.setDem_cvv("");
									dmd = demandePaiementService.save(dmd);	            
									page = "timeout";
									
									Util.writeInFileTransaction(folder, file, "*********** Fin processRequest () ************** ");
									System.out.println("*********** Fin processRequest () ************** ");
									
									return page;
						        }
						    }
						}
					 // 2024-06-04

						// Merchnat info
						merchantid = dmd.getComid();
						websiteid = dmd.getGalid();

						String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

						Util.writeInFileTransaction(folder, file, "authorization_" + orderid + timeStamp);

						CommercantDto current_merchant = null;
						try {
							current_merchant = commercantService.findByCmrNumcmr(merchantid);
						} catch (Exception e) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]"
											+ e);
							demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						if (current_merchant == null) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid
											+ "]");
							demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						if (current_merchant.getCmrCodactivite() == null) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid
											+ "]");
							demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						if (current_merchant.getCmrCodbqe() == null) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid
											+ "]");
							demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}
						InfoCommercantDto current_infoCommercant = null;

						try {
							current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
						} catch (Exception e) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 InfoCommercant misconfigured in DB or not existing orderid:["
											+ orderid + "] and merchantid:[" + merchantid + "] and websiteid:["
											+ websiteid + "]" + e);
							demandeDtoMsg
									.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						if (current_infoCommercant == null) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 InfoCommercantDto misconfigured in DB or not existing orderid:["
											+ orderid + "] and merchantid:[" + merchantid + "] and websiteid:["
											+ websiteid + "]");
							demandeDtoMsg
									.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						// Info
						merc_codeactivite = current_merchant.getCmrCodactivite();
						acqcode = current_merchant.getCmrCodbqe();

						orderid = dmd.getCommande();
						recurring = "";
						amount = String.valueOf(dmd.getMontant());
						promoCode = "";
						transactionid = "";

						// Merchnat info
						merchantid = dmd.getComid();
						merchantname = current_merchant.getCmrNom();
						merchant_name = Util.pad_merchant(merchantname, 19, ' ');
						websiteName = "";
						callbackUrl = dmd.getCallbackURL();
						successURL = dmd.getSuccessURL();
						failURL = dmd.getFailURL();

						// Card info
						cardnumber = dmd.getDem_pan();
						token = dmd.getToken();
						expirydate = expiry;
						holdername = "";
						cvv = dmd.getDem_cvv();
						cartenaps = dmd.getCartenaps();
						dateExnaps = dmd.getDateexpnaps();

						// Client info
						fname = dmd.getNom();
						lname = dmd.getPrenom();
						email = dmd.getEmail();
						country = dmd.getCountry();
						phone = dmd.getTel();
						city = dmd.getCity();
						state = dmd.getState();
						zipcode = dmd.getPostcode();
						address = dmd.getAddress();

						try {
							formatheure = new SimpleDateFormat("HHmmss");
							formatdate = new SimpleDateFormat("ddMMyy");
							date = formatdate.format(new Date());
							heure = formatheure.format(new Date());
							rrn = Util.getGeneratedRRN();
						} catch (Exception err2) {
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error during  date formatting for given orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "]" + err2);
							demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						if (reponseMPI.equals("") || reponseMPI == null) {
							dmd.setEtat_demande("MPI_KO");
							dmd.setDem_cvv("");
							demandePaiementService.save(dmd);
							Util.writeInFileTransaction(folder, file,
									"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
							Util.writeInFileTransaction(folder, file, "Response 3DS is null");
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (MPI_KO reponseMPI null), votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
							return page;
						}

						if (reponseMPI.equals("Y")) {
							// ********************* Frictionless responseMPI equal Y *********************
							Util.writeInFileTransaction(folder, file,
									"********************* responseMPI equal Y *********************");

							// 2024-03-05
							montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, dmd, page, model);
						
							// 2024-03-05
							montantRechgtrame = formatMontantRechargeTrame(folder, file, amount, orderid, merchantid, dmd, page, model);

							boolean cvv_present = check_cvv_presence(cvv);
							boolean is_reccuring = is_reccuring_check(recurring);
							boolean is_first_trs = true;

							String first_auth = "";
							long lrec_serie = 0;

							merchant_city = "MOROCCO        ";
							Util.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

							acq_type = "0000";
							processing_code = dmd.getTransactiontype();
							reason_code = "H";
							transaction_condition = "6";
							mesg_type = "0";

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

							// controls
							Util.writeInFileTransaction(folder, file, "Switch processing start ...");

							String tlv = "";
							Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

							if (!cvv_present && !is_reccuring) {
								dmd.setDem_cvv("");
								demandePaiementService.save(dmd);
								Util.writeInFileTransaction(folder, file,
										"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (cvv doit être présent dans la transaction normale), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							// not reccuring , normal
							if (cvv_present && !is_reccuring) {
								Util.writeInFileTransaction(folder, file,
										"not reccuring , normal cvv_present && !is_reccuring");
								try {
									// tag 046 tlv info carte naps
									String tlvCCB = new TLVEncoder().withField(Tags.tag1, cartenaps)
											.withField(Tags.tag14, montantRechgtrame).withField(Tags.tag42, dateExnaps)
											.encode();
									// tlv total ccb
									tlv = new TLVEncoder().withField(Tags.tag0, mesg_type)
											.withField(Tags.tag1, cardnumber).withField(Tags.tag3, processing_code)
											.withField(Tags.tag22, transaction_condition)
											.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
											.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
											.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
											.withField(Tags.tag16, date).withField(Tags.tag17, heure)
											.withField(Tags.tag10, merc_codeactivite)
											.withField(Tags.tag8, "0" + merchantid).withField(Tags.tag9, merchantid)
											.withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
											.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
											.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
											.withField(Tags.tag168, xid).withField(Tags.tag46, tlvCCB).encode();

									Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
									Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
									Util.writeInFileTransaction(folder, file,
											"tag3_request : [" + processing_code + "]");
									Util.writeInFileTransaction(folder, file,
											"tag22_request : [" + transaction_condition + "]");
									Util.writeInFileTransaction(folder, file, "tag49_request : [" + acq_type + "]");
									Util.writeInFileTransaction(folder, file, "tag14_request : [" + montanttrame + "]");
									Util.writeInFileTransaction(folder, file, "tag15_request : [" + currency + "]");
									Util.writeInFileTransaction(folder, file, "tag23_request : [" + reason_code + "]");
									Util.writeInFileTransaction(folder, file, "tag18_request : [761454]");
									Util.writeInFileTransaction(folder, file, "tag42_request : [" + expirydate + "]");
									Util.writeInFileTransaction(folder, file, "tag16_request : [" + date + "]");
									Util.writeInFileTransaction(folder, file, "tag17_request : [" + heure + "]");
									Util.writeInFileTransaction(folder, file,
											"tag10_request : [" + merc_codeactivite + "]");
									Util.writeInFileTransaction(folder, file, "tag8_request : [0" + merchantid + "]");
									Util.writeInFileTransaction(folder, file, "tag9_request : [" + merchantid + "]");
									Util.writeInFileTransaction(folder, file, "tag66_request : [" + rrn + "]");
									Util.writeInFileTransaction(folder, file, "tag67_request : [" + cvv + "]");
									Util.writeInFileTransaction(folder, file,
											"tag11_request : [" + merchant_name + "]");
									Util.writeInFileTransaction(folder, file,
											"tag12_request : [" + merchant_city + "]");
									Util.writeInFileTransaction(folder, file, "tag90_request : [" + acqcode + "]");
									Util.writeInFileTransaction(folder, file, "tag167_request : [" + champ_cavv + "]");
									Util.writeInFileTransaction(folder, file, "tag168_request : [" + xid + "]");
									Util.writeInFileTransaction(folder, file, "tag46_request : [" + tlvCCB + "]");

								} catch (Exception err4) {
									dmd.setDem_cvv("");
									demandePaiementService.save(dmd);
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during switch tlv buildup for given orderid:["
													+ orderid + "] and merchantid:[" + merchantid + "]" + err4);
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "result";
									Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
									System.out.println("Fin processRequestMobile ()");
									return page;
								}

								Util.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

							}

							// reccuring
							if (is_reccuring) {
								Util.writeInFileTransaction(folder, file, "reccuring");
							}

							Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

							String resp_tlv = "";
//							SwitchTCPClient sw = SwitchTCPClient.getInstance();
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
									dmd.setDem_cvv("");
									demandePaiementService.save(dmd);
									Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error Switch communication s_conn false switch ip:["
													+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
													+ "]");
									demandeDtoMsg
											.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "result";
									Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
									System.out.println("Fin processRequestMobile ()");
									return page;
								}

								if (s_conn) {
									Util.writeInFileTransaction(folder, file, "Switch Connected.");
									Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

									resp_tlv = switchTCPClient.sendMessage(tlv);

									Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
									switchTCPClient.shutdown();
								}

							} catch (UnknownHostException e) {
								dmd.setDem_cvv("");
								demandePaiementService.save(dmd);
								Util.writeInFileTransaction(folder, file,
										"Switch  malfunction UnknownHostException !!!" + e);
								switch_ko = 1;
								demandeDtoMsg
										.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							} catch (java.net.ConnectException e) {
								dmd.setDem_cvv("");
								demandePaiementService.save(dmd);
								Util.writeInFileTransaction(folder, file,
										"Switch  malfunction ConnectException !!!" + e);
								switch_ko = 1;
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							catch (SocketTimeoutException e) {
								dmd.setDem_cvv("");
								demandePaiementService.save(dmd);
								dmd.setEtat_demande("SW_KO");
								Util.writeInFileTransaction(folder, file,
										"Switch  malfunction  SocketTimeoutException !!!" + e);
								switch_ko = 1;
								e.printStackTrace();
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error Switch communication SocketTimeoutException"
												+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
												+ resp_tlv + "]");
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							catch (IOException e) {
								dmd.setDem_cvv("");
								dmd.setEtat_demande("SW_KO");
								demandePaiementService.save(dmd);
								Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
								switch_ko = 1;
								e.printStackTrace();
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error Switch communication IOException" + "switch ip:["
												+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
												+ "]");
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							catch (Exception e) {
								dmd.setDem_cvv("");
								dmd.setEtat_demande("SW_KO");
								demandePaiementService.save(dmd);
								Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
								switch_ko = 1;
								e.printStackTrace();
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							String resp = resp_tlv;

							// resp debug
							// resp =
							// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

							if (switch_ko == 0 && resp == null) {
								dmd.setDem_cvv("");
								dmd.setEtat_demande("SW_KO");
								demandePaiementService.save(dmd);
								Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
								switch_ko = 1;
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error Switch null response" + "switch ip:[" + sw_s
												+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							if (switch_ko == 0 && resp.length() < 3) {
								dmd.setDem_cvv("");
								dmd.setEtat_demande("SW_KO");
								demandePaiementService.save(dmd);
								switch_ko = 1;

								Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error Switch short response length() < 3 " + "switch ip:["
												+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
												+ "]");
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

							Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

							TLVParser tlvp = null;

							String tag0_resp = null, tag1_resp = null, tag3_resp = null, tag8_resp = null,
									tag9_resp = null, tag14_resp = null, tag15_resp = null, tag16_resp = null,
									tag17_resp = null, tag66_resp = null, tag18_resp = null, tag19_resp = null,
									tag23_resp = null, tag20_resp = null, tag21_resp = null, tag22_resp = null,
									tag80_resp = null, tag98_resp = null;

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
									dmd.setDem_cvv("");
									dmd.setEtat_demande("SW_KO");
									demandePaiementService.save(dmd);
									Util.writeInFileTransaction(folder, file,
											"Switch  malfunction tlv parsing !!!" + e);
									switch_ko = 1;
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during tlv Switch response parse" + "switch ip:["
													+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
													+ "]");
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "result";
									Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
									System.out.println("Fin processRequestMobile ()");
									return page;
								}

								// controle switch
								if (tag1_resp == null) {
									Util.writeInFileTransaction(folder, file,
											"Switch  malfunction !!! tag1_resp == null");
									switch_ko = 1;
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
													+ "switch ip:[" + sw_s + "] and switch port:[" + port
													+ "] resp_tlv : [" + resp_tlv + "]");
								}

								if (tag1_resp != null && tag1_resp.length() < 3) {
									Util.writeInFileTransaction(folder, file,
											"Switch  malfunction !!! tag1_resp == null");
									switch_ko = 1;
									Util.writeInFileTransaction(folder, file,
											"authorization 500"
													+ "Error during tlv Switch response parse tag1_resp length tag  < 3"
													+ "switch ip:[" + sw_s + "] and switch port:[" + port
													+ "] resp_tlv : [" + resp_tlv + "]");
								}

								if (tag20_resp == null) {
									Util.writeInFileTransaction(folder, file,
											"Switch  malfunction !!! tag20_resp == null");
									switch_ko = 1;
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
													+ "switch ip:[" + sw_s + "] and switch port:[" + port
													+ "] resp_tlv : [" + resp_tlv + "]");
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
							
							try {
								// calcule du montant avec les frais
								amount = calculMontantTotalOperation(dmd);
							} catch (Exception ex){
								Util.writeInFileTransaction(folder, file,"calcule du montant avec les frais : " + ex);
							}

							if (switch_ko == 1) {
								pan_auto = Util.formatagePan(cardnumber);
								Util.writeInFileTransaction(folder, file,
										"getSWHistoAuto pan_auto/rrn/amount/date/merchantid : " + pan_auto + "/" + rrn
												+ "/" + amount + "/" + date + "/" + merchantid);
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
									CodeReponseDto codeReponseDto = codeReponseService
											.findByRpcCode(tag20_resp_verified);
									System.out.println("codeReponseDto : " + codeReponseDto);
									Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
									if (codeReponseDto != null) {
										s_status = codeReponseDto.getRpcLibelle();
									}
								} catch (Exception ee) {
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error codeReponseDto null");
									ee.printStackTrace();
								}
								
								websiteid = dmd.getGalid();								

								Util.writeInFileTransaction(folder, file,
										"get status Switch status : [" + s_status + "]");

								Util.writeInFileTransaction(folder, file, "formatting pan...");

								pan_auto = Util.formatagePan(cardnumber);
								Util.writeInFileTransaction(folder, file,
										"formatting pan Ok pan_auto :[" + pan_auto + "]");

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
								if(websiteid.equals("")) {
									websiteid = "0066";
								}
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

								if (recurring.equalsIgnoreCase("Y"))
									hist.setIs_cof("Y");
								if (recurring.equalsIgnoreCase("N"))
									hist.setIs_cof("N");

								Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

								Util.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

								hist = histoAutoGateService.save(hist);
								
								Util.writeInFileTransaction(folder, file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

							} catch (Exception e) {
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error during  insert in histoautogate for given orderid:["
												+ orderid + "]" + e);
								try {
									Util.writeInFileTransaction(folder, file,
											"2eme tentative : HistoAutoGate Saving ... ");
									hist = histoAutoGateService.save(hist);
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
									Util.writeInFileTransaction(folder, file, "update etat demande : SW_PAYE ...");

									dmd.setEtat_demande("SW_PAYE");
									dmd.setDem_cvv("");
									demandePaiementService.save(dmd);
									Util.writeInFileTransaction(folder, file, "update etat demande : SW_PAYE OK");
								} catch (Exception e) {
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
													+ orderid + "]" + e);
								}								

							} else {

								Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
								Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

								try {
									Util.writeInFileTransaction(folder, file,
											"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

									dmd.setEtat_demande("SW_REJET");
									dmd.setDem_cvv("");
									demandePaiementService.save(dmd);
								} catch (Exception e) {
									dmd.setDem_cvv("");
									demandePaiementService.save(dmd);
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
													+ orderid + "]" + e);
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer.");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "result";
									Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
									System.out.println("Fin processRequestMobile ()");
									return page;
								}
								Util.writeInFileTransaction(folder, file,
										"update Demandepaiement status to SW_REJET OK.");
								// 2024-02-27
								try {
									if(hist.getId() == null) {
										// get histoauto check if exist
										HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
										if(histToAnnulle !=null) {
											Util.writeInFileTransaction(folder, file,
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
									Util.writeInFileTransaction(folder, file,
											"authorization 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
													+ "] and merchantid:[" + merchantid + "]" + err2);
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
										"authorization 500 Error during  paymentid generation for given orderid:["
												+ orderid + "]" + e);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (Erreur lors de la génération de l'ID de paiement), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							Util.writeInFileTransaction(folder, file, "Generating paymentid OK");
							Util.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

							// JSONObject jso = new JSONObject();

							Util.writeInFileTransaction(folder, file, "Preparing autorization api response");

							String authnumber = "";
							String coderep = "";
							String motif = "";
							String merchnatidauth = "";
							String dtdem = "";
							String frais;
							String montantSansFrais = "";

							try {
								authnumber = hist.getHatNautemt();
								coderep = hist.getHatCoderep();
								motif = hist.getHatMtfref1();
								merchnatidauth = hist.getHatNumcmr();
								dtdem = dmd.getDem_pan();
								transactionid = String.valueOf(hist.getHatNumdem());
								montantSansFrais = String.valueOf(dmd.getMontant());
								frais = String.valueOf(dmd.getFrais());
								Util.writeInFileTransaction(folder, file, "frais :[" + frais + "]");
							} catch (Exception e) {
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error during authdata preparation orderid:[" + orderid + "]"
												+ e);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							try {
								/*
								 * String data_noncrypt = "orderid=" + orderid + "&fname=" + fname + "&lname=" +
								 * lname + "&email=" + email + "&amount=" + amount + "&coderep=" + coderep +
								 * "&authnumber=" + authnumber + "&cardnumber=" + Util.formatCard(cardnumber) +
								 * "&transactionid=" + transactionid + "&paymentid=" + paymentid;
								 */
								String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
										+ email + "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto="
										+ coderep + "&numAuto=" + authnumber + "&numCarte="
										+ Util.formatCard(cardnumber) + "&typecarte=" + dmd.getType_carte()
										+ "&numTrans=" + transactionid;

								Util.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
								System.out.println("data_noncrypt : " + data_noncrypt);

								String plainTxtSignature = orderid + current_infoCommercant.getClePub();

								Util.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
								System.out.println("plainTxtSignature : " + plainTxtSignature);

								String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt,
										current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

								Util.writeInFileTransaction(folder, file, "data encrypt : " + data);
								System.out.println("data encrypt : " + data);

								if (coderep.equals("00")) {
									Util.writeInFileTransaction(folder, file,
											"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
									System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
									if (dmd.getSuccessURL() != null) {
										response.sendRedirect(
												dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
									} else {
										responseDto responseDto = new responseDto();
										responseDto.setLname(dmd.getNom());
										responseDto.setFname(dmd.getPrenom());
										responseDto.setOrderid(dmd.getCommande());
										responseDto.setAuthnumber(authnumber);
										responseDto.setAmount(dmd.getMontant());
										responseDto.setTransactionid(transactionid);
										responseDto.setMerchantid(dmd.getComid());
										responseDto.setEmail(dmd.getEmail());
										responseDto.setMerchantname(current_infoCommercant.getCmrNom());
										responseDto.setCardnumber(Util.formatCard(cardnumber));
										responseDto.setTransactiontime(dateFormat.format(new Date()));

										model.addAttribute("responseDto", responseDto);

										page = "index";
										Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
										System.out.println("Fin processRequestMobile ()");
										return page;
									}
								} else {
									Util.writeInFileTransaction(folder, file,
											"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
									System.out.println(
											"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
									String libelle = "";
									try {
										CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(coderep);
										System.out.println("codeReponseDto : " + codeReponseDto);
										Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
										if (codeReponseDto != null) {
											libelle = codeReponseDto.getRpcLibelle();
										}
									} catch (Exception ee) {
										Util.writeInFileTransaction(folder, file,
												"payer 500 Error codeReponseDto null");
										ee.printStackTrace();
									}
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti (Coderep "
													+ coderep + ":" + libelle + "),"
													+ " votre compte ne sera pas débité, merci de réessayer.");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "result";
									Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
									System.out.println("Fin processRequestMobile ()");
									return page;
								}

							} catch (Exception jsouterr) {
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error during jso out processing given authnumber:["
												+ authnumber + "]" + jsouterr);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}

							// fin
							// *******************************************************************************************************************
						} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
							// ********************* Cas chalenge responseMPI equal C ou D
							// *********************
							Util.writeInFileTransaction(folder, file,
									"****** Cas chalenge responseMPI equal C ou D ******");
							try {

								// Transaction info
								// jso.put("statuscode", coderep);
								// jso.put("status", motif);
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
								Util.writeInFileTransaction(folder, file,
										"link_chalenge " + link_chalenge + dmd.getTokencommande());

								System.out.println("autorization api response chalenge :  [" + jso.toString() + "]");
								Util.writeInFileTransaction(folder, file,
										"autorization api response chalenge :  [" + jso.toString() + "]");

								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");

								return jso.toString();
							} catch (Exception ex) {
								Util.writeInFileTransaction(folder, file,
										"authorization 500 Error during jso out processing " + ex);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}
						} else if (reponseMPI.equals("E")) {
							// ********************* Cas responseMPI equal E
							// *********************
							Util.writeInFileTransaction(folder, file, "****** Cas responseMPI equal E ******");
							Util.writeInFileTransaction(folder, file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
							switch (errmpi) {
							case "COMMERCANT NON PARAMETRE":
								Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
								dmd.setDem_xid(threeDSServerTransID);
								dmd.setEtat_demande("MPI_CMR_INEX");
								dmd.setDem_cvv("");
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "BIN NON PARAMETRE":
								Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
								dmd.setEtat_demande("MPI_BIN_NON_PAR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "DIRECTORY SERVER":
								Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
								dmd.setEtat_demande("MPI_DS_ERR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "CARTE ERRONEE":
								Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
								dmd.setEtat_demande("MPI_CART_ERROR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "CARTE NON ENROLEE":
								Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
								dmd.setEtat_demande("MPI_CART_NON_ENR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "ERROR REPONSE ACS":
								Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
								dmd.setEtat_demande("MPI_ERR_RS_ACS");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "Error 3DSS":
								Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
								dmd.setEtat_demande("MPI_ERR_3DSS");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}
						} else {
							switch (errmpi) {
							case "COMMERCANT NON PARAMETRE":
								Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
								dmd.setDem_xid(threeDSServerTransID);
								dmd.setEtat_demande("MPI_CMR_INEX");
								dmd.setDem_cvv("");
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "BIN NON PARAMETRE":
								Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
								dmd.setEtat_demande("MPI_BIN_NON_PAR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "DIRECTORY SERVER":
								Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
								dmd.setEtat_demande("MPI_DS_ERR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "CARTE ERRONEE":
								Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
								dmd.setEtat_demande("MPI_CART_ERROR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "CARTE NON ENROLEE":
								Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
								dmd.setEtat_demande("MPI_CART_NON_ENR");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "ERROR REPONSE ACS":
								Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
								dmd.setEtat_demande("MPI_ERR_RS_ACS");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							case "Error 3DSS":
								Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
								dmd.setEtat_demande("MPI_ERR_3DSS");
								dmd.setDem_cvv("");
								dmd.setDem_xid(threeDSServerTransID);
								demandePaiementService.save(dmd);
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
								return page;
							}
						}
					} else {
						dmd.setDem_cvv("");
						demandePaiementService.save(dmd);
						Util.writeInFileTransaction(folder, file,
								"if(eci!=05) || eci!=02|| eci!=06 || eci!=01) : arret du processus ");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Authentification failed), votre compte ne sera pas débité, merci de réessayer.");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
						return page;
					}
				} else {
					Util.writeInFileTransaction(folder, file, "threeDsecureResponse null");
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Authentification failed), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				}
			} else {
				Util.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
				System.out.println(
						"ACSController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
				DemandePaiementDto demandeP = new DemandePaiementDto();
				Util.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());
				System.out.println(
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());

				demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());

				if (demandeP != null) {

					demandeP.setEtat_demande("RETOUR_ACS_NON_AUTH");
					demandePaiementService.save(demandeP);

					msgRefus = "";

					Util.writeInFileTransaction(folder, file,
							"TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());
					System.out.println(
							"TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());
					
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
							+ "), votre compte ne sera pas débité, merci de réessayer.";
					
					demandeDtoMsg.setMsgRefus(msgRefus);
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				} else {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
							+ "), votre compte ne sera pas débité, merci de réessayer.";
					demandeDtoMsg.setMsgRefus(msgRefus);
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				}
			}
		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> Exception " + ex);
			System.out.println("ACSController RETOUR ACS =====> Exception " + ex);
			msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
					+ "), votre compte ne sera pas débité, merci de réessayer.";
			demandeDtoMsg.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
			System.out.println("Fin processRequestMobile ()");
			return page;
		}
		Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
		System.out.println("Fin processRequestMobile ()");

		return page;
	}

	@PostMapping(value = "/napspayment/linkCCB", consumes = "application/json", produces = "application/json")
	@ResponseBody
	public String getLinkCCB(@RequestHeader MultiValueMap<String, String> header, @RequestBody String linkP,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "MB_LINK_CCB_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start getLinkCCB() ************** ");
		System.out.println("*********** Start getLinkCCB() ************** ");

		String api_key = "";
		String api_product = "";
		String api_version = "";
		String api_user_agent = "";

		Util.writeInFileTransaction(folder, file, "getLinkCCB api call start ...");
		Util.writeInFileTransaction(folder, file, "getLinkCCB : [" + linkP + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(linkP);
		}

		catch (JSONException jserr) {
			Util.writeInFileTransaction(folder, file, "getLinkCCB 500 malformed json expression " + linkP + jserr);
			return getMsgError(folder, file, null, "getLinkCCB 500 malformed json expression", null);
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
						"getLinkCCB 500 malformed header" + header.toString() + head_err);
				return getMsgError(folder, file, null, "getLinkCCB 500 malformed header", null);
			} else {
				Util.writeInFileTransaction(folder, file, "getLinkCCB 500 malformed header" + head_err);
				return getMsgError(folder, file, null, "getLinkCCB 500 malformed header " + head_err.getMessage(),
						null);
			}
		}

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;

		String orderid, amount, merchantid, merchantname, websiteName, websiteid, recurring, country, phone, city,
				state, zipcode, address, expirydate, transactiondate, transactiontime, callbackUrl, fname, lname,
				email = "", securtoken24, mac_value, successURL, failURL, idDemande, id_client, token, cartenaps,
				dateexpnaps;
		try {
			// Transaction info
			orderid = (String) jsonOrequest.get("orderid");
			amount = (String) jsonOrequest.get("amount");
			recurring = (String) jsonOrequest.get("recurring");
			id_client = (String) jsonOrequest.get("id_client");
			token = (String) jsonOrequest.get("token");
			cartenaps = (String) jsonOrequest.get("cartenaps");
			dateexpnaps = (String) jsonOrequest.get("dateexpnaps");
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
			Util.writeInFileTransaction(folder, file, "getLinkCCB 500 malformed json expression " + linkP + jerr);
			return getMsgError(folder, file, null, "getLinkCCB 500 malformed json expression " + jerr.getMessage(),
					null);
		}
		
		String url = "", status = "", statuscode = "";
		JSONObject jso = new JSONObject();
		
		try {
			Double montant = 0.00;
			if (amount.equals("") || amount == null) {
				amount = "0";
			}
			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			montant = Double.valueOf(amount);
			if(montant<5) {
				url = "";
				statuscode = "17";
				status = "The amount must be greater than or equal to 5dh";
				
				jso.put("statuscode", statuscode);
				jso.put("status", status);
				jso.put("orderid", orderid);
				jso.put("amount", amount);
				jso.put("url", url);
				return jso.toString();
			}
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "The amount must be greater than or equal to 5dh" + e);
			return getMsgError(folder, file, null, "The amount must be greater than or equal to 5dh" + e.getMessage(),
					null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(folder, file, jsonOrequest,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error during PaiementRequest", null);
		}
		if (check_dmd != null && check_dmd.getEtat_demande().equals("SW_PAYE")) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Error Already exist in PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error Already exist in PaiementRequest",
					"16");
		}

		try {
			String tokencommande = "";
			if (check_dmd != null) {
				// generer token
				tokencommande = Util.genTokenCom(check_dmd.getCommande(), check_dmd.getComid());
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
				dmd.setCartenaps(cartenaps);
				dmd.setDateexpnaps(dateexpnaps);
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
				// calcule des frais de recharge
				String frais = fraisCCB;
				if(frais.equals("") || frais == null) {
					frais = "0.00";
				}
				Util.writeInFileTransaction(folder, file, "fraisCCB : " + frais +"%");
				Double fraisD = Double.valueOf(frais);
				
				Double montantrecharge = ((Double.parseDouble(amount) * fraisD) / 100);
				//Double montantrecharge = (0 + (Double.parseDouble(amount) * 0.65) / 100);
				String fraistr = String.format("%.2f", montantrecharge).replaceAll(",", ".");
				Util.writeInFileTransaction(folder, file, "FraisMontantRecharge : " + fraistr);
				
				dmd.setFrais(Double.parseDouble(fraistr));
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

				url = link_ccb + dmdSaved.getTokencommande();
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
					"getLinkCCB 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]"
							+ err1);

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error during DEMANDE_PAIEMENT insertion",
					null);
		}

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
					"getLinkCCB 500 Error during jso out processing given orderid:[" + orderid + "]" + err8);

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error during jso out processing", null);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin getLinkCCB() ************** ");
		System.out.println("*********** Fin getLinkCCB() ************** ");

		return jso.toString();

	}

	@RequestMapping(value = "/napspayment/authorization/ccb/token/{token}", method = RequestMethod.GET)
	public String showPageRchg(@PathVariable(value = "token") String token, Model model, HttpSession session) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "MB_PAGE_CCB_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start affichage page ccb ***********");
		System.out.println("*********** Start affichage page ccb ***********");

		Util.writeInFileTransaction(folder, file, "findByTokencommande token : " + token);
		System.out.println("findByTokencommande token : " + token);

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		CommercantDto merchant = null;
		GalerieDto galerie = null;
		String merchantid = "";
		String orderid = "";

		String page = "erecharge";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				System.out.println("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/"
						+ demandeDto.getCommande());
				Util.writeInFileTransaction(folder, file, "DemandePaiement is found iddemande/Commande : "
						+ demandeDto.getIddemande() + "/" + demandeDto.getCommande());

				// get list of years + 10
				int currentYear = Year.now().getValue();
				List<Integer> years = generateYearList(currentYear, currentYear + 10);

				demandeDto.setYears(years);

				// get list of months
				List<Month> months = Arrays.asList(Month.values());
				List<String> monthNames = convertMonthListToStringList(months);
				List<MonthDto> monthValues = convertStringAGListToFR(monthNames);

				demandeDto.setMonths(monthValues);
				// if cmr don't accept transaction cof demandeDto.getIs_cof() = N don't show
				// carte
				if (demandeDto.getIs_cof() == null || demandeDto.getIs_cof().equals("N")) {
					demandeDto.setDem_pan("");
					demandeDto.setDem_cvv("");
				}
				// if cmr accept transaction cof demandeDto.getIs_cof() = Y show your carte
				// saved
				// get cardnumber by idclient
				String idclient = demandeDto.getId_client();
				if (idclient == null) {
					idclient = "";
				}
				merchantid = demandeDto.getComid();
				// merchantid = "";
				String cardnumber = "";
				List<Cartes> cartes = new ArrayList<>();
				if (!idclient.equals("") && idclient != null && !idclient.equals("null")) {
					System.out.println("idclient/merchantid : " + idclient + "/" + merchantid);
					try {
						List<CardtokenDto> cards = new ArrayList<>();
						cards = cardtokenService.findByIdMerchantAndIdMerchantClient(merchantid, idclient);
						if (cards != null && cards.size() > 0) {
							for (CardtokenDto card : cards) {
								if (card.getCardNumber() != null) {
									Cartes carte = new Cartes();
									cardnumber = card.getCardNumber();
									carte.setCarte(cardnumber);
									carte.setPcidsscarte(Util.formatCard(cardnumber));
									String dateExStr = dateFormatSimple.format(card.getExprDate());
									formatDateExp(dateExStr, carte);
									cartes.add(carte);
								}
							}
							System.out.println("Cartes : " + cartes.toString());
							demandeDto.setCartes(cartes);
						} else {
							demandeDto.setCartes(null);
						}
					} catch (Exception ex) {
						Util.writeInFileTransaction(folder, file, "showPageRchg 500 idclient not found" + ex);
					}
				} else {
					demandeDto.setCartes(null);
				}

				// Créez un objet DecimalFormat avec le modèle "0.00"
				DecimalFormat df = new DecimalFormat("0.00");

				// Formatez le nombre en une chaîne avec deux chiffres après la virgule
				Double mont = demandeDto.getMontant();
				String mtFormate = df.format(mont);
				if (mtFormate.contains(",")) {
					mtFormate = mtFormate.replace(",", ".");
				}

				demandeDto.setMontantStr(mtFormate);

				model.addAttribute("demandeDto", demandeDto);

				if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
					Util.writeInFileTransaction(folder, file, "Opération déjà effectuée");
					demandeDto.setMsgRefus(
							"La transaction en cours est déjà effectuée, votre compte ne sera pas débité.");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtat_demande().equals("SW_REJET")) {
					Util.writeInFileTransaction(folder, file, "Transaction rejetée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDto);
					page = "result";
				} else {
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						merchant = commercantService.findByCmrNumcmr(merchantid);
						if (merchant != null) {
							demandeDto.setCommercantDto(merchant);
						}
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"showPageRchg 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
					try {
						merchantid = demandeDto.getComid();
						orderid = demandeDto.getCommande();
						galerie = galerieService.findByCodeCmr(merchantid);
						if (galerie != null) {
							demandeDto.setGalerieDto(galerie);
						}
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"showPageRchg 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + e);
						demandeDto = new DemandePaiementDto();
						demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
				}
			} else {
				Util.writeInFileTransaction(folder, file, "demandeDto not found token : " + token);
				System.out.println("demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPageRchg 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token + "]" + e);

			Util.writeInFileTransaction(folder, file, "showPageRchg 500 exception" + e);
			e.printStackTrace();
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}
		
		// gestion expiration de la session on stoque la date en millisecond
	    session.setAttribute("paymentStartTime", System.currentTimeMillis());
	    Util.writeInFileTransaction(folder, file, "paymentStartTime : " + System.currentTimeMillis());
	    demandeDto.setTimeoutURL(String.valueOf(System.currentTimeMillis()));
	    
		if (page.equals("erecharge")) {
			if(demandeDto.getEtat_demande().equals("INIT")) {
				demandeDto.setEtat_demande("P_CHRG_OK");
				demandePaiementService.save(demandeDto);
				System.out.println("update Demandepaiement status to P_CHRG_OK");
				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to P_CHRG_OK");
			}
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin affichage page ccb ************** ");
		System.out.println("*********** Fin affichage page ccb ************** ");

		return page;
	}

	@PostMapping("/recharger")
	public String recharger(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto,
			HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "MB_RECHARGER_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start recharger () ************** ");
		System.out.println("*********** Start recharger () ************** ");

		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, montantRechgtrame, cartenaps,
				dateExnaps, num_trs = "", successURL, failURL, transactiontype, idclient;

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		Objects.copyProperties(demandeDto, dto);
		System.out.println("Commande : " + demandeDto.getCommande());
		Util.writeInFileTransaction(folder, file, "CdemandeDto commande : " + dto.getCommande());
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		DemandePaiementDto dmd = new DemandePaiementDto();

		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;
		boolean flagNvCarte, flagSaveCarte;

		String page = "chalenge";
		try {
			// Transaction info
			orderid = demandeDto.getCommande();
			if (demandeDto.getMontant() == null) {
				demandeDto.setMontant(0.00);
			}
			amount = String.valueOf(demandeDto.getMontant());
			capture = "";
			currency = "504";
			recurring = "N";
			promoCode = "";
			transactionid = "";
			transactiontype = "0"; // 0 payment , P preauto

			// Merchnat info
			merchantid = demandeDto.getComid();
			merchantname = "";
			websiteName = "";
			websiteid = "";
			cardnumber = "";
			expirydate = "";
			callbackUrl = demandeDto.getCallbackURL();
			successURL = demandeDto.getSuccessURL();
			failURL = demandeDto.getFailURL();

			// Card info
			cvv = demandeDto.getDem_cvv();
			// if transaction not cof
			if (demandeDto.getDem_pan() != null && !demandeDto.getDem_pan().equals("")) {
				cardnumber = demandeDto.getDem_pan();
				expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois().substring(0, 2));
			}
			// if transaction cof
			if (demandeDto.getInfoCarte() != null && !demandeDto.isFlagNvCarte()
					&& (demandeDto.getDem_pan() == null || demandeDto.getDem_pan().equals(""))) {
				String infoCard = demandeDto.getInfoCarte().substring(8, demandeDto.getInfoCarte().length());
				Cartes carteFormated = fromString(infoCard);
				demandeDto.setCarte(carteFormated);
				cardnumber = demandeDto.getCarte().getCarte();
				String annee = String.valueOf(demandeDto.getCarte().getYear());
				expirydate = annee.substring(2, 4).concat(demandeDto.getCarte().getMoisValue());
			}
			if (demandeDto.getDem_pan().equals("") && demandeDto.getInfoCarte() != null) {
				if (!demandeDto.getAnnee().equals("") && !demandeDto.getMois().equals("")) {
					expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois().substring(0, 2));
				}
			}
			flagNvCarte = demandeDto.isFlagNvCarte();
			flagSaveCarte = demandeDto.isFlagSaveCarte();
			if (cardnumber.contains(",")) {
				cardnumber = cardnumber.replace(",", "");
			}
			// cardnumber = demandeDto.getDem_pan();
			token = "";
			// expirydate = demandeDto.getAnnee().substring(2,
			// 4).concat(demandeDto.getMois());
			holdername = "";
			cvv = demandeDto.getDem_cvv();

			// Client info
			fname = demandeDto.getPrenom();
			lname = demandeDto.getNom();
			email = demandeDto.getEmail();
			country = demandeDto.getCountry();
			phone = demandeDto.getTel();
			city = demandeDto.getCity();
			state = demandeDto.getState();
			zipcode = demandeDto.getPostcode();
			address = demandeDto.getAddress();

		} catch (Exception jerr) {
			Util.writeInFileTransaction(folder, file, "recharger 500 malformed json expression" + jerr);
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		InfoCommercantDto current_infoCommercant = null;

		try {
			current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 InfoCommercant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);

			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_infoCommercant == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 InfoCommercantDto misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");

			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			Util.writeInFileTransaction(folder, file, "recharger 500 Card number length is incorrect orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Le numéro de la carte est incomplet, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (i_card_valid == 2) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Card number  is not valid incorrect luhn check orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Le numéro de la carte est invalide, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		int i_card_type = Util.getCardIss(cardnumber);

		try {
			DemandePaiementDto dmdToEdit = demandePaiementService.findByIdDemande(demandeDto.getIddemande());

			dmdToEdit.setDem_pan(cardnumber);
			dmdToEdit.setDem_cvv(cvv);
			dmdToEdit.setType_carte(i_card_type + "");
			// dmdToEdit.setDateexpnaps(expirydate);
			dmdToEdit.setTransactiontype(transactiontype);
			int nbr_tv = dmdToEdit.getNbreTenta() + 1;
			dmdToEdit.setNbreTenta(nbr_tv);

			formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			dmdToEdit.setDem_date_time(dateFormat.format(new Date()));

			demandeDto = demandePaiementService.save(dmdToEdit);
			demandeDto.setExpery(expirydate);
			demandeDto.setFlagNvCarte(flagNvCarte);
			demandeDto.setFlagSaveCarte(flagSaveCarte);
			idclient = demandeDto.getId_client();
			if (idclient == null) {
				idclient = "";
			}
		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + err1);
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		// 2024-06-03
		// gestion expiration de la session on recupere la date en millisecond
		Long paymentStartTime = (Long) session.getAttribute("paymentStartTime");
		Util.writeInFileTransaction(folder, file, "paymentStartTime : " + paymentStartTime);

	    if (paymentStartTime != null) {
	        long currentTime = System.currentTimeMillis();
	        long elapsedTime = currentTime - paymentStartTime;
	        Util.writeInFileTransaction(folder, file, "currentTime : " + currentTime);
	        Util.writeInFileTransaction(folder, file, "elapsedTime : " + elapsedTime);
	        // Check if more than 5 minutes (300000 milliseconds) have passed
	        int timeoutF = timeout;
	        if (elapsedTime > timeoutF) {
				Util.writeInFileTransaction(folder, file, "Page expirée Time > 5min");
				demandeDtoMsg.setMsgRefus("Votre session de paiement a expiré. Veuillez réessayer.");				
				session.setAttribute("idDemande", demandeDto.getIddemande());				
				model.addAttribute("demandeDto", demandeDtoMsg);
				demandeDto.setEtat_demande("TimeOut");
				demandeDto.setDem_cvv("");
				demandeDto = demandePaiementService.save(demandeDto);	            
				page = "timeout";
				
				Util.writeInFileTransaction(folder, file, "*********** Fin recharger () ************** ");
				System.out.println("*********** Fin recharger () ************** ");
				
				return page;
	        }
	    }
	 // 2024-06-03

		if (demandeDto.getEtat_demande().equals("SW_PAYE") || demandeDto.getEtat_demande().equals("PAYE")) {
			demandeDto.setDem_cvv("");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file, "Opération déjà effectuée");
			demandeDto.setMsgRefus(
					"La transaction en cours est déjà effectuée, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDto);
			page = "operationEffectue";
			return page;
		}

		// for test control risk
		// refactoring code 2024-03-20
		Util.writeInFileTransaction(folder, file, "Debut controlleRisk");
		try {
			String msg = autorisationService.controlleRisk(demandeDto, folder, file);
			if (!msg.equalsIgnoreCase("OK")) {
				demandeDto.setDem_cvv("");
				demandeDto.setEtat_demande("REJET_RISK_CTRL");
				demandePaiementService.save(demandeDto);
				Util.writeInFileTransaction(folder, file, msg);
				demandeDto = new DemandePaiementDto();
				demandeDtoMsg.setMsgRefus(msg);
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}
		} catch (Exception e) {
			demandeDto.setDem_cvv("");
			demandeDto.setEtat_demande("REJET_RISK_CTRL");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"recharger 500 ControlRiskCmr misconfigured in DB or not existing merchantid:["
							+ demandeDto.getComid() + e);
			demandeDto = new DemandePaiementDto();
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		Util.writeInFileTransaction(folder, file, "Fin controlleRisk");
		
		// saving card if flagSaveCarte true
		if (demandeDto.isFlagSaveCarte()) {
			try {
				List<CardtokenDto> checkCardNumber = cardtokenService.findByIdMerchantClientAndCardNumber(idclient,
						cardnumber);

				CardtokenDto cardtokenDto = new CardtokenDto();
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
				Date dateExp;
				dateExp = dateFormatSimple.parse(expirydateFormated);

				if (checkCardNumber.size() == 0) {
					// insert new cardToken
					String tokencard = Util.generateCardToken(idclient);

					// test if token not exist in DB
					CardtokenDto checkCardToken = cardtokenService.findByIdMerchantAndToken(idclient, tokencard);

					while (checkCardToken != null) {
						tokencard = Util.generateCardToken(idclient);
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
					cardtokenDto.setIdMerchantClient(idclient);
					cardtokenDto.setFirst_name(fname);
					cardtokenDto.setLast_name(lname);
					cardtokenDto.setHolderName(holdername);
					cardtokenDto.setMcc(merchantid);

					CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

					Util.writeInFileTransaction(folder, file, "Saving CARDTOKEN OK");
				} else {
					Util.writeInFileTransaction(folder, file, "Carte deja enregistrée");
					for (CardtokenDto crd : checkCardNumber) {
						if (crd.getExprDate() != null) {
							if (crd.getCardNumber().equals(cardnumber)) {
								if (crd.getExprDate().before(dateToken)) {
									Util.writeInFileTransaction(folder, file, "Encienne date expiration est expirée : "
											+ dateFormatSimple.format(crd.getExprDate()));
									Util.writeInFileTransaction(folder, file,
											"Update par nv date expiration saisie : " + expirydateFormated);
									crd.setExprDate(dateExp);
									CardtokenDto cardSaved = cardtokenService.save(crd);
									System.out.println("Update CARDTOKEN OK");
									Util.writeInFileTransaction(folder, file, "Update CARDTOKEN OK");
								}
							}
						}
					}
				}

			} catch (ParseException e) {
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "savingcardtoken 500 Error during CARDTOKEN Saving " + e);
			}
		}

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			demandeDto.setDem_cvv("");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file, "recharger 500 Error during  date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + err2);
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		// appel 3DSSecure ***********************************************************

		/**
		 * dans la preprod les tests sans 3DSS on commente l'appel 3DSS et on mj
		 * reponseMPI="Y"
		 */
		Util.writeInFileTransaction(folder, file, "environement : " + environement);
		if (environement.equals("PREPROD")) {
			// threeDsecureResponse = autorisationService.preparerReqMobileThree3DSS(demandeDto,
			// folder, file);

			threeDsecureResponse.setReponseMPI("Y");
		} else {
			threeDsecureResponse = autorisationService.preparerReqMobileThree3DSS(demandeDto, folder, file);
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
		String idDemande = String.valueOf(demandeDto.getIddemande() == null ? "" : demandeDto.getIddemande());
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
			demandeDto.setDem_cvv("");
			demandeDto.setEtat_demande("MPI_KO");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO idDemande null : " + demandeDto.toString());
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

		if (dmd == null) {
			demandeDto.setDem_cvv("");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
							+ idDemande);
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("") || reponseMPI == null) {
			dmd.setDem_cvv("");
			dmd.setEtat_demande("MPI_KO");
			demandePaiementService.save(dmd);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
			Util.writeInFileTransaction(folder, file, "Response 3DS is null");
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (reponseMPI.equals("Y")) {
			// ********************* Frictionless responseMPI equal Y *********************
			Util.writeInFileTransaction(folder, file,
					"********************* Cas frictionless responseMPI equal Y *********************");
			if (!threeDSServerTransID.equals("")) {
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
			}
			cartenaps = dmd.getCartenaps();
			dateExnaps = dmd.getDateexpnaps();

			// 2024-03-05
			montanttrame = formatMontantTrame(folder, file, amount, orderid, merchantid, dmd, page, model);
		
			// 2024-03-05
			montantRechgtrame = formatMontantRechargeTrame(folder, file, amount, orderid, merchantid, dmd, page, model);

			merchantname = current_merchant.getCmrNom();
			websiteName = "";
			websiteid = dmd.getGalid();
			String url = "", status = "", statuscode = "";

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

			String first_auth = "";
			long lrec_serie = 0;

			// controls
			Util.writeInFileTransaction(folder, file, "Switch processing start ...");

			String tlv = "";
			Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

			if (!cvv_present && !is_reccuring) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file,
						"recharger 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				demandeDtoMsg.setMsgRefus(
						"Le champ CVV est vide. Veuillez saisir le code de sécurité à trois chiffres situé au dos de votre carte pour continuer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// not reccuring , normal
			if (cvv_present && !is_reccuring) {
				Util.writeInFileTransaction(folder, file, "not reccuring , normal cvv_present && !is_reccuring");
				try {
					// tag 046 tlv info carte naps
					String tlvCCB = new TLVEncoder().withField(Tags.tag1, cartenaps)
							.withField(Tags.tag14, montantRechgtrame).withField(Tags.tag42, dateExnaps).encode();
					// tlv total ccb
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
							.withField(Tags.tag168, xid).withField(Tags.tag46, tlvCCB).encode();

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
					Util.writeInFileTransaction(folder, file, "tag46_request : [" + tlvCCB + "]");

				} catch (Exception err4) {
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err4);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la création du switch tlv), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
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
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error Switch communication s_conn false switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
					demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}

				if (s_conn) {
					Util.writeInFileTransaction(folder, file, "Switch Connected.");
					Util.writeInFileTransaction(folder, file, "Switch Sending TLV Request ...");

					resp_tlv = switchTCPClient.sendMessage(tlv);

					Util.writeInFileTransaction(folder, file, "Switch TLV Request end.");
					switchTCPClient.shutdown();
				}

			} catch (UnknownHostException e) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;

			} catch (java.net.ConnectException e) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Un dysfonctionnement du switch), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (SocketTimeoutException e) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error Switch communication SocketTimeoutException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur de communication du switch SocketTimeoutException), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (IOException e) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur de communication du switch IOException), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (Exception e) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch Exception), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch null response" + "switch ip:["
						+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp null), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			if (switch_ko == 0 && resp.length() < 3) {
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				switch_ko = 1;

				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp < 3 !!!), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
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
					Util.writeInFileTransaction(folder, file, "recharger 500 Error during tlv Switch response parse"
							+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// controle switch
				if (tag1_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:["
									+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				if (tag1_resp != null && tag1_resp.length() < 3) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"recharger 500" + "Error during tlv Switch response parse tag1_resp length tag  < 3"
									+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
									+ "]");
				}

				if (tag20_resp == null) {
					Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
					switch_ko = 1;
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:["
									+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
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

			try {
				// calcule du montant avec les frais
				amount = calculMontantTotalOperation(dmd);
			} catch (Exception ex){
				Util.writeInFileTransaction(folder, file,"calcule du montant avec les frais : " + ex);
			}

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
					Util.writeInFileTransaction(folder, file, "recharger 500 Error codeReponseDto null");
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
				if(websiteid.equals("")) {
					websiteid = "0066";
				}
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

				if (recurring.equalsIgnoreCase("Y"))
					hist.setIs_cof("Y");
				if (recurring.equalsIgnoreCase("N"))
					hist.setIs_cof("N");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling end ...");

				Util.writeInFileTransaction(folder, file, "HistoAutoGate Saving ...");

				hist = histoAutoGateService.save(hist);
				
				Util.writeInFileTransaction(folder, file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + e);
				try {
					Util.writeInFileTransaction(folder, file, "2eme tentative : HistoAutoGate Saving ... ");
					hist = histoAutoGateService.save(hist);
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							"2eme tentative : recharger 500 Error during  insert in histoautogate for given orderid:["
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
					Util.writeInFileTransaction(folder, file, "update etat demande : SW_PAYE ...");

					dmd.setEtat_demande("SW_PAYE");
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file, "update etat demande : SW_PAYE OK");
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
									+ orderid + "]" + e);
				}				

			} else {

				Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {
					Util.writeInFileTransaction(folder, file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtat_demande("SW_REJET");
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
				} catch (Exception e) {
					dmd.setDem_cvv("");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during  DemandePaiement update SW_REJET for given orderid:[" + orderid
									+ "]" + e);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}
				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to SW_REJET OK.");
				// 2024-02-27
				try {
					if(hist.getId() == null) {
						// get histoauto check if exist
						HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
						if(histToAnnulle != null) {
							Util.writeInFileTransaction(folder, file,
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
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err2);
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
						"recharger 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + e);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			Util.writeInFileTransaction(folder, file, "Generating paymentid OK");
			Util.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

			// JSONObject jso = new JSONObject();

			Util.writeInFileTransaction(folder, file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "", frais = "", montantSansFrais = "";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDem_pan();
				transactionid = String.valueOf(hist.getHatNumdem());
				montantSansFrais = String.valueOf(dmd.getMontant());
				frais = String.valueOf(dmd.getFrais());
				Util.writeInFileTransaction(folder, file, "frais :[" + frais + "]");
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// reccurent transaction processing

			// reccurent insert and update

			try {

				/*
				 * String data_noncrypt = "orderid=" + orderid + "&fname=" + fname + "&lname=" +
				 * lname + "&email=" + email + "&amount=" + amount + "&coderep=" + coderep +
				 * "&authnumber=" + authnumber + "&cardnumber=" + Util.formatCard(cardnumber) +
				 * "&transactionid=" + transactionid + "&paymentid=" + paymentid;
				 */

				String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
						+ "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto=" + coderep + "&numAuto="
						+ authnumber + "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte=" + dmd.getType_carte()
						+ "&numTrans=" + transactionid;

				Util.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
				System.out.println("data_noncrypt : " + data_noncrypt);

				String plainTxtSignature = orderid + current_infoCommercant.getClePub();

				Util.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
				System.out.println("plainTxtSignature : " + plainTxtSignature);

				String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt, current_infoCommercant.getClePub(),
						plainTxtSignature, folder, file);

				Util.writeInFileTransaction(folder, file, "data encrypt : " + data);
				System.out.println("data encrypt : " + data);

				if (coderep.equals("00")) {
					Util.writeInFileTransaction(folder, file,
							"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
					if (dmd.getSuccessURL() != null) {
						response.sendRedirect(dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
					} else {
						responseDto responseDto = new responseDto();
						responseDto.setLname(dmd.getNom());
						responseDto.setFname(dmd.getPrenom());
						responseDto.setOrderid(dmd.getCommande());
						responseDto.setAuthnumber(authnumber);
						responseDto.setAmount(dmd.getMontant());
						responseDto.setTransactionid(transactionid);
						responseDto.setMerchantid(dmd.getComid());
						responseDto.setEmail(dmd.getEmail());
						responseDto.setMerchantname(current_infoCommercant.getCmrNom());
						responseDto.setCardnumber(Util.formatCard(cardnumber));
						responseDto.setTransactiontime(dateFormat.format(new Date()));

						model.addAttribute("responseDto", responseDto);

						page = "index";
						Util.writeInFileTransaction(folder, file, "Fin recharger ()");
						System.out.println("Fin recharger ()");
						return page;
					}
				} else {
					Util.writeInFileTransaction(folder, file,
							"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
					System.out.println("coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
					String libelle = "";
					try {
						CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(coderep);
						System.out.println("codeReponseDto : " + codeReponseDto);
						Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
						if (codeReponseDto != null) {
							libelle = codeReponseDto.getRpcLibelle();
						}
					} catch (Exception ee) {
						Util.writeInFileTransaction(folder, file, "recharger 500 Error codeReponseDto null");
						ee.printStackTrace();
					}
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Coderep " + coderep
									+ ":" + libelle + ")," + " votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
				}
			} catch (Exception jsouterr) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during jso out processing given authnumber:[" + authnumber + "]"
								+ jsouterr);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			// fin
			// *******************************************************************************************************************
		} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
			// ********************* Cas chalenge responseMPI equal C ou D
			// *********************
			Util.writeInFileTransaction(folder, file, "****** Cas chalenge responseMPI equal C ou D ******");
			try {
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				if(threeDSServerTransID.equals("") || threeDSServerTransID == null) {
					threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();
				}
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("SND_TO_ACS");
				demandeDto = demandePaiementService.save(dmd);
				Util.writeInFileTransaction(folder, file, "threeDSServerTransID : " + demandeDto.getDem_xid());
				model.addAttribute("demandeDto", demandeDto);
				// 2024-06-27 old
				/*page = "chalenge";

				Util.writeInFileTransaction(folder, file, "set demandeDto model creq : " + demandeDto.getCreq());
				Util.writeInFileTransaction(folder, file, "return page : " + page);*/

				// 2024-06-27
				// autre façon de faire la soumission automatique de formulaires ACS via le HttpServletResponse.
	
		        String creq = "";
		        String acsUrl = "";
		        String response3DS = threeDsecureResponse.getHtmlCreq();
		        Pattern pattern = Pattern.compile("action='(.*?)'.*value='(.*?)'");
		        Matcher matcher = pattern.matcher(response3DS);

		        // Si une correspondance est trouvée
	            if (matcher.find()) {
	                acsUrl = matcher.group(1);
	                creq = matcher.group(2);
	                System.out.println("L'URL ACS est : " + acsUrl);
	                System.out.println("La valeur de creq est : " + creq);
	                Util.writeInFileTransaction(folder, file, "L'URL ACS est : " + acsUrl);
	                Util.writeInFileTransaction(folder, file, "La valeur de creq est : " + creq);

	                String decodedCreq = new String(Base64.decodeBase64(creq.getBytes()));
	                System.out.println("La valeur de decodedCreq est : " + decodedCreq);
	                Util.writeInFileTransaction(folder, file, "La valeur de decodedCreq est : " + decodedCreq);
	                
	                // URL de feedback après soumission ACS
	                String feedbackUrl = request.getContextPath() + "/acsFeedback";

	                // Afficher le formulaire HTML dans la réponse
	                response.setContentType("text/html");
	                response.setCharacterEncoding("UTF-8");
	                response.getWriter().println("<html><body>");
	                response.getWriter().println("<form id=\"acsForm\" action=\"" + acsUrl + "\" method=\"post\">");
	                response.getWriter().println("<input type=\"hidden\" name=\"creq\" value=\"" + creq + "\">");
	                response.getWriter().println("</form>");
	                response.getWriter().println("<script>document.getElementById('acsForm').submit();</script>");
	                
	                /* a revoir apres pour la confirmation de l'affichage acs
	                response.getWriter().println("document.getElementById('acsForm').submit();");
	                response.getWriter().println("fetch('" + feedbackUrl + "', { method: 'POST' });");  // Envoi du feedback
	                response.getWriter().println("</script>");
	                */
	                response.getWriter().println("</body></html>");
	                
	                System.out.println("Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");
	                Util.writeInFileTransaction(folder, file, "Le Creq a été envoyé à l'ACS par soumission automatique du formulaire.");
	                
	                return null;  // Terminer le traitement ici après avoir envoyé le formulaire
	            } else {
	                System.out.println("Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
	                Util.writeInFileTransaction(folder, file, "Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML.");
	                page = "error";  // Définir la page d'erreur appropriée
	            }
				
			// 2024-06-27
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file, "Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML " + ex);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Aucune correspondance pour l'URL ACS et creq trouvée dans la réponse HTML), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				dmd.setDem_cvv("");
				demandePaiementService.save(dmd);
				page = "result";
				return page;
			}
		} else if (reponseMPI.equals("E")) {
			// ********************* Cas responseMPI equal E
			// *********************
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setDem_cvv("");
				dmd.setEtat_demande("MPI_CMR_INEX");
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "BIN NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "DIRECTORY SERVER":
				Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "CARTE ERRONEE":
				Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "CARTE NON ENROLEE":
				Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "ERROR REPONSE ACS":
				Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_RS_ACS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "Error 3DSS":
				Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_3DSS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			}
		} else {
			switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setDem_cvv("");
				dmd.setEtat_demande("MPI_CMR_INEX");
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "BIN NON PARAMETRE":
				Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtat_demande("MPI_BIN_NON_PAR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "DIRECTORY SERVER":
				Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtat_demande("MPI_DS_ERR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "CARTE ERRONEE":
				Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_ERROR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "CARTE NON ENROLEE":
				Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtat_demande("MPI_CART_NON_ENR");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			case "ERROR REPONSE ACS":
				Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_RS_ACS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			case "Error 3DSS":
				Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
				dmd.setEtat_demande("MPI_ERR_3DSS");
				dmd.setDem_cvv("");
				dmd.setDem_xid(threeDSServerTransID);
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
				return page;
			}
		}

		System.out.println("demandeDto htmlCreq : " + demandeDto.getCreq());
		System.out.println("return page : " + page);

		Util.writeInFileTransaction(folder, file, "*********** Fin recharger () ************** ");
		System.out.println("*********** Fin recharger () ************** ");

		return page;
	}

	public String calculMontantTotalOperation(DemandePaiementDto dto) {
		if(dto.getMontant() == null) {
			dto.setMontant(0.00);
		}
		if(dto.getFrais() == null) {
			dto.setFrais(0.00);
		}
		double mnttotalopp = dto.getMontant() + dto.getFrais();
		String mntttopp = String.format("%.2f", mnttotalopp).replaceAll(",", ".");
		return mntttopp;
	}

	public String calculMontantSansOperation(DemandePaiementDto dto) {
		if(dto.getMontant() == null) {
			dto.setMontant(0.00);
		}
		double mnttotalopp = dto.getMontant();
		String mntttopp = String.format("%.2f", mnttotalopp).replaceAll(",", ".");
		return mntttopp;
	}

	// Static factory method to create a Cartes object from a string
	public Cartes fromString(String input) {
		Cartes cartes = new Cartes();

		// Remove square brackets and split the input string
		String[] keyValuePairs = input.substring(0, input.length() - 1).split(", ");

		for (String pair : keyValuePairs) {
			String[] keyValue = pair.split("=");

			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();

				switch (key) {
				case "carte":
					cartes.setCarte(value);
					break;
				case "pcidsscarte":
					cartes.setPcidsscarte(value);
					break;
				case "year":
					cartes.setYear(Integer.parseInt(value));
					break;
				case "mois":
					cartes.setMois(value);
					break;
				case "moisValue":
					cartes.setMoisValue(value);
					break;
				// Handle other properties as needed
				}
			}
		}

		return cartes;
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
			//jso.put("orderid", "");
			//jso.put("merchantid", "");
			//jso.put("amount", "");
		}
		if (coderep != null) {
			jso.put("statuscode", coderep);
		} else {
			jso.put("statuscode", "17");
		}

		jso.put("status", msg);
		jso.put("etataut", "N");
		//jso.put("linkacs", "");
		//jso.put("url", "");
		//jso.put("idDemande", "");

		Util.writeInFileTransaction(folder, file, "json : " + jso.toString());
		System.out.println("json : " + jso.toString());

		Util.writeInFileTransaction(folder, file, "*********** Fin getMsgError() ************** ");
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
	
	private String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid, 
			DemandePaiementDto dmd, String page, Model model) {
		String montanttrame;
		String[] mm;
		String[] m;
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		try {
			montanttrame = "";
			mm = new String[2];
			amount = calculMontantTotalOperation(dmd);

			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			//System.out.println("montant recharge avec frais : [" + amount + "]");
			Util.writeInFileTransaction(folder, file,
					"montant recharge avec frais : [" + amount + "]");

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
			//System.out.println("montanttrame avec frais : [" + montanttrame + "]");
			Util.writeInFileTransaction(folder, file,
					"montanttrame avec frais : [" + montanttrame + "]");
		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Error during  amount formatting for given orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + err3);
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
			System.out.println("Fin processRequestMobile ()");
			return page;
		}
		return montanttrame;
	}
	
	private String formatMontantRechargeTrame(String folder, String file, String amount, String orderid, String merchantid, 
			DemandePaiementDto dmd, String page, Model model) {
		String montantRechgtrame;
		String[] mm;
		String[] m;
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		try {
			montantRechgtrame = "";

			mm = new String[2];
			String amount1 = calculMontantSansOperation(dmd);

			if (amount1.contains(",")) {
				amount1 = amount1.replace(",", ".");
			}
			if (!amount1.contains(".") && !amount1.contains(",")) {
				amount1 = amount1 + "." + "00";
			}
			//System.out.println("montant recharge sans frais : [" + amount1 + "]");
			Util.writeInFileTransaction(folder, file,
					"montant recharge sans frais : [" + amount1 + "]");

			String montantt = amount1 + "";

			mm = montantt.split("\\.");
			if (mm[1].length() == 1) {
				montantRechgtrame = amount1 + "0";
			} else {
				montantRechgtrame = amount1 + "";
			}

			m = new String[2];
			m = montantRechgtrame.split("\\.");
			if (m[1].equals("0")) {
				montantRechgtrame = montantRechgtrame.replace(".", "0");
			} else
				montantRechgtrame = montantRechgtrame.replace(".", "");
			montantRechgtrame = Util.formatageCHamps(montantRechgtrame, 12);
			//System.out.println("montantRechgtrame sans frais: [" + montantRechgtrame + "]");
			Util.writeInFileTransaction(folder, file,
					"montantRechgtrame sans frais : [" + montantRechgtrame + "]");
		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Error during  amount formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err3);
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		return montantRechgtrame;
	}

	private List<Integer> generateYearList(int startYear, int endYear) {
		List<Integer> years = new ArrayList<>();
		for (int year = startYear; year <= endYear; year++) {
			years.add(year);
		}
		return years;
	}

	private List<String> convertMonthListToStringList(List<Month> monthList) {
		List<String> monthNames = new ArrayList<>();
		for (Month month : monthList) {
			monthNames.add(month.toString()); // Convert Month to its string representation
		}
		return monthNames;
	}

	private List<MonthDto> convertStringAGListToFR(List<String> monthList) {
		List<String> monthNames = new ArrayList<>();
		List<MonthDto> monthNamesValues = new ArrayList<>();

		for (String month : monthList) {
			MonthDto exp = new MonthDto();
			if (month.equals("JANUARY")) {
				month = "Janvier";
				exp.setMonth(month);
				exp.setValue("01");
			} else if (month.toString().equals("FEBRUARY")) {
				month = "Février";
				exp.setMonth(month);
				exp.setValue("02");
			} else if (month.toString().equals("MARCH")) {
				month = "Mars";
				exp.setMonth(month);
				exp.setValue("03");
			} else if (month.toString().equals("APRIL")) {
				month = "Avril";
				exp.setMonth(month);
				exp.setValue("04");
			} else if (month.toString().equals("MAY")) {
				month = "Mai";
				exp.setMonth(month);
				exp.setValue("05");
			} else if (month.toString().equals("JUNE")) {
				month = "Juin";
				exp.setMonth(month);
				exp.setValue("06");
			} else if (month.toString().equals("JULY")) {
				month = "Juillet";
				exp.setMonth(month);
				exp.setValue("07");
			} else if (month.toString().equals("AUGUST")) {
				month = "Aout";
				exp.setMonth(month);
				exp.setValue("08");
			} else if (month.toString().equals("SEPTEMBER")) {
				month = "Septembre";
				exp.setMonth(month);
				exp.setValue("09");
			} else if (month.toString().equals("OCTOBER")) {
				month = "Octobre";
				exp.setMonth(month);
				exp.setValue("10");
			} else if (month.toString().equals("NOVEMBER")) {
				month = "Novembre";
				exp.setMonth(month);
				exp.setValue("11");
			} else if (month.toString().equals("DECEMBER")) {
				month = "Décembre";
				exp.setMonth(month);
				exp.setValue("12");
			}
			// Convert Month to its string representation
			monthNames.add(month.toString());
			monthNamesValues.add(exp);

		}
		return monthNamesValues;
	}

	public void formatDateExp(String expirationDate, Cartes carte) {
		try {
			LocalDate localDate = LocalDate.parse(expirationDate);
			Month mois = localDate.getMonth();
			Integer year = localDate.getYear();
			carte.setYear(year);
			// String formattedMonth = mapToFrenchMonth(month);
			String moisStr = String.format("%s", mois);
			List<String> list = new ArrayList<>();
			list.add(moisStr);
			MonthDto month = mapToFrenchMonth(moisStr);
			carte.setMois(month.getMonth());
			carte.setMoisValue(month.getValue());

			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();
			// get year from date
			// format date to "yyyy-MM-dd"
			String expirydateFormated = carte.getYear() + "-" + carte.getMoisValue() + "-" + "01";
			// exp
			// String expirydateFormated = "2020" + "-" + "05" + "-" + "01";
			System.out.println("cardtokenDto expirydate : " + expirydateFormated);
			Util.writeInFileTransaction(folder, file, "cardtokenDto expirydate formated : " + expirydateFormated);
			Date dateExp = dateFormatSimple.parse(expirydateFormated);
			if (dateExp.before(dateToken)) {
				System.out.println("date exiration est inferieur à la date systeme : " + dateExp + " < " + dateToken);
				Util.writeInFileTransaction(folder, file,
						"date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
				carte.setMoisValue("xxxx");
				carte.setMois("xxxx");
				carte.setYear(1111);
			}
			if (dateExp.after(dateToken)) {
				//System.out.println("date exiration est superieur à la date systeme : " + dateExp + " < " + dateToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MonthDto mapToFrenchMonth(String month) {

		MonthDto exp = new MonthDto();
		if (month.equals("JANUARY")) {
			month = "Janvier";
			exp.setMonth(month);
			exp.setValue("01");
		} else if (month.toString().equals("FEBRUARY")) {
			month = "Février";
			exp.setMonth(month);
			exp.setValue("02");
		} else if (month.toString().equals("MARCH")) {
			month = "Mars";
			exp.setMonth(month);
			exp.setValue("03");
		} else if (month.toString().equals("APRIL")) {
			month = "Avril";
			exp.setMonth(month);
			exp.setValue("04");
		} else if (month.toString().equals("MAY")) {
			month = "Mai";
			exp.setMonth(month);
			exp.setValue("05");
		} else if (month.toString().equals("JUNE")) {
			month = "Juin";
			exp.setMonth(month);
			exp.setValue("06");
		} else if (month.toString().equals("JULY")) {
			month = "Juillet";
			exp.setMonth(month);
			exp.setValue("07");
		} else if (month.toString().equals("AUGUST")) {
			month = "Aout";
			exp.setMonth(month);
			exp.setValue("08");
		} else if (month.toString().equals("SEPTEMBER")) {
			month = "Septembre";
			exp.setMonth(month);
			exp.setValue("09");
		} else if (month.toString().equals("OCTOBER")) {
			month = "Octobre";
			exp.setMonth(month);
			exp.setValue("10");
		} else if (month.toString().equals("NOVEMBER")) {
			month = "Novembre";
			exp.setMonth(month);
			exp.setValue("11");
		} else if (month.toString().equals("DECEMBER")) {
			month = "Décembre";
			exp.setMonth(month);
			exp.setValue("12");
		}

		return exp;
	}

	private String mapToFrenchMonth(Month month) {
		// Simple mapping from English to French month names.
		switch (month) {
		case JANUARY:
			return "Janvier";
		case FEBRUARY:
			return "Février";
		case MARCH:
			return "Mars";
		case APRIL:
			return "Avril";
		case MAY:
			return "Mai";
		case JUNE:
			return "Juin";
		case JULY:
			return "Juillet";
		case AUGUST:
			return "Août";
		case SEPTEMBER:
			return "Septembre";
		case OCTOBER:
			return "Octobre";
		case NOVEMBER:
			return "Novembre";
		case DECEMBER:
			return "Décembre";
		default:
			return ""; // Handle unknown month
		}
	}
}
