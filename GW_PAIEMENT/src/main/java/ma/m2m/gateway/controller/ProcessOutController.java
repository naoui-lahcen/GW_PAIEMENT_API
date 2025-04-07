package ma.m2m.gateway.controller;

/**
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2024-11-25
 */

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.m2m.gateway.dto.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.GalerieService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.CRes;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
import ma.m2m.gateway.utils.Util;

@Controller
public class ProcessOutController {

	private static final Logger logger = LogManager.getLogger(ProcessOutController.class);

	private LocalDateTime dateF;
	private String folder;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;

	private Gson gson;

	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDSS;

	@Value("${key.LINK_FAIL}")
	private String linkFail;

	@Value("${key.LINK_CHALENGE}")
	private String linkChalenge;

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

	@Value("${key.TIMEOUT}")
	private int timeout;

	//@Autowired
	private final DemandePaiementService demandePaiementService;

	//@Autowired
	private final AutorisationService autorisationService;

	//@Autowired
	private final CommercantService commercantService;

	//@Autowired
	private final InfoCommercantService infoCommercantService;

	//@Autowired
	private final GalerieService galerieService;

	//@Autowired
	private final HistoAutoGateService histoAutoGateService;
	
	//@Autowired
	private final CodeReponseService codeReponseService;
	
	public static final String DF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	DateFormat dateFormat = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
	DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

	public ProcessOutController(DemandePaiementService demandePaiementService, AutorisationService autorisationService,
			HistoAutoGateService histoAutoGateService, CommercantService commercantService, 
			InfoCommercantService infoCommercantService, GalerieService galerieService,
			CodeReponseService codeReponseService) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		dateF = LocalDateTime.now(ZoneId.systemDefault());
		folder = dateF.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
		this.demandePaiementService = demandePaiementService;
		this.autorisationService = autorisationService;
		this.histoAutoGateService = histoAutoGateService;
		this.commercantService = commercantService;
		this.infoCommercantService = infoCommercantService;
		this.galerieService = galerieService;
		this.codeReponseService = codeReponseService;
	}
	
	@PostMapping("/napspayment/processout/acs")
	@SuppressWarnings("all")
	public String processoutRequest(HttpServletRequest request, HttpServletResponse response, Model model,
			HttpSession session) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "R__PROCESSOUT_ACS_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "Start processoutRequest ()");
		logger.info("Start processoutRequest ()");
		CRes cleanCres = new CRes();
		String msgRefus = "";
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		String page = "index";

		try {
			String encodedCres = request.getParameter("cres");
			logger.info("ProcessOutController RETOUR ACS =====> encodedCres : " + encodedCres);
			autorisationService.logMessage(file,
					"ProcessOutController RETOUR ACS =====> encodedCres : " + encodedCres);

			String decodedCres = "";

			decodedCres = new String(Base64.decodeBase64(encodedCres.getBytes()));
			if (decodedCres.indexOf("}") != -1) {
				decodedCres = decodedCres.substring(0, decodedCres.indexOf("}") + 1);
			}
			autorisationService.logMessage(file,
					"ProcessOutController RETOUR ACS =====> decodedCres : " + decodedCres);
			logger.info("ProcessOutController RETOUR ACS =====> decodedCres : " + decodedCres);

			cleanCres = gson.fromJson(decodedCres, CRes.class);
			autorisationService.logMessage(file,
					"ProcessOutController RETOUR ACS =====> cleanCres : " + cleanCres);

			autorisationService.logMessage(file, "transStatus/threeDSServerTransID : " + cleanCres.getTransStatus()
					+ "/" + cleanCres.getThreeDSServerTransID());

			// TODO: just for test
			// TODO: cleanCres.setTransStatus("N");

			if (cleanCres.getTransStatus().equals("Y") || cleanCres.getTransStatus().equals("N")) {
				logger.info("ProcessOutController RETOUR ACS =====> getRreqFromThree3DSSAfterACS ");
				autorisationService.logMessage(file,
						"ProcessOutController RETOUR ACS =====> getRreqFromThree3DSSAfterACS ");

				ThreeDSecureResponse threeDsecureResponse = autorisationService.getRreqFromThree3DSSAfterACS(decodedCres,
						folder, file);

				DemandePaiementDto dmd = new DemandePaiementDto();
				JSONObject jso = new JSONObject();
				String[] mm;
				String[] m;
				SimpleDateFormat formatheure, formatdate = null;

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
				String expiry = ""; // TODO: YYMM
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
				String num_trs = "";
				String successURL = "";
				String failURL;

				if (threeDsecureResponse != null && threeDsecureResponse.getEci() != null) {
					if (threeDsecureResponse.getEci().equals("05") || threeDsecureResponse.getEci().equals("02")
							|| threeDsecureResponse.getEci().equals("06")
							|| threeDsecureResponse.getEci().equals("01")) {

						autorisationService.logMessage(file,
								"if(eci=05) || eci=02 || eci=06 || eci=01) : continue le processus");

						idDemande = threeDsecureResponse.getIdDemande();

						reponseMPI = threeDsecureResponse.getReponseMPI();

						threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();

						eci = threeDsecureResponse.getEci() == null ? "" : threeDsecureResponse.getEci();

						cavv = threeDsecureResponse.getCavv() == null ? "" : threeDsecureResponse.getCavv();

						errmpi = threeDsecureResponse.getErrmpi() == null ? "" : threeDsecureResponse.getErrmpi();

						expiry = threeDsecureResponse.getExpiry() == null ? "" : threeDsecureResponse.getExpiry();

						if (idDemande == null || idDemande.equals("")) {
							autorisationService.logMessage(file, "received idDemande from MPI is Null or Empty");
							autorisationService.logMessage(file,
									"demandePaiement after update MPI_KO idDemande null");
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							autorisationService.logMessage(file, "Fin processoutRequest ()");
							logger.info("Fin processoutRequest ()");
							return page;
						}

						dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

						if (dmd == null) {
							autorisationService.logMessage(file,
									"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
											+ idDemande);
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							autorisationService.logMessage(file, "Fin processoutRequest ()");
							logger.info("Fin processoutRequest ()");
							return page;
						}

						page = autorisationService.handleSessionTimeout(session, file, timeout, dmd, demandeDtoMsg, model);

						if ("timeout".equals(page)) {
							return page;
						}

						// TODO: Merchnat info
						merchantid = dmd.getComid();
						websiteid = dmd.getGalid();

						String timeStamp = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS).format(new Date());

						autorisationService.logMessage(file, "authorization_" + orderid + timeStamp);

						CommercantDto current_merchant = null;
						try {
							current_merchant = commercantService.findByCmrNumcmr(merchantid);
						} catch (Exception e) {
							return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
						}

						if (current_merchant == null) {
							return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
						}

						if (current_merchant.getCmrCodactivite() == null) {
							return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
						}

						if (current_merchant.getCmrCodbqe() == null) {
							return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, null, demandeDtoMsg, model, page, true);
						}
						InfoCommercantDto current_infoCommercant = null;

						try {
							current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
						} catch (Exception e) {
							return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, websiteid, demandeDtoMsg, model, page, false);
						}

						if (current_infoCommercant == null) {
							return autorisationService.handleMerchantAndInfoCommercantError(file, orderid, merchantid, websiteid, demandeDtoMsg, model, page, false);
						}

						// TODO: Info
						merc_codeactivite = current_merchant.getCmrCodactivite();
						acqcode = current_merchant.getCmrCodbqe();

						orderid = dmd.getCommande();
						recurring = "";
						amount = String.valueOf(dmd.getMontant());
						promoCode = "";
						transactionid = "";

						// TODO: Merchnat info
						merchantid = dmd.getComid();
						merchantname = current_merchant.getCmrNom();
						merchant_name = Util.pad_merchant(merchantname, 19, ' ');
						websiteName = "";
						callbackUrl = dmd.getCallbackURL();
						successURL = dmd.getSuccessURL();
						failURL = dmd.getFailURL();

						// TODO: Card info
						cardnumber = dmd.getDemPan();
						token = dmd.getToken();
						expirydate = expiry;
						holdername = "";
						cvv = dmd.getDemCvv();

						// TODO: Client info
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
							dmd.setDemCvv("");
							demandePaiementService.save(dmd);
							autorisationService.logMessage(file,
									"authorization 500 Error during  date formatting for given orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
							autorisationService.logMessage(file, "Fin processoutRequest ()");
							logger.info("Fin processoutRequest ()");
							response.sendRedirect(failURL);
							return null;
						}

						if (reponseMPI == null || reponseMPI.equals("")) {
							dmd.setDemCvv("");
							dmd.setEtatDemande("MPI_KO");
							demandePaiementService.save(dmd);
							autorisationService.logMessage(file,
									"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
							autorisationService.logMessage(file, "Response 3DS is null");
							autorisationService.logMessage(file, "Fin processoutRequest ()");
							logger.info("Fin processoutRequest ()");
							response.sendRedirect(failURL);
							return null;
						}

						if (reponseMPI.equals("Y")) {
							// TODO: ********************* Frictionless responseMPI equal Y *********************
							autorisationService.logMessage(file,
									"********************* responseMPI equal Y *********************");

							if (threeDSServerTransID != null && !threeDSServerTransID.equals("")) {
								dmd.setDemxid(threeDSServerTransID);
								// TODO: stackage de eci dans le chmp date_sendMPI vu que ce chmp nest pas utilisé
								dmd.setDateSendMPI(eci);
								// TODO: stackage de cavv dans le chmp date_SendSWT vu que ce chmp nest pas utilisé
								dmd.setDateSendSWT(cavv);
								dmd = demandePaiementService.save(dmd);
							}

							// TODO: 2024-03-05
							montanttrame = Util.formatMontantTrame(folder, file, amount, orderid, merchantid, dmd, model);

							boolean cvv_present = checkCvvPresence(cvv);
							boolean is_reccuring = isReccuringCheck(recurring);
							boolean is_first_trs = true;

							String first_auth = "";

							merchant_city = "MOROCCO        ";
							autorisationService.logMessage(file, "merchant_city : [" + merchant_city + "]");

							acq_type = "0000";
							processing_code = dmd.getTransactiontype();
							reason_code = "H";
							transaction_condition = "6";
							mesg_type = "0";

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

							// TODO: controls
							autorisationService.logMessage(file, "Switch processing start ...");

							String tlv = "";
							autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

							if (!cvv_present && !is_reccuring) {
								dmd.setDemCvv("");
								demandePaiementService.save(dmd);
								autorisationService.logMessage(file,
										"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");
								autorisationService.logMessage(file, "Fin processoutRequest ()");
								logger.info("Fin processoutRequest ()");
								response.sendRedirect(failURL);
								return null;
							}

							// TODO: not reccuring , normal
							if (cvv_present && !is_reccuring) {
								autorisationService.logMessage(file,
										"not reccuring , normal cvv_present && !is_reccuring");
								try {

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
											.withField(Tags.tag168, xid).encode();

								} catch (Exception err4) {
									dmd.setDemCvv("");
									demandePaiementService.save(dmd);
									autorisationService.logMessage(file,
											"authorization 500 Error during switch tlv buildup for given orderid:["
													+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
									autorisationService.logMessage(file, "Fin processoutRequest ()");
									logger.info("Fin processoutRequest ()");
									response.sendRedirect(failURL);
									return null;
								}

								autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");

							}

							// TODO: reccuring
							if (is_reccuring) {
								autorisationService.logMessage(file, "reccuring");
							}

							autorisationService.logMessage(file, "Preparing Switch TLV Request end.");

							String resp_tlv = "";
//							SwitchTCPClient sw = SwitchTCPClient.getInstance();
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
									autorisationService.logMessage(file, "Switch  malfunction cannot connect!!!");
									autorisationService.logMessage(file,
											"authorization 500 Error Switch communication s_conn false switch ip:["
													+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
													+ "]");
									autorisationService.logMessage(file, "Fin processoutRequest ()");
									logger.info("Fin processoutRequest ()");
									response.sendRedirect(failURL);
									return null;
								}

								if (s_conn) {
									autorisationService.logMessage(file, "Switch Connected.");

									resp_tlv = switchTCPClient.sendMessage(tlv);

									autorisationService.logMessage(file, "Switch TLV Request end.");
									switchTCPClient.shutdown();
								}

							} catch (Exception e) {
								switch_ko = 1;
								// return autorisationService.handleSwitchError(e, file, orderid, merchantid, resp_tlv, dmd, model, "result");
								dmd.setDemCvv("");
								dmd.setEtatDemande("SW_KO");
								demandePaiementService.save(dmd);
								response.sendRedirect(failURL);
								return null;
							}

							String resp = resp_tlv;

							// TODO: resp debug
							// TODO: resp =
							// TODO: "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

							if (switch_ko == 0 && resp == null) {
								dmd.setDemCvv("");
								dmd.setEtatDemande("SW_KO");
								demandePaiementService.save(dmd);
								autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
								switch_ko = 1;
								autorisationService.logMessage(file,
										"authorization 500 Error Switch null response" + "switch ip:[" + sw_s
												+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
								autorisationService.logMessage(file, "Fin processoutRequest ()");
								logger.info("Fin processoutRequest ()");
								response.sendRedirect(failURL);
								return null;
							}

							if (switch_ko == 0 && resp.length() < 3) {
								dmd.setDemCvv("");
								dmd.setEtatDemande("SW_KO");
								demandePaiementService.save(dmd);
								switch_ko = 1;

								autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
								autorisationService.logMessage(file,
										"authorization 500 Error Switch short response length() < 3 " + "switch ip:["
												+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
												+ "]");
								autorisationService.logMessage(file, "Fin processoutRequest ()");
								logger.info("Fin processoutRequest ()");
								response.sendRedirect(failURL);
								return null;
							}

							autorisationService.logMessage(file, "Switch TLV Respnose :[" + resp + "]");

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
									dmd.setDemCvv("");
									dmd.setEtatDemande("SW_KO");
									demandePaiementService.save(dmd);
									autorisationService.logMessage(file,
											"Switch  malfunction tlv parsing !!!" + Util.formatException(e));
									switch_ko = 1;
									autorisationService.logMessage(file,
											"authorization 500 Error during tlv Switch response parse" + "switch ip:["
													+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv
													+ "]");
									autorisationService.logMessage(file, "Fin processoutRequest ()");
									logger.info("Fin processoutRequest ()");
									response.sendRedirect(failURL);
									return null;
								}

								// TODO: controle switch
								if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
									autorisationService.logMessage(file,
											"Switch  malfunction !!! tag1_resp == null");
									switch_ko = 1;
									autorisationService.logMessage(file,
											"authorization 500"
													+ "Error during tlv Switch response parse tag1_resp length tag  < 3"
													+ "switch ip:[" + sw_s + "] and switch port:[" + port
													+ "] resp_tlv : [" + resp_tlv + "]");
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
								autorisationService.logMessage(file,
										"getSWHistoAuto pan_auto/rrn/amount/date/merchantid : " + pan_auto + "/" + rrn
												+ "/" + amount + "/" + date + "/" + merchantid);
							}
							HistoAutoGateDto hist = null;
							Integer Ihist_id = null;

							autorisationService.logMessage(file, "Insert into Histogate...");

							s_status = "";
							try {
								CodeReponseDto codeReponseDto = codeReponseService
										.findByRpcCode(tag20_resp_verified);
								autorisationService.logMessage(file, "codeReponseDto : " + codeReponseDto);
								if (codeReponseDto != null) {
									s_status = codeReponseDto.getRpcLibelle();
								}
							} catch (Exception ee) {
								autorisationService.logMessage(file,
										"authorization 500 Error codeReponseDto null" + Util.formatException(ee));
							}
							autorisationService.logMessage(file,"get status Switch status : [" + s_status + "]");

							try {

								hist = new HistoAutoGateDto();
								Date curren_date_hist = new Date();
								int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

								websiteid = dmd.getGalid();

								autorisationService.logMessage(file, "formatting pan...");

								pan_auto = Util.formatagePan(cardnumber);
								autorisationService.logMessage(file,
										"formatting pan Ok pan_auto :[" + pan_auto + "]");

								autorisationService.logMessage(file, "HistoAutoGate data filling start ...");

								autorisationService.logMessage(file, "websiteid : " + websiteid);

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
								hist.setHatNumCommande(orderid);
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

								autorisationService.logMessage(file,
										"hatNomdeandeur : " + hist.getHatNomdeandeur());

							} catch (Exception e) {
								autorisationService.logMessage(file,
										"authorization 500 Error during  insert in histoautogate for given orderid:["
												+ orderid + "]" + Util.formatException(e));
								try {
									autorisationService.logMessage(file,
											"2eme tentative : HistoAutoGate Saving ... ");
									hist = histoAutoGateService.save(hist);
								} catch (Exception ex) {
									autorisationService.logMessage(file,
											"2eme tentative : authorization 500 Error during  insert in histoautogate for given orderid:["
													+ orderid + "]" + Util.formatException(ex));
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
									autorisationService.logMessage(file,
											"authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
													+ orderid + "]" + Util.formatException(e));
								}

								// TODO: 2023-01-03 confirmation par Callback URL
								String resultcallback = "";
								String callbackURL = dmd.getCallbackURL();
								autorisationService.logMessage(file, "Call Back URL: " + callbackURL);
								if (dmd.getCallbackURL() != null && !dmd.getCallbackURL().equals("")
										&& !dmd.getCallbackURL().equals("NA")) {
									String clesigne = current_infoCommercant.getClePub();

									String montanttrx = String.format("%.2f", dmd.getMontant()).replace(",", ".");
									String token_gen = "";

									autorisationService.logMessage(file,
											"sendPOST(" + callbackURL + "," + clesigne + "," + dmd.getCommande() + ","
													+ tag20_resp + "," + montanttrx + "," + hist.getHatNautemt() + ","
													+ hist.getHatNumdem() + "," + dmd.getTypeCarte() + ")");

									resultcallback = sendPOST(callbackURL, clesigne, dmd.getCommande(), tag20_resp,
											montanttrx, hist.getHatNautemt(), hist.getHatNumdem(), token_gen,
											Util.formatCard(cardnumber), dmd.getTypeCarte(), folder, file);

									autorisationService.logMessage(file,
											"resultcallback :[" + resultcallback + "]");

									boolean repsucces = resultcallback.indexOf("GATESUCCESS") != -1 ? true : false;

									boolean repfailed = resultcallback.indexOf("GATEFAILED") != -1 ? true : false;

									autorisationService.logMessage(file, "repsucces : " + repsucces);
									autorisationService.logMessage(file, "repfailed : " + repfailed);
									if (repsucces) {
										autorisationService.logMessage(file,
												"Reponse recallURL OK => GATESUCCESS");
										dmd.setRecallRep("Y");
										demandePaiementService.save(dmd);
									} else {
										if (repfailed) {
											autorisationService.logMessage(file,
													"Reponse recallURL KO => GATEFAILED");
											dmd.setRecallRep("N");
											demandePaiementService.save(dmd);
										}
										// TODO: else {

										autorisationService.logMessage(file, "Annulation auto start ...");

										String repAnnu = annulationAuto(dmd, current_merchant, hist, model, folder,
												file);

										autorisationService.logMessage(file, "Annulation auto end");
										s_status = "";
										try {
											CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(repAnnu);
											autorisationService.logMessage(file,
													"codeReponseDto annulation : " + codeReponseDto);
											if (codeReponseDto != null) {
												s_status = codeReponseDto.getRpcLibelle();
											}
										} catch (Exception ee) {
											autorisationService.logMessage(file,
													"Annulation auto 500 Error codeReponseDto null" + Util.formatException(ee));
											// TODO: ee.printStackTrace();
										}
										autorisationService.logMessage(file,
												"Switch status annulation : [" + s_status + "]");
										if (repAnnu.equals("00")) {
											dmd.setEtatDemande("SW_ANNUL_AUTO");
											demandePaiementService.save(dmd);
											demandeDtoMsg.setMsgRefus(
													"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
											model.addAttribute("demandeDto", demandeDtoMsg);
											page = "operationAnnulee";
										} else {
											page = "error";
										}

										response.sendRedirect(dmd.getFailURL());

										autorisationService.logMessage(file, "Fin processoutRequest ()");
										logger.info("Fin processoutRequest ()");
										//return page;
										return null;

										// TODO: }
									}
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
								} catch (Exception e) {
									dmd.setDemCvv("");
									demandePaiementService.save(dmd);
									autorisationService.logMessage(file,
											"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
													+ orderid + "]" + Util.formatException(e));
									autorisationService.logMessage(file, "Fin processoutRequest ()");
									logger.info("Fin processoutRequest ()");
									response.sendRedirect(failURL);
									return null;
								}
								autorisationService.logMessage(file,
										"update Demandepaiement status to SW_REJET OK.");
								// TODO: 2024-02-27
								try {
									if (hist.getId() == null) {
										// TODO: get histoauto check if exist
										HistoAutoGateDto histToAnnulle = histoAutoGateService
												.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
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
											"processoutRequest 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:["
													+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
								}
								autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
								// TODO: 2024-02-27
							}

							// TODO: JSONObject jso = new JSONObject();

							autorisationService.logMessage(file, "Preparing autorization api response");

							String authnumber = "";
							String coderep = "";
							String motif = "";
							String merchnatidauth = "";
							String dtdem = "";
							String data = "";

							try {
								authnumber = hist.getHatNautemt();
								coderep = hist.getHatCoderep();
								motif = hist.getHatMtfref1();
								merchnatidauth = hist.getHatNumcmr();
								dtdem = dmd.getDemPan();
								transactionid = String.valueOf(hist.getHatNumdem());
							} catch (Exception e) {
								autorisationService.logMessage(file,
										"authorization 500 Error during authdata preparation orderid:[" + orderid + "]"
												+ Util.formatException(e));
							}

							try {
								String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
										+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
										+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
										+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid;

								autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
								logger.info("data_noncrypt : " + data_noncrypt);

								if (data_noncrypt.length() > 200) {
									// TODO : First, try reducing the length by adjusting the fname
									if (!fname.isEmpty()) {
										fname = fname.length() > 10 ? fname.substring(0, 10) : fname;
									}

									// TODO : Rebuild the data_noncrypt string with the updated fname
									data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
											+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
											+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
											+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid;

									autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
									// TODO : If the length is still greater than 200, reduce the length of email
									if (data_noncrypt.length() > 200 && !email.isEmpty()) {
										email = email.length() > 10 ? email.substring(0, 10) : email;
									}

									// TODO : Rebuild again with the updated email
									data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
											+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
											+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
											+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid;

									autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
								}

								String plainTxtSignature = orderid + current_infoCommercant.getClePub();

								autorisationService.logMessage(file, "plainTxtSignature : " + plainTxtSignature);
								logger.info("plainTxtSignature : " + plainTxtSignature);

								data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt,
										current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

								autorisationService.logMessage(file, "data encrypt : " + data);
								logger.info("data encrypt : " + data);

							} catch (Exception jsouterr) {
								autorisationService.logMessage(file,
										"authorization 500 Error during jso out processing given authnumber:["
												+ authnumber + "]" + jsouterr);
								autorisationService.logMessage(file,
										"Erreur lors du traitement de sortie, transaction abouti redirection to SuccessUrl");
							}

							if (coderep.equals("00")) {
								if (dmd.getSuccessURL() != null) {
									// TODO: envoie de la reponse normal
									autorisationService.logMessage(file,
											"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
									autorisationService.logMessage(file,"?data=" + data + "==&codecmr=" + merchantid);

									response.sendRedirect(dmd.getSuccessURL());
									autorisationService.logMessage(file, "Fin processoutRequest ()");
									return  null;
								} else {
									ResponseDto responseDto = new ResponseDto();
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
									autorisationService.logMessage(file, "Fin processoutRequest ()");
									logger.info("Fin processoutRequest ()");
									return page;
								}
							} else {
								autorisationService.logMessage(file,
										"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
								logger.info(
										"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
								response.sendRedirect(dmd.getFailURL());
								autorisationService.logMessage(file, "Fin processoutRequest ()");
								return  null;
							}

							// TODO: fin
							// TODO: *******************************************************************************************************************
						} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
							autorisationService.logMessage(file,
									"2eme chalenge apres auth acs => Redirect to failURL : " + dmd.getFailURL());
							response.sendRedirect(dmd.getFailURL());
							autorisationService.logMessage(file, "Fin processoutRequest ()");
							logger.info("Fin processoutRequest ()");

							return null;
						} else if (reponseMPI.equals("E")) {
							// TODO: ********************* Cas responseMPI equal E
							// TODO: *********************
							autorisationService.logMessage(file, "****** Cas responseMPI equal E ******");
							autorisationService.logMessage(file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
							page = autorisationService.handleMpiError(errmpi, file, idDemande, threeDSServerTransID, dmd, model, page);
							response.sendRedirect(dmd.getFailURL());
							return null;
						} else {
							page = autorisationService.handleMpiError(errmpi, file, idDemande, threeDSServerTransID, dmd, model, page);
							response.sendRedirect(dmd.getFailURL());
							page = "error";
						}

						if(page.equals("error")) {
							response.sendRedirect(dmd.getFailURL());
							return null;
						}

					} else {
						idDemande = threeDsecureResponse.getIdDemande() == null ? "" : threeDsecureResponse.getIdDemande();
						dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));
						dmd.setEtatDemande("AUTH_ACS_FAILED");
						dmd.setDemxid(threeDSServerTransID);
						// TODO: stackage de eci dans le chmp date_sendMPI vu que ce chmp nest pas utilisé
						dmd.setDateSendMPI(eci);
						// TODO: stackage de cavv dans le chmp date_SendSWT vu que ce chmp nest pas utilisé
						dmd.setDateSendSWT(cavv);
						dmd.setDemCvv("");
						demandePaiementService.save(dmd);
						autorisationService.logMessage(file,
								"if(eci!=05) || eci!=02|| eci!=06 || eci!=01) : arret du processus ");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Authentification failed), votre compte ne sera pas débité, merci de réessayer.");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						autorisationService.logMessage(file, "Fin processoutRequest ()");
						logger.info("Fin processoutRequest ()");
						//return page;
						response.sendRedirect(dmd.getFailURL());
						return null;
					}
				} else {
					autorisationService.logMessage(file, "threeDsecureResponse null");
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Authentification failed), votre compte ne sera pas débité, merci de réessayer.");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					autorisationService.logMessage(file, "Fin processoutRequest ()");
					logger.info("Fin processoutRequest ()");
					return page;
				}

			} else {
				autorisationService.logMessage(file,
						"ProcessOutController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
				logger.info(
						"ProcessOutController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
				DemandePaiementDto demandeP = new DemandePaiementDto();
				autorisationService.logMessage(file, "ProcessOutController RETOUR ACS =====> findByDem_xid : "
						+ cleanCres.getThreeDSServerTransID());
				logger.info("ProcessOutController RETOUR ACS =====> findByDem_xid : "
						+ cleanCres.getThreeDSServerTransID());

				demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());

				if (demandeP != null) {

					demandeP.setEtatDemande("RETOUR_ACS_NON_AUTH");
					demandePaiementService.save(demandeP);

					msgRefus = "";

					autorisationService.logMessage(file,
							"TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());
					logger.info(
							"TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());
					autorisationService.logMessage(file, "Fin processoutRequest ()");
					logger.info("Fin processoutRequest ()");
					//return page;
					response.sendRedirect(demandeP.getFailURL());
					return null;
				} else {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
							+ "), votre compte ne sera pas débité, merci de réessayer.";
					demandeDtoMsg.setMsgRefus(msgRefus);
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					autorisationService.logMessage(file, "Fin processoutRequest ()");
					logger.info("Fin processoutRequest ()");
					return page;
				}
			}
		} catch (Exception ex) {
			autorisationService.logMessage(file, "ProcessOutController RETOUR ACS =====> Exception " + Util.formatException(ex));
			logger.info("ProcessOutController RETOUR ACS =====> Exception " + Util.formatException(ex));
			msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
					+ "), votre compte ne sera pas débité, merci de réessayer.";
			demandeDtoMsg.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			autorisationService.logMessage(file, "Fin processoutRequest ()");
			logger.info("Fin processoutRequest ()");
			return page;
		}
		autorisationService.logMessage(file, "Fin processoutRequest ()");
		logger.info("Fin processoutRequest ()");

		return page;
	}

	@PostMapping(value = "/napspayment/processout/authorize", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String authorizeProcessOut(@RequestHeader MultiValueMap<String, String> header, @RequestBody String auths,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_PROCESSOUT_AUTH_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start authorizeProcessOut() ************** ");
		logger.info("*********** Start authorizeProcessOut() ************** ");

		logger.info("authorizeProcessOut api call start ...");

		autorisationService.logMessage(file, "authorizeProcessOut api call start ...");

		autorisationService.logMessage(file, "authorizeProcessOut : [" + auths + "]");

		LinkRequestDto linkRequestDto;

		try {
			linkRequestDto = new ObjectMapper().readValue(auths, LinkRequestDto.class);
		} catch (JsonProcessingException e) {
			autorisationService.logMessage(file, "authorizeProcessOut 500 malformed json expression " + auths + Util.formatException(e));
			return Util.getMsgError(folder, file, null, "authorizeProcessOut 500 malformed json expression", null);
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

		String timeStamp = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS).format(new Date());

		autorisationService.logMessage(file, "authorization_" + linkRequestDto.getOrderid() + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(linkRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Merchant misconfigured in DB or not existing", "");
		}

		GalerieDto galerie = null;

		try {
			galerie = galerieService.findByCodeCmr(linkRequestDto.getMerchantid());
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Galerie misconfigured in DB or not existing", "15");
		}

		if (galerie == null) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Galerie misconfigured in DB or not existing orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Galerie misconfigured in DB or not existing", "15");
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
					"authorizeProcessOut 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err1));

			return Util.getMsgError(folder, file, linkRequestDto, "authorizeProcessOut 500 Error during PaiementRequest",
					null);
		}
		if (check_dmd != null) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Error Already exist in PaiementRequest findByCommandeAndComid linkRequestDto.getOrderid():["
							+ linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto,
					"authorizeProcessOut 500 Error Already exist in PaiementRequest", "16");
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Card number length is incorrect orderid:[" + linkRequestDto.getOrderid() + "] and merchantid:["
							+ linkRequestDto.getMerchantid() + "]");

			return Util.getMsgError(folder, file, linkRequestDto, "The card number is incomplete, please try again.", null);
		}

		if (i_card_valid == 2) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Card number  is not valid incorrect luhn check orderid:[" + linkRequestDto.getOrderid()
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
			if (linkRequestDto.getTransactiontype().equals("P")) {
				// stokage date exp pour utiliser dans la capture (api : .../cpautorisation)
				dmd.setDateexpnaps(expirydate);
			}
			dmd.setLangue("E");
			dmd.setEtatDemande("INIT");

			formatter_1 = new SimpleDateFormat(FORMAT_DEFAUT);
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			// dmd.setDemDateTime(transactiondate + transactiontime);
			dmd.setDemDateTime(dateFormat.format(new Date()));
			if (linkRequestDto.getRecurring() != null && linkRequestDto.getRecurring().equalsIgnoreCase("Y"))
				dmd.setIsCof("Y");
			if (linkRequestDto.getRecurring() != null && linkRequestDto.getRecurring().equalsIgnoreCase("N"))
				dmd.setIsCof("N");

			dmd.setIsAddcard("N");
			dmd.setIsTokenized("N");
			dmd.setIsWhitelist("N");
			dmd.setIsWithsave("N");

			// generer token
			String tokencommande = Util.genTokenCom(dmd.getCommande(), dmd.getComid());
			dmd.setTokencommande(tokencommande);
			// set transctiontype
			dmd.setTransactiontype(linkRequestDto.getTransactiontype());
			// insérer info capture dans le champ Refdemande
			dmd.setRefdemande("capture=" + linkRequestDto.getCapture());

			dmdSaved = demandePaiementService.save(dmd);
			dmdSaved.setExpery(expirydate);

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + linkRequestDto.getOrderid()
							+ "]" + Util.formatException(err1));

			return Util.getMsgError(folder, file, linkRequestDto,
					"The current transaction was not successful, your account will not be debited, please try again .",
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
				autorisationService.logMessage(file, "authorizeProcessOut 500 " + msg);
				return Util.getMsgError(folder, file, linkRequestDto, msg, null);
			}
		} catch (Exception e) {
			dmdSaved.setDemCvv("");
			dmdSaved.setEtatDemande("REJET_RISK_CTRL");
			demandePaiementService.save(dmdSaved);
			autorisationService.logMessage(file,
					"authorizeProcessOut 500 ControlRiskCmr misconfigured in DB or not existing linkRequestDto.getMerchantid():["
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
					"authorizeProcessOut 500 Error during  date formatting for given orderid:[" + linkRequestDto.getOrderid()
							+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err2));

			return Util.getMsgError(folder, file, linkRequestDto, "authorizeProcessOut 500 Error during  date formatting",
					null);
		}

		String uuid_paymentid, paymentid = "", operation_id = "";;
		try {
			uuid_paymentid = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
			operation_id = paymentid;
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
				threeDsecureResponse = autorisationService.preparerProcessOutAeqThree3DSS(dmdSaved, folder, file);
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

			boolean cvv_present = checkCvvPresence(cvv);
			// TODO: controls
			autorisationService.logMessage(file, "Switch processing start ...");

			String tlv = "";
			autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

			if (!cvv_present) {
				dmd.setDemCvv("");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file,
						"authorizeProcessOut 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				return Util.getMsgError(folder, file, linkRequestDto,
						"authorizeProcessOut 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction",
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
							.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
							.withField(Tags.tag168, xid).encode();

				} catch (Exception err4) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"authorizeProcessOut 500 Error during switch tlv buildup for given orderid:[" + linkRequestDto.getOrderid()
									+ "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err4));

					return Util.getMsgError(folder, file, linkRequestDto,
							"authorizeProcessOut 500 Error during switch tlv buildup", "96");
				}

				autorisationService.logMessage(file, "Switch TLV Request :[" + tlv + "]");

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
			}

			catch (SocketTimeoutException e) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction  SocketTimeoutException !!!" + Util.formatException(e));
				switch_ko = 1;
				autorisationService.logMessage(file,
						"authorizeProcessOut 500 Error Switch communication SocketTimeoutException" + "switch ip:["
								+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			}

			catch (IOException e) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction IOException !!!" + Util.formatException(e));
				switch_ko = 1;
				autorisationService.logMessage(file,
						"authorizeProcessOut 500 Error Switch communication IOException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			}

			catch (Exception e) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction Exception!!!" + Util.formatException(e));
				switch_ko = 1;
				// TODO: e.printStackTrace();
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				dmd.setDemCvv("");
				dmd.setEtatDemande("SW_KO");
				demandePaiementService.save(dmd);
				autorisationService.logMessage(file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				autorisationService.logMessage(file, "authorizeProcessOut 500 Error Switch null response"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				return Util.getMsgError(folder, file, linkRequestDto, "Payment failed, the Switch is down.", "96");
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				autorisationService.logMessage(file, "Switch  malfunction resp < 3 !!!");
				autorisationService.logMessage(file,
						"authorizeProcessOut 500 Error Switch short response length() < 3 " + "switch ip:[" + sw_s
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
							"authorizeProcessOut 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				}

				// TODO: controle switch
				if (tag1_resp == null || tag1_resp.length() < 3 || tag20_resp == null) {
					autorisationService.logMessage(file, "Switch  malfunction !!! tag1_resp == null");
					switch_ko = 1;
					autorisationService.logMessage(file,
							"authorizeProcessOut 500 Error during tlv Switch response parse tag1_resp length tag  < 3"
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

			s_status = "";
			try {
				CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(tag20_resp_verified);
				autorisationService.logMessage(file, "" + codeReponseDto);
				if (codeReponseDto != null) {
					s_status = codeReponseDto.getRpcLibelle();
				}
			} catch (Exception ee) {
				autorisationService.logMessage(file, "authorizeProcessOut 500 Error codeReponseDto null" + Util.formatException(ee));
			}
			autorisationService.logMessage(file, "get status Switch status : [" + s_status + "]");

			try {

				hist = new HistoAutoGateDto();
				Date curren_date_hist = new Date();
				int numTransaction = Util.generateNumTransaction(folder, file, curren_date_hist);

				websiteid = dmd.getGalid();

				autorisationService.logMessage(file, "formatting pan...");

				pan_auto = Util.formatagePan(cardnumber);
				autorisationService.logMessage(file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

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
						"authorizeProcessOut 500 Error during  insert in histoautogate for given orderid:[" + linkRequestDto.getOrderid()
								+ "]" + Util.formatException(e));
				try {
					autorisationService.logMessage(file, "2eme tentative : HistoAutoGate Saving ... ");
					hist = histoAutoGateService.save(hist);
				} catch (Exception ex) {
					autorisationService.logMessage(file,
							"2eme tentative : authorizeProcessOut 500 Error during  insert in histoautogate for given orderid:["
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
							"authorizeProcessOut 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
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
				} catch (Exception e) {
					dmd.setDemCvv("");
					demandePaiementService.save(dmd);
					autorisationService.logMessage(file,
							"authorizeProcessOut 500 Error during  DemandePaiement update SW_REJET for given orderid:["
									+ linkRequestDto.getOrderid() + "]" + Util.formatException(e));

					return Util.getMsgError(folder, file, linkRequestDto,
							"authorizeProcessOut 500 Error during  DemandePaiement update SW_REJET", tag20_resp);
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
							"authorizeProcessOut 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:["
									+ linkRequestDto.getOrderid() + "] and merchantid:[" + linkRequestDto.getMerchantid() + "]" + Util.formatException(err2));
				}
				autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
				// TODO: 2024-02-27
			}

			autorisationService.logMessage(file, "Generating paymentid...");

			// TODO: JSONObject jso = new JSONObject();

			autorisationService.logMessage(file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "";

			try {
				authnumber = hist.getHatNautemt() == null ? "" : hist.getHatNautemt();
				coderep = hist.getHatCoderep() == null ? "17" : hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDemPan();
			} catch (Exception e) {
				autorisationService.logMessage(file,
						"authorizeProcessOut 500 Error during authdata preparation orderid:[" + linkRequestDto.getOrderid() + "]" + Util.formatException(e));

				return Util.getMsgError(folder, file, linkRequestDto,
						"authorizeProcessOut 500 Error during authdata preparation", tag20_resp);
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
				jso.put("operation_id", operation_id);
				jso.put("acquRefNbr", "11010");

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
						"authorizeProcessOut 500 Error during jso out processing given authnumber:[" + authnumber + "]"
								+ jsouterr);
				return Util.getMsgError(folder, file, linkRequestDto,
						"authorizeProcessOut 500 Error during jso out processing", tag20_resp);
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
				jso.put("operation_id", operation_id);
				jso.put("acquRefNbr", "11010");

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
				autorisationService.logMessage(file,
						"authorizeProcessOut 500 Error during jso out processing " + Util.formatException(ex));

				return Util.getMsgError(folder, file, linkRequestDto,
						"authorizeProcessOut 500 Error during jso out processing ", null);
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
		autorisationService.logMessage(file, "*********** End authorizeProcessOut() ************** ");
		logger.info("*********** End authorizeProcessOut() ************** ");
		return jso.toString();

	}

	@PostMapping(value = "napspayment/processout/threedsVerify", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String verify3ds(@RequestHeader MultiValueMap<String, String> header, @RequestBody String requestAuth,
			HttpServletResponse response) throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_VERIFY3DS_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start verify3ds() ************** ");
		logger.info("*********** Start verify3ds() ************** ");

		logger.info("verify3ds api call start ...");

		autorisationService.logMessage(file, "verify3ds api call start ...");

		autorisationService.logMessage(file, "verify3ds : [" + requestAuth + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(requestAuth);
		}

		catch (JSONException jserr) {
			autorisationService.logMessage(file, "verify3ds 500 malformed json expression " + requestAuth + jserr);
			return getMsgError(folder, file, null, "verify3ds 500 malformed json expression", null);
		}

		String orderid, merchantid, authnumber = "", coderep, motif, dtdem = "", amount,
				websiteid, cardnumber = "", cvv,transactiondate, transactiontime,
				eci = "", cavv = "", xid = "",expirydate;

		try {
			orderid = (String) jsonOrequest.get("orderid");
			// TODO: Merchnat info
			merchantid = (String) jsonOrequest.get("merchantid");
		} catch (Exception jerr) {
			autorisationService.logMessage(file, "verify3ds 500 malformed json expression " + Util.formatException(jerr));
			return getMsgError(folder, file, null, "verify3ds 500 malformed json expression " + jerr.getMessage(),
					null);
		}

		String timeStamp = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS).format(new Date());

		autorisationService.logMessage(file, "authorization_" + orderid + timeStamp);

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"verify3ds 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		if (current_merchant == null) {
			autorisationService.logMessage(file,
					"verify3ds 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Merchant misconfigured in DB or not existing",
					"15");
		}

		// TODO: get demandepaiement id , check if exist

		DemandePaiementDto check_dmd = null;

		try {
			check_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"verify3ds 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err1));

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Error during PaiementRequest", null);
		}
		if (check_dmd == null) {
			autorisationService.logMessage(file,
					"verify3ds 500 PaiementRequest not found for given orderid:[" + orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 PaiementRequest not found",
					"16");
		}

		String dtpattern, sdt = "", tmpattern, stm = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;
		boolean frictionless = false;
		try {
			amount= String.valueOf(check_dmd.getMontant());
			cardnumber = check_dmd.getDemPan() == null ? "" : check_dmd.getDemPan();
			cvv = check_dmd.getDemCvv() == null ? "" : check_dmd.getDemCvv();
			expirydate = check_dmd.getDateexpnaps() == null ? "" : check_dmd.getDateexpnaps(); // TODO: YYMM
			xid = check_dmd.getDemxid() == null ? "" : check_dmd.getDemxid();
			// TODO: Le cavv deja stocké dans le champ date_SendSWT
			cavv =  check_dmd.getDateSendSWT() == null ? "" : check_dmd.getDateSendSWT();
			// TODO: Le eci deja stocké dans le champ date_sendMPI
			eci = check_dmd.getDateSendMPI() == null ? "" : check_dmd.getDateSendMPI();
			cvv = check_dmd.getDemCvv() == null ? "" : check_dmd.getDemCvv();
			frictionless = (check_dmd.getIs3ds() == null || check_dmd.getIs3ds().equals("Y")) ? true : false;
		} catch (Exception err2) {
			autorisationService.logMessage(file, "verify3ds 500 Error during  date formatting for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Error during  date formatting", null);
		}

		HistoAutoGateDto current_hist = null;

		try {
			// TODO: get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmrAndHatPorteur(orderid, merchantid, cardnumber);

		} catch (Exception err2) {
			autorisationService.logMessage(file,
					"verify3ds 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrAndHatPorteur orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Transaction not found", null);
		}

		if (current_hist == null) {
			autorisationService.logMessage(file, "verify3ds 500 Transaction not found for orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Transaction not found", null);
		}

		autorisationService.logMessage(file, "Preparing verify3ds api response");

		try {
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
			authnumber = current_hist.getHatNautemt() == null ? "" : current_hist.getHatNautemt();
			coderep = current_hist.getHatCoderep() == null ? "17" : current_hist.getHatCoderep();
			motif = "authorized";
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"verify3ds 500 Error during authdata preparation orderid:[" + orderid + "]" + Util.formatException(e));
			coderep = "96";
			motif = "Operation failed";
		}

		JSONObject jso = new JSONObject();

		try {
			// TODO: Transaction info
			jso.put("statuscode", coderep);
			jso.put("status", motif);
			jso.put("etataut", "Y");
			jso.put("orderid", orderid);
			jso.put("authnumber", authnumber);
			jso.put("amount", amount);
			jso.put("transactiondate", sdt);
			jso.put("transactiontime", stm);
			jso.put("cavv", cavv);
			jso.put("xid", xid);
			jso.put("eci", eci);
			jso.put("frictionless", frictionless);

			// TODO: Card info
			jso.put("cardnumber", Util.formatCard(cardnumber));

		} catch (Exception jsouterr) {
			autorisationService.logMessage(file,
					"verify3ds 500 Error during jso out processing given authnumber:[" + authnumber + "]" + jsouterr);
			return getMsgError(folder, file, jsonOrequest, "verify3ds 500 Error during jso out processing", "96");
		}

		logger.info("verify3ds api response frictionless :  [" + jso.toString() + "]");
		autorisationService.logMessage(file, "verify3ds api response frictionless :  [" + jso.toString() + "]");

		autorisationService.logMessage(file, "*********** End verify3ds() ************** ");
		logger.info("*********** End verify3ds() ************** ");
		return jso.toString();

	}

	@PostMapping(value = "/napspayment/infoTrs", consumes = "application/json", produces = "application/json")
	@ResponseBody
	@SuppressWarnings("all")
	public String infoTrs(@RequestHeader MultiValueMap<String, String> header, @RequestBody String infoTrs,
			HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "API_InfoTrs_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "*********** Start infoTrs() ************** ");
		logger.info("*********** Start infoTrs() ************** ");

		autorisationService.logMessage(file, "infoTrs api call start ...");
		autorisationService.logMessage(file, "infoTrs : [" + infoTrs + "]");

		JSONObject jsonOrequest = null;
		try {
			jsonOrequest = new JSONObject(infoTrs);
		}

		catch (JSONException jserr) {
			autorisationService.logMessage(file, "infoTrs 500 malformed json expression " + infoTrs + jserr);
			return getMsgError(folder, file, null, "infoTrs 500 malformed json expression", null);
		}

		String orderid, authnumber = "", merchantid, cardnumber = "";
		try {
			// TODO: Transaction info
			orderid = (String) jsonOrequest.get("orderid");

			// TODO: Merchant info
			merchantid = (String) jsonOrequest.get("merchantid");
		} catch (Exception jerr) {
			autorisationService.logMessage(file, "infoTrs 500 malformed json expression " + infoTrs + Util.formatException(jerr));
			return getMsgError(folder, file, null, "infoTrs 500 malformed json expression " + jerr.getMessage(), null);
		}

		JSONObject jso = new JSONObject();

		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		autorisationService.logMessage(file, "infoTrs_" + orderid + timeStamp);

		DemandePaiementDto current_dmd = null;

		try {
			current_dmd = demandePaiementService.findByCommandeAndComid(orderid, merchantid);

		} catch (Exception err1) {
			autorisationService.logMessage(file,
					"infoTrs 500 Error during PaiementRequest findByCommandeAndComid orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err1));
			return getMsgError(folder, file, jsonOrequest, "infoTrs 500 Error during PaiementRequest", null);
		}
		if (current_dmd == null) {
			autorisationService.logMessage(file, "infoTrs 500 PaiementRequest not found for given orderid:["
					+ orderid + "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "infoTrs 500 Transaction not found", null);
		}
		try {
			cardnumber = current_dmd.getDemPan();
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"infoTrs 500 Error during  formating cardnumber for given orderid:[" + orderid + "]" + Util.formatException(e));
		}
		HistoAutoGateDto current_hist = null;

		try {
			// TODO: get histoauto check if exist
			current_hist = histoAutoGateService.findByHatNumCommandeAndHatNumcmrAndHatPorteur(orderid, merchantid, cardnumber);

		} catch (Exception err2) {
			autorisationService.logMessage(file,
					"infoTrs 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrAndHatPorteur orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));

			return getMsgError(folder, file, jsonOrequest, "infoTrs 500 Transaction not found", null);
		}

		if (current_hist == null) {
			autorisationService.logMessage(file, "infoTrs 500 Transaction not found for orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "infoTrs 500 Transaction not found", null);
		}

		String infoTrs_id = "", dtpattern, sdt = "", tmpattern, stm = "";
		Date dt = null;
		SimpleDateFormat sfdt = null;
		SimpleDateFormat sftm = null;

		try {
			infoTrs_id = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
			authnumber = current_hist.getHatNautemt();
			dt = new Date();
			dtpattern = FORMAT_DEFAUT;
			sfdt = new SimpleDateFormat(dtpattern);
			sdt = sfdt.format(dt);
			tmpattern = "HH:mm:ss";
			sftm = new SimpleDateFormat(tmpattern);
			stm = sftm.format(dt);
		} catch (Exception e) {
			autorisationService.logMessage(file,
					"infoTrs 500 Error during  infoTrs_id generation for given orderid:[" + orderid + "]" + Util.formatException(e));
		}

		try {
			// TODO: Transaction info
			jso.put("statuscode", "00");
			jso.put("status", "authorized");
			jso.put("orderid", orderid);
			jso.put("authnumber", authnumber);
			jso.put("infoTrsid", infoTrs_id);
			jso.put("infoTrdate", sdt);
			jso.put("infoTrtime", stm);

			// TODO: Merchant info
			jso.put("merchantid", merchantid);

			autorisationService.logMessage(file, "json res : [" + jso.toString() + "]");
			logger.info("json res : [" + jso.toString() + "]");

		} catch (Exception err8) {
			autorisationService.logMessage(file, "infoTrs 500 Error during jso out processing given authnumber"
					+ "authnumber:[" + authnumber + "]" + Util.formatException(err8));

			return getMsgError(folder, file, jsonOrequest, "infoTrs 500 Error during jso out processing", null);
		}

		autorisationService.logMessage(file, "*********** End infoTrs() ************** ");
		logger.info("*********** End infoTrs() ************** ");

		return jso.toString();

	}

	@SuppressWarnings("all")
	public String getMsgError(String folder, String file, JSONObject jsonOrequest, String msg, String coderep) {
		autorisationService.logMessage(file, "*********** Start getMsgError() ************** ");
		logger.info("*********** Start getMsgError() ************** ");

		JSONObject jso = new JSONObject();
		if (jsonOrequest != null) {
			jso.put("orderid", (String) jsonOrequest.get("orderid"));
			jso.put("merchantid", (String) jsonOrequest.get("merchantid"));
		}
		if (coderep != null) {
			jso.put("statuscode", coderep);
		} else {
			jso.put("statuscode", "17");
		}

		jso.put("status", msg);
		jso.put("etataut", "N");

		autorisationService.logMessage(file, "json : " + jso.toString());
		logger.info("json : " + jso.toString());

		autorisationService.logMessage(file, "*********** End getMsgError() ************** ");
		logger.info("*********** End getMsgError() ************** ");
		return jso.toString();
	}

	@SuppressWarnings("all")
	public static String sendPOST(String urlcalback, String clepub, String idcommande, String repauto, String montant,
			String numAuto, Long numTrans, String token_gen, String pan_trame, String typecarte, String folder,
			String file) throws IOException {

		String result = "";
		HttpPost post = new HttpPost(urlcalback);

		String reqenvoi = idcommande + repauto + clepub + montant;
		String signature = Util.hachInMD5(reqenvoi);
		Util.writeInFileTransaction(folder, file,"Signature : " + signature);
		// TODO: add request parameters or form parameters
		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("repauto", repauto));
		urlParameters.add(new BasicNameValuePair("montant", montant));
		urlParameters.add(new BasicNameValuePair("signature", signature));
		urlParameters.add(new BasicNameValuePair("numAuto", numAuto));
		urlParameters.add(new BasicNameValuePair("numTrans", String.valueOf(numTrans)));
		urlParameters.add(new BasicNameValuePair("idcommande", idcommande));
		urlParameters.add(new BasicNameValuePair("token", token_gen));
		urlParameters.add(new BasicNameValuePair("carte", pan_trame));
		urlParameters.add(new BasicNameValuePair("typecarte", typecarte));
		post.setEntity(new UrlEncodedFormEntity(urlParameters));

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				httpClient = (CloseableHttpClient) getAllSSLClient();
			} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
				Util.writeInFileTransaction(folder, file,"[GW-EXCEPTION-KeyManagementException] sendPOST " + e1);
			}
			Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 1");
			CloseableHttpResponse response = httpClient.execute(post);

			result = EntityUtils.toString(response.getEntity());

		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file,
					" sendPOST Exception => {} tv 1 :" + ex.getMessage() + "ex : " + Util.formatException(ex));
			result = "ko";
		}
		boolean repsucces = result.indexOf("GATESUCCESS") != -1 ? true : false;
		boolean repfailed = result.indexOf("GATEFAILED") != -1 ? true : false;
		if (!repsucces && !repfailed) {
			try {
				Thread.sleep(10000);
				Util.writeInFileTransaction(folder, file,idcommande + " Recall URL tentative 2");
				// TODO: tentative 2 apès 10 s
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse response = httpClient.execute(post)) {

					result = EntityUtils.toString(response.getEntity());
				}
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file,
						" sendPOST Exception => {} tv 2 :" + ex.getMessage() + "ex : " + Util.formatException(ex));
				result = "ko";
			}
			boolean repsucces2 = result.indexOf("GATESUCCESS") != -1 ? true : false;
			boolean repfailed2 = result.indexOf("GATEFAILED") != -1 ? true : false;
			if (!repsucces2 && !repfailed2) {
				try {
					Thread.sleep(10000);
					// TODO: tentative 3 après 10s
					Util.writeInFileTransaction(folder, file,idcommande + " Recall URL tentative 3");
					try (CloseableHttpClient httpClient = HttpClients.createDefault();
							CloseableHttpResponse response = httpClient.execute(post)) {

						result = EntityUtils.toString(response.getEntity());
					}
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							" sendPOST Exception => {} tv 3 :" + ex.getMessage() + "ex : " + Util.formatException(ex));
					result = "ko";
				}
			}
		}

		return result;
	}

	@SuppressWarnings("all")
	public String annulationAuto(DemandePaiementDto current_dmd, CommercantDto current_merchant,
			HistoAutoGateDto current_hist, Model model, String folder, String file) {

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		String[] mm;
		String[] m;
		String montanttrame = "";
		String amount = String.valueOf(current_dmd.getMontant());
		String orderid = current_dmd.getCommande();
		String merchantid = current_dmd.getComid();
		String page = "index";

		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";
		} catch (Exception err3) {
			autorisationService.logMessage(file,
					"annulation auto 500 Error during date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err3));

			return "96";
		}

		// TODO: 2024-03-05
		montanttrame = Util.formatMontantTrame(folder, file, amount, orderid, merchantid, current_dmd, model);

		autorisationService.logMessage(file, "Switch processing start ...");

		String tlv = "";
		autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

		// TODO: controls
		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		String merchantname = current_merchant.getCmrAbrvnom();
		String cardnumber = current_dmd.getDemPan();
		String authnumber = current_hist.getHatNautemt();
		String merchant_name = merchantname;

		String mesg_type = "2";
		String acq_type = "0000";
		String processing_code = "0";
		String reason_code = "H";
		String transaction_condition = "6";
		String transactionnumber = current_hist.getHatNautemt();
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

		} catch (Exception err4) {
			autorisationService.logMessage(file,
					"annulation auto 500 Error during switch tlv buildu for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));

			return "96";
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

				return "96";
			}

		} catch (Exception e) {
			autorisationService.logMessage(file, "Switch  malfunction !!!" + Util.formatException(e));
			return "96";
		}

		String resp = resp_tlv;
		if (resp == null) {
			autorisationService.logMessage(file, "Switch  malfunction !!!");
			return "96";
		}

		if (resp.length() < 3) {
			autorisationService.logMessage(file, "Switch  malfunction !!! short response length() < 3 switch");
			return "96";
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
			autorisationService.logMessage(file, "Switch  malfunction !!! response parse switch" + Util.formatException(e));
			return "96";
		}

		// TODO: controle switch
		if (tag1_resp == null) {
			autorisationService.logMessage(file, "Switch  malfunction !!! response parse tag1_resp tag null");
			return "96";
		}

		if (tag1_resp.length() < 3) {
			autorisationService.logMessage(file, "Switch  malfunction !!! response parse tag1_resp length tag  < 3 switch");
			return "96";
		}

		autorisationService.logMessage(file, "Switch TLV Respnose Processed");

		if (tag20_resp == null) {
			return "96";
		}
		if (tag20_resp.length() < 1) {
			return "96";
		}

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
						"annulation auto 500 Error during  demandepaiement update  A for given orderid:[" + orderid
								+ "]" + Util.formatException(e));

				return "96";
			}

			autorisationService.logMessage(file, "Setting DemandePaiement status OK.");

			autorisationService.logMessage(file, "Setting HistoAutoGate status A ...");

			// TODO: 2024-03-15
			try {
				if (current_hist.getId() == null) {
					// TODO: get histoauto check if exist
					HistoAutoGateDto histToAnnulle = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr(orderid,
							merchantid);
					if (histToAnnulle != null) {
						autorisationService.logMessage(file,
								"transaction declinded ==> update HistoAutoGateDto etat to A ...");
						histToAnnulle.setHatEtat('A');
						histToAnnulle = histoAutoGateService.save(histToAnnulle);
					} else {
						current_hist.setHatEtat('A');
						current_hist = histoAutoGateService.save(current_hist);
					}
				} else {
					current_hist.setHatEtat('A');
					current_hist = histoAutoGateService.save(current_hist);
				}

			} catch (Exception err2) {
				autorisationService.logMessage(file,
						"annulation auto 500 Error during HistoAutoGate findLastByHatNumCommandeAndHatNumcmr orderid:["
								+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
			}
			autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
			// TODO: 2024-03-15

			autorisationService.logMessage(file, "Setting HistoAutoGate status OK.");
		} else {

			autorisationService.logMessage(file, "Transaction annulation auto declined.");
			autorisationService.logMessage(file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		return tag20_resp;
	}

	private boolean isReccuringCheck(String recurring) {
		return recurring.equalsIgnoreCase("Y");
	}

	private boolean checkCvvPresence(String cvv) {
		return cvv != null && cvv.length() == 3;
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
			return d;
		}
		return dNotime;

	}

	@SuppressWarnings("all")
	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"}) // Suppression intentionnelle : La validation des certificats est désactivée par choix
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"}) // Suppression intentionnelle : La validation des certificats est désactivée par choix
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		// TODO: modified 2024-0-30
		// TODO: SSLContext context = SSLContext.getInstance("SSL");
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, trustAllCerts, null);

		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		builder.setSSLSocketFactory(sslConnectionFactory);

		PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();

		return builder.build();

	}
}
