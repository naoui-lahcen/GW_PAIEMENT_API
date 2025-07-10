package ma.m2m.gateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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
import javax.xml.namespace.QName;

import ma.m2m.gateway.dto.*;
import ma.m2m.gateway.service.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.lydec.DemandesReglements;
import ma.m2m.gateway.lydec.GererEncaissement;
import ma.m2m.gateway.lydec.GererEncaissementService;
import ma.m2m.gateway.lydec.Impaye;
import ma.m2m.gateway.lydec.MoyenPayement;
import ma.m2m.gateway.lydec.Portefeuille;
import ma.m2m.gateway.lydec.ReponseReglements;
import ma.m2m.gateway.lydec.Transaction;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.CRes;
import ma.m2m.gateway.threedsecure.RequestEnvoieEmail;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */
import ma.m2m.gateway.utils.Util;

@Controller
public class ACSController {

	private static final Logger logger = LogManager.getLogger(ACSController.class);
	private LocalDateTime date;
	private String folder;
	private String file;
	private SplittableRandom splittableRandom = new SplittableRandom();
	long randomWithSplittableRandom;

	private Gson gson;

	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDSS;

	@Value("${key.SWITCH_URL}")
	private String ipSwitch;

	@Value("${key.SWITCH_PORT}")
	private String portSwitch;

	@Value("${key.LYDEC_PREPROD}")
	private String lydecPreprod;

	@Value("${key.LYDEC_PROD}")
	private String lydecProd;

	@Value("${key.URL_WSDL_LYDEC}")
	private String urlWsdlLydec;

	@Value("${key.DGI_PREPROD}")
	private String dgiPreprod;

	@Value("${key.DGI_PROD}")
	private String dgiProd;

	@Value("${key.LIEN_ENVOIE_EMAIL_DGI}")
	private String lienEnvoieEmailDgi;

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
	private final HistoAutoGateService histoAutoGateService;

	//@Autowired
	private final CardtokenService cardtokenService;

	//@Autowired
	private final CodeReponseService codeReponseService;

	//@Autowired
	private final FactureLDService factureLDService;

	//@Autowired
	private final ArticleDGIService articleDGIService;

	//@Autowired
	private final CFDGIService cfdgiService;

	private final ReccuringTransactionService recService;

	private final EmetteurService emetteurService;
	
	public static final String DF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	DateFormat dateFormat = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
	DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

	private static final QName SERVICE_NAME = new QName("http://service.lydec.com", "GererEncaissementService");

	public ACSController(DemandePaiementService demandePaiementService, AutorisationService autorisationService,
			HistoAutoGateService histoAutoGateService, CommercantService commercantService, 
			InfoCommercantService infoCommercantService,
			CardtokenService cardtokenService, CodeReponseService codeReponseService,
			FactureLDService factureLDService, ArticleDGIService articleDGIService,
			CFDGIService cfdgiService, ReccuringTransactionService recService,
	        EmetteurService emetteurService) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "R_ACS_" + randomWithSplittableRandom;
		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
		this.demandePaiementService = demandePaiementService;
		this.autorisationService = autorisationService;
		this.histoAutoGateService = histoAutoGateService;
		this.commercantService = commercantService;
		this.infoCommercantService = infoCommercantService;
		this.cardtokenService = cardtokenService;
		this.codeReponseService = codeReponseService;
		this.factureLDService = factureLDService;
		this.articleDGIService = articleDGIService;
		this.cfdgiService = cfdgiService;
		this.recService = recService;
		this.emetteurService = emetteurService;
	}

	@PostMapping("/napspayment/acs")
	@SuppressWarnings("all")
	public String processRequest(HttpServletRequest request, HttpServletResponse response, Model model, HttpSession session)
			throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "R_ACS_" + randomWithSplittableRandom;
		// TODO: create file log
		Util.creatFileTransaction(file);
		autorisationService.logMessage(file, "Start processRequest ()");
		logger.info("Start processRequest ()");
		CRes cleanCres = new CRes();
		String msgRefus = "";
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		String page = "index";
	    
		try {
			String encodedCres = request.getParameter("cres");
			logger.info("ACSController RETOUR ACS =====> encodedCres : " + encodedCres);
			autorisationService.logMessage(file, "ACSController RETOUR ACS =====> encodedCres : " + encodedCres);

			String decodedCres = "";

			decodedCres = new String(Base64.decodeBase64(encodedCres.getBytes()));
			if (decodedCres.indexOf("}") != -1) {
				decodedCres = decodedCres.substring(0, decodedCres.indexOf("}") + 1);
			}
			autorisationService.logMessage(file, "ACSController RETOUR ACS =====> decodedCres : " + decodedCres);
			logger.info("ACSController RETOUR ACS =====> decodedCres : " + decodedCres);

			cleanCres = gson.fromJson(decodedCres, CRes.class);
			autorisationService.logMessage(file, "ACSController RETOUR ACS =====> cleanCres : " + cleanCres);

			autorisationService.logMessage(file, "transStatus/threeDSServerTransID : " + cleanCres.getTransStatus()
					+ "/" + cleanCres.getThreeDSServerTransID());

			// TODO: just for test
			// TODO: cleanCres.setTransStatus("N");

			if (cleanCres.getTransStatus().equals("Y") || cleanCres.getTransStatus().equals("N")) {
				logger.info("ACSController RETOUR ACS =====> getRreqFromThree3DSSAfterACS ");
				autorisationService.logMessage(file, "ACSController RETOUR ACS =====> getRreqFromThree3DSSAfterACS ");

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
				String acq_type = "0000";
				String merchant_city = "MOROCCO        ";
				String reason_code = "H";
				String transaction_condition = "6";
				String mesg_type = "0";
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
				String token_gen = "";
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
				String failURL = "";

				if (threeDsecureResponse != null && threeDsecureResponse.getEci() != null) {
					if (threeDsecureResponse.getEci().equals("05") || threeDsecureResponse.getEci().equals("02")
							|| threeDsecureResponse.getEci().equals("06")
							|| threeDsecureResponse.getEci().equals("01")) {

						autorisationService.logMessage(file,
								"if(eci=05) || eci=02 || eci=06 || eci=01) : continue le processus");

						reponseMPI = threeDsecureResponse.getTransStatus() == null ? threeDsecureResponse.getReponseMPI() : threeDsecureResponse.getTransStatus();
						if(threeDsecureResponse != null && threeDsecureResponse.getMessageType() != null) {
							if(threeDsecureResponse.getMessageType().equals("Erro")) {
								reponseMPI = "E";
								errmpi = threeDsecureResponse.getErrorDetail();
							}
						} else {
							errmpi = threeDsecureResponse.getErrmpi() == null ? "" : threeDsecureResponse.getErrmpi();
						}

						threeDSServerTransID = threeDsecureResponse.getThreeDSServerTransID();

						eci = threeDsecureResponse.getEci() == null ? "" : threeDsecureResponse.getEci();

						cavv = threeDsecureResponse.getAuthenticationValue() == null ? threeDsecureResponse.getCavv() : threeDsecureResponse.getAuthenticationValue();

						expiry = threeDsecureResponse.getExpiry() == null ? "" : threeDsecureResponse.getExpiry();

						idDemande = threeDsecureResponse.getIdDemande();

						if(idDemande == null) {
							autorisationService.logMessage(file,"idDemande null => return idDemande by findByDem_xid " + cleanCres.getThreeDSServerTransID());
							dmd = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());
						} else {
							dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));
						}

						if (dmd == null) {
							autorisationService.logMessage(file,
									"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
											+ idDemande);
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							autorisationService.logMessage(file, "Fin processRequest ()");
							logger.info("Fin processRequest ()");
							//return page;
							failURL = autorisationService.getFailUrl(cleanCres.getThreeDSServerTransID());
							response.sendRedirect(failURL);
							return null;
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
						recurring = dmd.getIs3ds() == null ? "N" : dmd.getIs3ds();
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
						token_gen = dmd.getToken() == null ? "" : dmd.getToken();

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
							autorisationService.logMessage(file, "Fin processRequest ()");
							logger.info("Fin processRequest ()");
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
							
							// TODO: generation et envoie du token de la carte enregitré dans le successURL
							// TODO: apres le paiment de 0 DH de check porteur carte
							if (dmd.getIsAddcard().equals("Y") && dmd.getIsTokenized().equals("Y")
									&& dmd.getIsWithsave().equals("Y") && dmd.getIsCof().equals("Y")) {
								boolean flag = false;
								String tokencard = "";
								String data_noncrypt_token = "";
								String data_token = "";
								CardtokenDto cardtokenDto = new CardtokenDto();
								
								String plainTxtSignature = orderid + current_infoCommercant.getClePub();

								autorisationService.logMessage(file, "plainTxtSignature : " + plainTxtSignature);
								logger.info("plainTxtSignature : " + plainTxtSignature);
								try {
									// TODO: insert new cardToken
									tokencard = Util.generateCardToken(merchantid);
									// TODO: test if token not exist in DB
									CardtokenDto checkCardToken = cardtokenService
											.findByIdMerchantAndToken(merchantid, tokencard);

									while (checkCardToken != null) {
										tokencard = Util.generateCardToken(merchantid);
										System.out
												.println("checkCardToken exist => generate new tokencard : "
														+ tokencard);
										autorisationService.logMessage(file,
												"checkCardToken exist => generate new tokencard : "
														+ tokencard);
										checkCardToken = cardtokenService
												.findByIdMerchantAndToken(merchantid, tokencard);
									}
									autorisationService.logMessage(file, "tokencard : " + tokencard);

									cardtokenDto.setToken(tokencard);
									String tokenid = UUID.randomUUID().toString();
									cardtokenDto.setIdToken(tokenid);
									Calendar dateCalendar = Calendar.getInstance();
									Date dateToken = dateCalendar.getTime();
									expirydate = dmd.getDateexpnaps();
									autorisationService.logMessage(file,
											"cardtokenDto expirydate : " + expirydate);
									String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
									// TODO: get year from date
									String year = anne.substring(0, 2) + expirydate.substring(0, 2);
									String moi = expirydate.substring(2, expirydate.length());
									// TODO: format date to "yyyy-MM-dd"
									String expirydateFormated = year + "-" + moi + "-" + "01";
									autorisationService.logMessage(file,
											"cardtokenDto expirydate formated : " + expirydateFormated);
									Date dateExp = dateFormatSimple.parse(expirydateFormated);
									cardtokenDto.setExprDate(dateExp);
									String dateTokenStr = dateFormat.format(dateToken);
									Date dateTokenFormated = dateFormat.parse(dateTokenStr);
									cardtokenDto.setTokenDate(dateTokenFormated);
									cardtokenDto.setCardNumber(cardnumber);
									cardtokenDto.setIdMerchant(merchantid);
									cardtokenDto.setIdMerchantClient(merchantid);
									cardtokenDto.setFirstName(fname);
									cardtokenDto.setLastName(lname);
									cardtokenDto.setHolderName(holdername);
									cardtokenDto.setMcc(merchantid);

									CardtokenDto cardtokenSaved = cardtokenService.save(cardtokenDto);

									autorisationService.logMessage(file, "Saving CARDTOKEN OK");
									data_noncrypt_token = "id_commande=" + orderid + "&montant=" + amount
											+ "&repauto=" + "00" + "&numAuto=" + "123456"
											+ "&numCarte=" + Util.formatCard(cardnumber) + "&numTrans="
											+ transactionid + "&token=" + cardtokenSaved.getToken();
									
									autorisationService.logMessage(file,
											"data_noncrypt_token : " + data_noncrypt_token);
									logger.info("data_noncrypt_token : " + data_noncrypt_token);

									data_token = RSACrypto.encryptByPublicKeyWithMD5Sign(
											data_noncrypt_token, current_infoCommercant.getClePub(),
											plainTxtSignature, folder, file);

									autorisationService.logMessage(file,
											"data_token encrypt : " + data_token);
									logger.info("data_token encrypt : " + data_token);
									
									autorisationService.logMessage(file, "redirect to SuccessURL : " + dmd.getSuccessURL());
									
									response.sendRedirect(dmd.getSuccessURL() + "?data=" + data_token
											+ "==&codecmr=" + merchantid);
									autorisationService.logMessage(file, "Fin processRequest ()");
									return null; // TODO: Terminer le traitement ici après avoir envoyé la réponse
								} catch (Exception e) {
									autorisationService.logMessage(file,
											"authorization 500 Error during saving CRADTOKEN " + Util.formatException(e));
									flag = true;
								}
								if (flag) {
									autorisationService.logMessage(file, "Error during saving CRADTOKEN redirect to FailUrl : " + dmd.getFailURL());
									logger.info("Error during saving CRADTOKEN redirect to FailUrl : " + dmd.getFailURL());
									response.sendRedirect(dmd.getFailURL());
									return null; // TODO: Terminer le traitement ici après avoir envoyé la réponse
								}
							}

							if (dmd.getEtatDemande().equals("SW_PAYE") || dmd.getEtatDemande().equals("PAYE")) {
								dmd.setDemCvv("");
								demandePaiementService.save(dmd);
								autorisationService.logMessage(file, "Opération déjà effectuée, redirection vers failUrl");
								autorisationService.logMessage(file, "Fin processRequest ()");
								response.sendRedirect(dmd.getFailURL());
								return null;
							}
							
							// TODO: 2024-03-05
							montanttrame = Util.formatMontantTrame(folder, file, amount, orderid, merchantid, dmd, model);

							processing_code = dmd.getTransactiontype();

							Date curren_date = new Date();
							int numTransaction = Util.generateNumTransaction(folder, file, curren_date);
							String numTrsStr = Util.formatNumTrans(String.valueOf(numTransaction));

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
							boolean is_reccuring = isReccuringCheck(recurring);
							boolean is_first_trs = true;

							String first_auth = "";
							long lrec_serie = 0;
							String rec_serie = "";

							autorisationService.logMessage(file, "Switch processing start ...");

							String tlv = "";
							autorisationService.logMessage(file, "Preparing Switch TLV Request start ...");

							if (!cvv_present && !is_reccuring) {
								dmd.setDemCvv("");
								demandePaiementService.save(dmd);
								autorisationService.logMessage(file,
										"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");
								autorisationService.logMessage(file, "Fin processRequest ()");
								logger.info("Fin processRequest ()");
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
											.withField(Tags.tag18, numTrsStr).withField(Tags.tag42, expirydate)
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
									autorisationService.logMessage(file, "Fin processRequest ()");
									logger.info("Fin processRequest ()");
									response.sendRedirect(failURL);
									return null;
								}

								autorisationService.logMessage(file, "Switch TLV Request :[" + Util.getTLVPCIDSS(tlv, folder, file) + "]");

							}

							// TODO: 12-06-2025 implemente reccuring payment
							if (is_reccuring) {
								{
									is_first_trs = isFirstTransaction(merchantid, cardnumber);

									// card uknown in system ==> first transaction
									autorisationService.logMessage(file, "is_first_trs : " + is_first_trs);
									autorisationService.logMessage(file, "reccurent_cvv_check_obligatory : " + reccurent_cvv_check_obligatory);

									if (is_first_trs) {
										if (!cvv_present) { // is the cvv present ?
											if (reccurent_cvv_check_obligatory) { // is the cvv obligatory ? national switch yes
												dmd.setDemCvv("");
												demandePaiementService.save(dmd);
												autorisationService.logMessage(file,
														"authorization 500 cvv not set , reccuring flag set to Y and first transaction is detected orderid:[" + orderid + "] and merchantid:[" + merchantid + "]");
												autorisationService.logMessage(file, "Fin processRequest ()");
												logger.info("Fin processRequest ()");
												response.sendRedirect(failURL);
												return null;
											} else {
												// cvv not obligatory in first transaction, international
												autorisationService.logMessage(file, "cvv not obligatory in first transaction, international");
												try {
													tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
															.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
															.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
															.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
															.withField(Tags.tag18, numTrsStr).withField(Tags.tag42, expirydate)
															.withField(Tags.tag16, date).withField(Tags.tag17, heure)
															.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
															.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn)
															.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
															.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
															.withField(Tags.tag168, xid).withField(Tags.tag601, "R111111111").encode();
												} catch (Exception err4) {
													dmd.setDemCvv("");
													demandePaiementService.save(dmd);
													autorisationService.logMessage(file,
															"authorization 500 Error during switch tlv buildup for given orderid:["
																	+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
													autorisationService.logMessage(file, "Fin processRequest ()");
													logger.info("Fin processRequest ()");
													response.sendRedirect(failURL);
													return null;
												}
												autorisationService.logMessage(file, "Switch TLV Request :[" + Util.getTLVPCIDSS(tlv, folder, file) + "]");
											}
										} else { // first transaction with cvv present, a normal transaction
											autorisationService.logMessage(file, "first transaction with cvv present, a normal transaction");
											try {
												tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
														.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
														.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
														.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
														.withField(Tags.tag18, numTrsStr).withField(Tags.tag42, expirydate)
														.withField(Tags.tag16, date).withField(Tags.tag17, heure)
														.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
														.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
														.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
														.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
														.withField(Tags.tag168, xid)/*.withField(Tags.tag601, "R111111111")*/.encode();
											} catch (Exception err4) {
												dmd.setDemCvv("");
												demandePaiementService.save(dmd);
												autorisationService.logMessage(file,
														"authorization 500 Error during switch tlv buildup for given orderid:["
																+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
												autorisationService.logMessage(file, "Fin processRequest ()");
												logger.info("Fin processRequest ()");
												response.sendRedirect(failURL);
												return null;
											}
											autorisationService.logMessage(file, "Switch TLV Request :[" + Util.getTLVPCIDSS(tlv, folder, file) + "]");
										}

									} else { // reccuring
										autorisationService.logMessage(file, "trs already existe");
										try {
											first_auth = getFirstTransactionAuth(merchantid, cardnumber);
											lrec_serie = getTransactionSerie(merchantid, cardnumber);

										} catch (Exception e) {
											dmd.setDemCvv("");
											demandePaiementService.save(dmd);
											autorisationService.logMessage(file,
													"authorization 500 Error during switch tlv buildup for given orderid:["
															+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(e));
											autorisationService.logMessage(file, "Fin processRequest ()");
											logger.info("Fin processRequest ()");
											response.sendRedirect(failURL);
											return null;
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
													.withField(Tags.tag18, numTrsStr).withField(Tags.tag42, expirydate)
													.withField(Tags.tag16, date).withField(Tags.tag17, heure)
													.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
													.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn).withField(Tags.tag67, cvv)
													.withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
													.withField(Tags.tag90, acqcode).withField(Tags.tag167, champ_cavv)
													.withField(Tags.tag168, xid).withField(Tags.tag601, "R" + rec_serie + first_auth)
													.encode();
										} catch (Exception err4) {
											dmd.setDemCvv("");
											demandePaiementService.save(dmd);
											autorisationService.logMessage(file,
													"authorization 500 Error during switch tlv buildup for given orderid:["
															+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err4));
											autorisationService.logMessage(file, "Fin processRequest ()");
											logger.info("Fin processRequest ()");
											response.sendRedirect(failURL);
											return null;
										}
										autorisationService.logMessage(file, "Switch TLV Request :[" + Util.getTLVPCIDSS(tlv, folder, file) + "]");
									}
								}
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
									autorisationService.logMessage(file, "Fin processRequest ()");
									logger.info("Fin processRequest ()");
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
								autorisationService.logMessage(file, "Fin processRequest ()");
								logger.info("Fin processRequest ()");
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
								autorisationService.logMessage(file, "Fin processRequest ()");
								logger.info("Fin processRequest ()");
								response.sendRedirect(failURL);
								return null;
							}

							autorisationService.logMessage(file, "Switch TLV Respnose :[" + Util.getTLVPCIDSS(resp_tlv, folder, file) + "]");

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
									autorisationService.logMessage(file, "Fin processRequest ()");
									logger.info("Fin processRequest ()");
									response.sendRedirect(failURL);
									return null;
								}

								// TODO: TODO: controle switch
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

							// TODO: TODO: SWHistoAutoDto swhist = null;

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
								logger.info("codeReponseDto : " + codeReponseDto);
								autorisationService.logMessage(file, "" + codeReponseDto);
								if (codeReponseDto != null) {
									s_status = codeReponseDto.getRpcLibelle();
								}
							} catch (Exception ee) {
								autorisationService.logMessage(file,"authorization 500 Error codeReponseDto null" + Util.formatException(ee));
							}
							autorisationService.logMessage(file,"get status Switch status : [" + s_status + "]");

							try {

								hist = new HistoAutoGateDto();
								Date curren_date_hist = new Date();
								
								websiteid = dmd.getGalid();

								autorisationService.logMessage(file, "formatting pan...");

								pan_auto = Util.formatagePan(cardnumber);

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

								if (recurring.equalsIgnoreCase("Y"))
									hist.setIsCof("Y");
								if (recurring.equalsIgnoreCase("N"))
									hist.setIsCof("N");

								autorisationService.logMessage(file, "HistoAutoGate data filling end ...");

								autorisationService.logMessage(file, "HistoAutoGate Saving ...");

								hist = histoAutoGateService.save(hist);
								
								autorisationService.logMessage(file, "hatNomdeandeur : " + hist.getHatNomdeandeur());

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
									if(dmd.getTransactiontype().equals("0")) {
										dmd.setDemCvv("");
									}									
									demandePaiementService.save(dmd);
									autorisationService.logMessage(file, "update etat demande : SW_PAYE OK");

								} catch (Exception e) {
									autorisationService.logMessage(file,
											"authorization 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:["
													+ orderid + "]" + Util.formatException(e));
								}
								
								// TODO: 2023-11-27 preparation reconciliation Ecom Lydec
								if (lydecPreprod.equals(merchantid) || lydecProd.equals(merchantid)) {
									List<FactureLDDto> listFactureLD = new ArrayList<>();
									listFactureLD = factureLDService.findFactureByIddemande(dmd.getIddemande());
									java.util.Calendar datePai = Calendar.getInstance();
									// TODO: Date date = Calendar.getInstance().getTime();
									DateFormat dateFormat = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
									String strDate_Pai = dateFormat.format(datePai.getTime());

									ReponseReglements reponseRegelemnt = preparerReglementLydec(listFactureLD,
											hist.getHatNautemt(), datePai, dmd, current_infoCommercant, folder, file);
									autorisationService.logMessage(file, "commande/reponseRegelemnt : "
											+ dmd.getCommande() + "/" + reponseRegelemnt.getMessage());

									if (reponseRegelemnt.getMessage() != null && !reponseRegelemnt.isOk()) {

										autorisationService.logMessage(file, "reponseRegelemnt KO ");
										autorisationService.logMessage(file, "Annulation auto LYDEC Start ... ");

										String repAnnu = annulationAuto(dmd, current_merchant, hist,model, folder, file);

										autorisationService.logMessage(file, "Annulation auto LYDEC end");
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
											// TODO: TODO: ee.printStackTrace();
										}
										autorisationService.logMessage(file,
												"Switch status annulation : [" + s_status + "]");

										if (repAnnu.equals("00")) {
											dmd.setEtatDemande("SW_ANNUL_AUTO");
											dmd.setDemCvv("");
											demandePaiementService.save(dmd);
											demandeDtoMsg.setMsgRefus(
													"La transaction en cours n’a pas abouti (Web service LYDEC Hors service), votre compte ne sera pas débité, merci de réessayer.");
											model.addAttribute("demandeDto", demandeDtoMsg);
											page = "operationAnnulee";
										} else {
											page = "error";
										}
										response.sendRedirect(failURL);

										autorisationService.logMessage(file, "Fin processRequest ()");
										logger.info("Fin processRequest ()");
										//return page;
										return null;
									} else {
										autorisationService.logMessage(file, "reponseRegelemnt OK ");
										for (FactureLDDto facLD : listFactureLD) {
											facLD.setEtat("O");
											facLD.setDatepai(strDate_Pai);
											facLD.setTrxFactureLydec(
													String.valueOf(reponseRegelemnt.getNumeroTransaction()));
											factureLDService.save(facLD);
											autorisationService.logMessage(file,
													"facLD commande/etat/numrecnaps/TrxFactureLydec : "
															+ facLD.getNumCommande() + "/" + facLD.getEtat() + "/"
															+ facLD.getNumrecnaps() + "/" + facLD.getTrxFactureLydec());
										}
										ResponseDto responseDto = new ResponseDto();
										responseDto.setLname(dmd.getNom());
										responseDto.setFname(dmd.getPrenom());
										responseDto.setOrderid(dmd.getCommande());
										responseDto.setAuthnumber(hist.getHatNautemt());
										responseDto.setAmount(dmd.getMontant());
										responseDto.setTransactionid(transactionid);
										responseDto.setMerchantid(dmd.getComid());
										responseDto.setEmail(dmd.getEmail());
										responseDto.setMerchantname(current_infoCommercant.getCmrNom());
										responseDto.setCardnumber(Util.formatCard(cardnumber));
										responseDto.setTransactiontime(dateFormat.format(new Date()));
										responseDto.setNumTransLydec(
												String.valueOf(reponseRegelemnt.getNumeroTransaction()));

										model.addAttribute("responseDto", responseDto);

										page = "recapLydec";
										autorisationService.logMessage(file, "Fin processRequest ()");
										logger.info("Fin processRequest ()");
										return page;
									}
								}
								// TODO: 2023-12-27 confirmation DGI
								if (dgiPreprod.equals(merchantid) || dgiProd.equals(merchantid)) {

									String resultcallback = envoyerConfirmation(dmd, response, hist.getHatNautemt(),
											folder, file);
									String resultFormat= "";
									if (!resultcallback.equals("")) {
										//JSONObject json = new JSONObject(resultcallback);
										//String msg = (String) json.get("msg");
										//String refReglement = (String) json.get("refReglement");
										//String codeRetour = (String) json.get("codeRetour");
										//String refcanal = (String) json.get("refcanal");
										String msg = "";
										String refReglement = "";
										String codeRetour = "";
										String refcanal = "";
										resultFormat = resultcallback.substring(1, resultcallback.length());
										JSONObject json = new JSONObject(resultFormat);
										// TODO: JSONObject json = new JSONObject(result);

										try {
											msg = (String) json.get("msg");
										} catch (Exception ex) {
											autorisationService.logMessage(file, "msg : " + Util.formatException(ex));
										}
										try {
											codeRetour = (String) json.get("codeRetour");
										} catch (Exception ex) {
											autorisationService.logMessage(file, "codeRetour : " + Util.formatException(ex));
										}
										try {
											refcanal = (String) json.get("refcanal");
										} catch (Exception ex) {
											autorisationService.logMessage(file, "refcanal : " + Util.formatException(ex));
										}
										try {
											refReglement = (String) json.get("refReglement");
										} catch (Exception ex) {
											autorisationService.logMessage(file, "refReglement : " + Util.formatException(ex));
										}

										// TODO: fin enregistrement des infos de retour WS de la DGI
										if (codeRetour.equals("000")) {
											autorisationService.logMessage(file,
													" ******** coreRetour 000 : Envoyer email au client ******** ");
											// TODO: pour envoyer un email au client
											envoyerEmail(dmd, response, folder, file);
											// TODO: envoyer le lien de recu au client
											autorisationService.logMessage(file,
													" ******** coreRetour 000 : envoyer le lien de recu au client ******** ");
											confirmerTrs(dmd, response, hist.getHatNautemt(), folder, file);

											ResponseDto responseDto = new ResponseDto();
											responseDto.setLname(dmd.getNom());
											responseDto.setFname(dmd.getPrenom());
											responseDto.setOrderid(dmd.getCommande());
											responseDto.setAuthnumber(hist.getHatNautemt());
											responseDto.setAmount(dmd.getMontant());
											responseDto.setTransactionid(transactionid);
											responseDto.setMerchantid(dmd.getComid());
											responseDto.setEmail(dmd.getEmail());
											responseDto.setMerchantname(current_infoCommercant.getCmrNom());
											responseDto.setCardnumber(Util.formatCard(cardnumber));
											responseDto.setTransactiontime(dateFormat.format(new Date()));

											model.addAttribute("responseDto", responseDto);

											page = "recapDGI";
											autorisationService.logMessage(file, "Fin processRequest ()");
											logger.info("Fin processRequest ()");
											return page;
										} else {
											autorisationService.logMessage(file, "Annulation auto DGI start ...");

											String repAnnu = annulationAuto(dmd, current_merchant, hist, model, folder, file);

											autorisationService.logMessage(file, "Annulation auto DGI end");
											s_status = "";
											try {
												CodeReponseDto codeReponseDto = codeReponseService
														.findByRpcCode(repAnnu);
												logger.info("codeReponseDto annulation : " + codeReponseDto);
												autorisationService.logMessage(file,
														"codeReponseDto annulation : " + codeReponseDto);
												if (codeReponseDto != null) {
													s_status = codeReponseDto.getRpcLibelle();
												}
											} catch (Exception ee) {
												autorisationService.logMessage(file,
														"Annulation auto 500 Error codeReponseDto null" + Util.formatException(ee));
												// TODO: TODO: ee.printStackTrace();
											}
											autorisationService.logMessage(file,
													"Switch status annulation : [" + s_status + "]");
											if (repAnnu.equals("00")) {
												dmd.setEtatDemande("SW_ANNUL_AUTO");
												demandePaiementService.save(dmd);
												demandeDtoMsg.setMsgRefus(
														"La transaction en cours n’a pas abouti (Web service DGI Hors service), votre compte ne sera pas débité, merci de réessayer.");
												model.addAttribute("demandeDto", demandeDtoMsg);
												page = "operationAnnulee";
											} else {
												page = "error";
											}
											response.sendRedirect(failURL);

											autorisationService.logMessage(file, "Fin processRequest ()");
											logger.info("Fin processRequest ()");
											//return page;
											return null;
										}
									}
									// TODO: fin confirmation DGI
								}
								// TODO: 2023-01-03 confirmation par Callback URL
								String resultcallback = "";
								String callbackURL = dmd.getCallbackURL();
								autorisationService.logMessage(file, "Call Back URL: " + callbackURL);
								if (dmd.getCallbackURL() != null && !dmd.getCallbackURL().equals("")
										&& !dmd.getCallbackURL().equals("NA")) {
									String clesigne = current_infoCommercant.getClePub();

									String montanttrx = String.format("%.2f", dmd.getMontant()).replace(",", ".");
									token_gen = dmd.getToken() == null ? "" : dmd.getToken();

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
										dmd = demandePaiementService.save(dmd);
									} else {
										if (repfailed) {
											autorisationService.logMessage(file,
													"Reponse recallURL KO => GATEFAILED");
											dmd.setRecallRep("N");
											dmd = demandePaiementService.save(dmd);
										} 
										//else {
											if (!dgiPreprod.equals(merchantid) && !dgiProd.equals(merchantid)) {
												autorisationService.logMessage(file, "Annulation auto start ...");

												String repAnnu = annulationAuto(dmd, current_merchant, hist, model, folder,
														file);

												autorisationService.logMessage(file, "Annulation auto end");
												s_status = "";
												try {
													CodeReponseDto codeReponseDto = codeReponseService
															.findByRpcCode(repAnnu);
													logger.info("codeReponseDto annulation : " + codeReponseDto);
													autorisationService.logMessage(file,
															"codeReponseDto annulation : " + codeReponseDto);
													if (codeReponseDto != null) {
														s_status = codeReponseDto.getRpcLibelle();
													}
												} catch (Exception ee) {
													autorisationService.logMessage(file,
															"Annulation auto 500 Error codeReponseDto null" + Util.formatException(ee));
													// TODO: TODO: ee.printStackTrace();
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
												
												autorisationService.logMessage(file, "Fin processRequest ()");
												logger.info("Fin processRequest ()");
												//return page;
												return null;
											}
										//}
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
									dmd = demandePaiementService.save(dmd);	
								} catch (Exception e) {
									dmd.setDemCvv("");
									demandePaiementService.save(dmd);
									autorisationService.logMessage(file,
											"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
													+ orderid + "]" + Util.formatException(e));
									autorisationService.logMessage(file, "Fin processRequest ()");
									logger.info("Fin processRequest ()");
									response.sendRedirect(failURL);
									return null;
								}
								autorisationService.logMessage(file,
										"update Demandepaiement status to SW_REJET OK.");
								// TODO: 2024-02-27
								try {
									if(hist.getId() == null) {
										// TODO: get histoauto check if exist
										HistoAutoGateDto histToAnnulle = histoAutoGateService.findByHatNumCommandeAndHatNumcmrV1(orderid, merchantid);
										if(histToAnnulle != null) {
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
											"payer 500 Error during HistoAutoGate findByHatNumCommandeAndHatNumcmrV1 orderid:[" + orderid
													+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
								}
								autorisationService.logMessage(file, "update HistoAutoGateDto etat to A OK.");
								// TODO: 2024-02-27
							}

							autorisationService.logMessage(file, "Generating paymentid...");

							String uuid_paymentid, paymentid = "";
							try {
								uuid_paymentid = String.format("%040d",	new BigInteger(UUID.randomUUID().toString().replace("-", ""), 36));
								paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
							} catch (Exception e) {
								autorisationService.logMessage(file,
										"authorization 500 Error during  paymentid generation for given orderid:[" + orderid + "]" + Util.formatException(e));
							}

							autorisationService.logMessage(file, "Generating paymentid OK");
							autorisationService.logMessage(file, "paymentid :[" + paymentid + "]");

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

							// TODO: reccurent transaction processing
							// first time transaction insert into rec
							if (is_first_trs && is_reccuring && coderep.equalsIgnoreCase("00")) {

								ReccuringTransactionDto rec_1 = new ReccuringTransactionDto();
								try {
									rec_1.setAmount(amount);
									rec_1.setAuthorizationNumber(authnumber);
									rec_1.setCardnumber(cardnumber);
									rec_1.setCountry(country);
									rec_1.setCurrency(currency);
									rec_1.setFirstTransaction("Y");
									rec_1.setFirstTransactionNumber(authnumber);
									rec_1.setMerchantid(merchantid);
									rec_1.setOrderid(orderid);
									rec_1.setPaymentid(transactionid);
									rec_1.setReccuringNumber(0);
									rec_1.setToken(token);
									rec_1.setTransactionid(transactionid);
									rec_1.setWebsiteid(websiteid.length() > 3 ? websiteid.substring(0,3) : websiteid);

									recService.save(rec_1);
									autorisationService.logMessage(file, "rec_1 " + rec_1.toString());
								} catch (Exception e) {
									autorisationService.logMessage(file,
											"authorization 500 Error during save in api_reccuring orderid:[" + orderid + "]" + Util.formatException(e));
								}
							}
							// TODO: reccurent insert and update
							if (!is_first_trs && is_reccuring && coderep.equalsIgnoreCase("00")) {

								ReccuringTransactionDto rec_1 = new ReccuringTransactionDto();
								try {
									rec_1.setAmount(amount);
									rec_1.setAuthorizationNumber(authnumber);
									rec_1.setCardnumber(cardnumber);
									rec_1.setCountry(country);
									rec_1.setCurrency(currency);
									rec_1.setFirstTransaction("N");
									rec_1.setFirstTransactionNumber(authnumber);
									rec_1.setMerchantid(merchantid);
									rec_1.setOrderid(orderid);
									rec_1.setPaymentid(paymentid);
									rec_1.setReccuringNumber(lrec_serie);
									rec_1.setToken(token);
									rec_1.setTransactionid(transactionid);
									rec_1.setWebsiteid(websiteid.length() > 3 ? websiteid.substring(0,3) : websiteid);

									recService.save(rec_1);
									autorisationService.logMessage(file, "rec_1 " + rec_1.toString());
								} catch (Exception e) {
									autorisationService.logMessage(file,
											"authorization 500 Error during save in api_reccuring orderid:[" + orderid + "]" + Util.formatException(e));
								}
							}

							try {
								String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
										+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
										+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
										+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid + "&token=" + token_gen;

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
											+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid + "&token=" + token_gen;

									autorisationService.logMessage(file, "data_noncrypt : " + data_noncrypt);
									// TODO : If the length is still greater than 200, reduce the length of email
									if (data_noncrypt.length() > 200 && !email.isEmpty()) {
										email = email.length() > 10 ? email.substring(0, 10) : email;
									}

									// TODO : Rebuild again with the updated email
									data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email="
											+ email + "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep
											+ "&numAuto=" + authnumber + "&numCarte=" + Util.formatCard(cardnumber)
											+ "&typecarte=" + dmd.getTypeCarte() + "&numTrans=" + transactionid + "&token=" + token_gen;

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

									response.sendRedirect(
											dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
									autorisationService.logMessage(file, "Fin processRequest ()");
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
									autorisationService.logMessage(file, "Fin processRequest ()");
									logger.info("Fin processRequest ()");
									return page;
								}
							} else {
								autorisationService.logMessage(file,
										"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
								response.sendRedirect(dmd.getFailURL());
								autorisationService.logMessage(file, "Fin processRequest ()");
								return  null;
								//return page;
							}
						} else if (reponseMPI.equals("C") || reponseMPI.equals("D")) {
							try {
								autorisationService.logMessage(file,
										"2eme chalenge apres auth acs => Redirect to failURL : " + dmd.getFailURL());
								response.sendRedirect(dmd.getFailURL());

								autorisationService.logMessage(file, "Fin processRequest ()");
								logger.info("Fin processRequest ()");

								return null;
							} catch (Exception ex) {
								autorisationService.logMessage(file,
										"authorization 500 Error during jso out processing " + Util.formatException(ex));
								demandeDtoMsg.setMsgRefus(
										"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer.");
								model.addAttribute("demandeDto", demandeDtoMsg);
								page = "result";
								autorisationService.logMessage(file, "Fin processRequest ()");
								logger.info("Fin processRequest ()");
								return page;
							}
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
						if(idDemande == null || idDemande.equals("")) {
							dmd = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());
						} else {
							dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));
						}

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
						autorisationService.logMessage(file, "Fin processRequest ()");
						logger.info("Fin processRequest ()");
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
					//return page;
					autorisationService.logMessage(file,
							"threeDsecureResponse null ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());
					DemandePaiementDto demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());
					String failToRedirect = "https://agent.naps.ma/RCB/FCB.html";
					if (demandeP != null) {
						if(demandeP.getFailURL() != null && !demandeP.getFailURL().isEmpty()) {
							failToRedirect = demandeP.getFailURL();
							autorisationService.logMessage(file,"demandeP existe failURL : " + failToRedirect);
						}
					}
					autorisationService.logMessage(file, "Fin processRequest ()");
					response.sendRedirect(failToRedirect);
					return null;
				}

			} else {
				autorisationService.logMessage(file,
						"ACSController RETOUR ACS =====> cleanCres TransStatus = " + cleanCres.getTransStatus());
				DemandePaiementDto demandeP = new DemandePaiementDto();
				autorisationService.logMessage(file,
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());

				demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());

				if (demandeP != null) {

					demandeP.setEtatDemande("RETOUR_ACS_NON_AUTH");
					demandePaiementService.save(demandeP);

					msgRefus = "";

					autorisationService.logMessage(file,
							"TransStatus != N && TransStatus != Y => Redirect to FailURL : " + demandeP.getFailURL());

					autorisationService.logMessage(file, "Fin processRequest ()");
					//return page;
					response.sendRedirect(demandeP.getFailURL());
					return null;
				} else {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
							+ "), votre compte ne sera pas débité, merci de réessayer.";
					demandeDtoMsg.setMsgRefus(msgRefus);
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					autorisationService.logMessage(file, "Fin processRequest ()");
					return page;
				}
			}
		} catch (Exception ex) {
			autorisationService.logMessage(file, "ACSController RETOUR ACS =====> Exception " + Util.formatException(ex));
			msgRefus = "La transaction en cours n’a pas abouti (TransStatus = " + cleanCres.getTransStatus()
					+ "), votre compte ne sera pas débité, merci de réessayer.";
			demandeDtoMsg.setMsgRefus(msgRefus);
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			autorisationService.logMessage(file, "Fin processRequest ()");
			return page;
		}
		autorisationService.logMessage(file, "Fin processRequest ()");

		return page;
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
		Date curren_date = new Date();
		int numTransaction = Util.generateNumTransaction(folder, file, curren_date);
		String numTrsStr = Util.formatNumTrans(String.valueOf(numTransaction));

		try {

			String currency = current_hist.getHatDevise();
			String expirydate = current_hist.getHatExpdate();
			String rrn = current_hist.getHatRrn();
			transactionnumber = rrn;
			tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
					.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
					.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame).withField(Tags.tag15, currency)
					.withField(Tags.tag18, numTrsStr).withField(Tags.tag42, expirydate).withField(Tags.tag16, date)
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

		autorisationService.logMessage(file, "Switch TLV Request :[" + Util.getTLVPCIDSS(tlv, folder, file) + "]");

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

		autorisationService.logMessage(file, "Switch TLV Respnose :[" + Util.getTLVPCIDSS(resp_tlv, folder, file) + "]");

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
			autorisationService.logMessage(file, "Switch  malfunction !!! Error during tlv Switch response parse switch" + Util.formatException(e));
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
				autorisationService.logMessage(file, "HAT_ID : " + current_hist.getId());
				if(current_hist.getId() == null) {
					// TODO: get histoauto check if exist
					HistoAutoGateDto histToAnnulle = histoAutoGateService.findLastByHatNumCommandeAndHatNumcmr(orderid, merchantid);
					if(histToAnnulle !=null) {
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
						"annulation auto 500 Error during HistoAutoGate findLastByHatNumCommandeAndHatNumcmr orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err2));
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

	public String redirectFailURL(DemandePaiementDto demandePaiementDto, String file) {

		autorisationService.logMessage(file,
				"REDIRECT FAIL URL DEMANDE PAIEMENT {" + demandePaiementDto.getIddemande() + "} => " + "Commerçant: {"
						+ demandePaiementDto.getComid() + "} Commande: {" + demandePaiementDto.getCommande() + "}");
		String signedFailUrl;
		String idCommande = demandePaiementDto.getCommande();
		InfoCommercantDto infoCommercantDto = infoCommercantService.findByCmrCode(demandePaiementDto.getComid());
		String md5Signature = Util.hachInMD5(idCommande + infoCommercantDto.getClePub());

		signedFailUrl = demandePaiementDto.getFailURL() + "?id_commande=" + demandePaiementDto.getCommande() + "&token="
				+ md5Signature;
		autorisationService.logMessage(file, "FAIL URL Signed : " + signedFailUrl);
		return signedFailUrl;
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
	
	@SuppressWarnings("all")
	public static String sendPOST(String urlcalback, String clepub, String idcommande, String repauto, String montant,
			String numAuto, Long numTrans, String token_gen, String pan_trame, String typecarte, String folder,
			String file) throws IOException {

		String result = "";
		HttpPost post = new HttpPost(urlcalback);

		String reqenvoi = idcommande + repauto + clepub + montant;
		String signature = Util.hachInMD5(reqenvoi);
		Util.writeInFileTransaction(folder, file, "Signature : " + signature);
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
				Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-KeyManagementException] sendPOST " + e1);
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
				Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 2");
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
					Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 3");
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
	public ReponseReglements preparerReglementLydec(List<FactureLDDto> listFactureLD, String num_auto,
			java.util.Calendar datePai, DemandePaiementDto demandePaiement, InfoCommercantDto infoCommercant,
			String folder, String file) throws IOException {
		autorisationService.logMessage(file, "Debut preparerReglementLydec");
		ReponseReglements reponseReglement = null;
		try {
			// TODO: java.util.Calendar date = Calendar.getInstance();
			DemandesReglements demReglement = new DemandesReglements();
			demReglement.setAgc_Cod((short) 840);
			BigDecimal b2 = new BigDecimal("-1");

			List<Impaye> factListImpayes = new ArrayList<Impaye>();
			BigDecimal montant = new BigDecimal(0);
			BigDecimal montantTimbre = new BigDecimal(0);
			// TODO: BigDecimal montantTotalSansTimbre = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal montantTotalSansTimbre = BigDecimal.valueOf(0).setScale(2, BigDecimal.ROUND_HALF_UP);
			for (FactureLDDto facLD : listFactureLD) {
				Impaye imp = new Impaye();
				imp.setNumeroFacture(Integer.valueOf(facLD.getNumfacture()));
				if (facLD.getNumligne() != null) {
					imp.setNumeroLigne(Integer.valueOf(facLD.getNumligne()));
				} else {
					imp.setNumeroLigne(0);
				}
				imp.setCodeFourniture(facLD.getFourniture());
				imp.setNumeroPolice(facLD.getNumPolice());
				imp.setProduit(Integer.valueOf(facLD.getProduit()));
				imp.setMontantTTC(BigDecimal.valueOf(facLD.getMontantTtc()).setScale(2, BigDecimal.ROUND_HALF_UP));
				imp.setMontantTimbre(BigDecimal.valueOf(facLD.getMontantTbr()).setScale(2, BigDecimal.ROUND_HALF_UP));
				imp.setMontantTVA(BigDecimal.valueOf(facLD.getMontantTva()).setScale(2, BigDecimal.ROUND_HALF_UP));
				montant = montant.add(BigDecimal.valueOf(facLD.getMontantTtc()).setScale(2, BigDecimal.ROUND_HALF_UP));
				montantTimbre = montantTimbre
						.add(BigDecimal.valueOf(facLD.getMontantTbr()).setScale(2, BigDecimal.ROUND_HALF_UP));
				autorisationService.logMessage(file, "preparerReglementLydec imp  : " + imp.toString());
				factListImpayes.add(imp);
			}
			autorisationService.logMessage(file,
					"preparerReglementLydec factListImpayes size : " + factListImpayes.size());

			montantTotalSansTimbre = montant.subtract(montantTimbre);

			Portefeuille[] listePortefeuilles = preparerTabEcritureLydecListe(factListImpayes);
			autorisationService.logMessage(file,
					"preparerReglementLydec listePortefeuilles size : " + listePortefeuilles.length);

			MoyenPayement[] listeMoyensPayement = new MoyenPayement[1];

			MoyenPayement ecr = new MoyenPayement();

			ecr.setType_Moy_Pai("C");
			ecr.setBanq_Cod("NPS");

			ecr.setDate_Pai(datePai);
			ecr.setMontant(montantTotalSansTimbre);
			ecr.setMoyen_Pai(num_auto);
			listeMoyensPayement[0] = ecr;
			autorisationService.logMessage(file,
					"preparerReglementLydec listeMoyensPayement[0] : " + listeMoyensPayement[0].toString());
			Transaction transaction = new Transaction();
			transaction.setNum_Trans(Integer.valueOf(listeMoyensPayement[0].getMoyen_Pai()));
			transaction.setAgc_Cod((short) 840);
			transaction.setDate_Trans(listeMoyensPayement[0].getDate_Pai());
			transaction.setDate_Val(new Date());
			transaction.setEtat_Trans("R");
			transaction.setType_Trans("RX");
			transaction.setGuichet_Cod((short) 3);
			transaction.setMatr(12345);
			transaction.setMt_Annule_Timbre(montantTimbre);
			transaction.setMt_Facture(BigDecimal.valueOf(0));
			transaction.setMt_Credite_Cred(BigDecimal.valueOf(0));
			transaction.setMt_Credite_Vers(BigDecimal.valueOf(0));
			transaction.setMt_Credite_Prov(BigDecimal.valueOf(0));
			transaction.setMt_Remb_Cheq(BigDecimal.valueOf(0));
			transaction.setMt_Od(BigDecimal.valueOf(0));
			transaction.setMt_Enc_Mp(montant.subtract(montantTimbre));
			transaction.setMt_Debite(montant.multiply(b2));
			transaction.setMt_Enc_Esp(BigDecimal.valueOf(0));
			transaction.setTr_Recu("");

			demReglement.setTransaction(transaction);
			demReglement.setListeMoyensPayement(listeMoyensPayement);
			demReglement.setListePortefeuilles(listePortefeuilles);

			// TODO: URL wsdlURL = GererEncaissementService.WSDL_LOCATION;
			URL wsdlURL = new URL(urlWsdlLydec);
			autorisationService.logMessage(file, "wsdlURL : " + wsdlURL);

			GererEncaissementService ss = new GererEncaissementService(wsdlURL, SERVICE_NAME);
			GererEncaissement port = ss.getGererEncaissement();
			autorisationService.logMessage(file, "preparerReglementLydec transaction : " + transaction.toString());
			autorisationService.logMessage(file,
					"preparerReglementLydec demReglement : " + demReglement.toString());

			reponseReglement = port.ecrireReglements(demReglement);

			if (reponseReglement != null) {
				logger.info("reponseReglement isOk/message : " + reponseReglement.isOk() + "/"
						+ reponseReglement.getMessage());
				autorisationService.logMessage(file, "reponseReglement isOk/message : " + reponseReglement.isOk()
						+ "/" + reponseReglement.getMessage());
			} else {
				logger.info("reponseReglement : " + null);
				autorisationService.logMessage(file, "reponseReglement : " + null);
			}

		} catch (Exception e) {
			autorisationService.logMessage(file, "preparerReglementLydec Exception =>" + e.getMessage());
			autorisationService.logMessage(file, "preparerReglementLydec Exception =>" + e.getStackTrace());
		}
		autorisationService.logMessage(file, "Fin preparerReglementLydec");
		return reponseReglement;
	}

	public Portefeuille[] preparerTabEcritureLydecListe(List<Impaye> facListe) {
		Portefeuille[] tabEcr = new Portefeuille[facListe.size()];
		int i = 0;
		for (Impaye fac : facListe) {
			Portefeuille ecr = new Portefeuille();
			ecr.setFac_Num(fac.getNumeroFacture());
			ecr.setLigne(fac.getNumeroLigne());
			tabEcr[i] = ecr;

			i++;

		}

		return tabEcr;
	}

	@SuppressWarnings("all")
	public String envoyerConfirmation(DemandePaiementDto demandePaiementDto, HttpServletResponse response,
			String numAuto, String folder, String file) throws IOException {

		autorisationService.logMessage(file,
				" *************************************** Debut envoyerConfirmation DGI *************************************** ");
		autorisationService.logMessage(file, "Commande : " + demandePaiementDto.getCommande());
		CFDGIDto cfDGI = new CFDGIDto();
		cfDGI = cfdgiService.findCFDGIByIddemande(demandePaiementDto.getIddemande());
		List<ArticleDGIDto> articles = articleDGIService
				.getArticlesByIddemandeSansFrais(demandePaiementDto.getIddemande());

		String num_taxe = cfDGI.getCF_R_OINReference();
		String montantTotal = String.valueOf(demandePaiementDto.getMontant());
		String montantTrans = cfDGI.getCF_R_OIMtTotal();
		String concatcreanceConfirmesIds = "";
		// TODO: concatcreanceConfirmesIds = la concaténation de la valeur 'UniqueID' des
		// TODO: balises <Article>
		// TODO: en excluant celle avec la valeur 111111111111 qui correspond au frais.
		// TODO: (cet
		// TODO: UniqueID est sur 13 caractère)
		for (ArticleDGIDto art : articles) {
			concatcreanceConfirmesIds = concatcreanceConfirmesIds + art.getUniqueID();
		}
		
		String dateTX;
		String creancier_id = concatcreanceConfirmesIds;
		String email = cfDGI.getCF_R_OIemail();
		dateTX = demandePaiementDto.getDateRetourSWT().replaceAll("\\s+", "").replace("-", "").replace(":", "");
		dateTX = dateTX.substring(0, Math.min(dateTX.length(), 14));
		String date_taxe = dateTX;
		String type_creance_id = cfDGI.getCF_R_OICodeOper(); // TODO: "03";
		String callbackURL = demandePaiementDto.getCallbackURL();

		String resultcallback = sendPOSTDGIInsert(callbackURL, montantTrans, montantTotal, creancier_id,
				type_creance_id, num_taxe, email, date_taxe, cfDGI, articles);

		autorisationService.logMessage(file,
				"envoyerConfirmation resultcallbackDGI : " + resultcallback.toString());
		String msg = "";
		String refReglement = "";
		String codeRetour = "";
		String refcanal = "";
		String resultFormat = "";
		if (!resultcallback.equals("")) {
			if (!resultcallback.equals("ko")) {				
				resultFormat = resultcallback.substring(1, resultcallback.length());
				JSONObject json = new JSONObject(resultFormat);
				// TODO: JSONObject json = new JSONObject(resultcallback);
				try {
					msg = (String) json.get("msg");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "envoyerConfirmation result 1 msg : " + Util.formatException(ex));
				}
				try {
					codeRetour = (String) json.get("codeRetour");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "envoyerConfirmation result 1 codeRetour : " + Util.formatException(ex));
				}
				try {
					refcanal = (String) json.get("refcanal");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "envoyerConfirmation result 1 refcanal : " + Util.formatException(ex));
				}
				try {
					refReglement = (String) json.get("refReglement");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "envoyerConfirmation result 1 refReglement : " + Util.formatException(ex));
				}
				autorisationService.logMessage(file,
						"envoyerConfirmation resultcallbackDGI => codeRetour/refReglement/msg/refcanal : " + codeRetour
								+ "/" + refReglement + "/" + msg + "/" + refcanal);
				// TODO: enregistrement des infos de retour WS de la DGI
				cfDGI.setRefReglement(refReglement);
				cfDGI.setCodeRtour(codeRetour);
				cfDGI.setMsg(msg);
				cfDGI.setRefcanal(refcanal);
				cfdgiService.save(cfDGI);
				autorisationService.logMessage(file, "update cfDGI apres retour WS de la DGI : " + cfDGI.toString());

				// TODO: fin enregistrement des infos de retour WS de la DGI

			}


		}
		autorisationService.logMessage(file,
				" *************************************** End envoyerConfirmation DGI *************************************** ");

		return resultcallback;
	}

	@SuppressWarnings("all")
	public String sendPOSTDGIInsert(String urlcalback, String montant, String montantTotal, String creancier_id,
			String type_creance_id, String num_taxe, String email, String date_taxe, CFDGIDto cfDGI,
			List<ArticleDGIDto> articles) throws IOException {
		autorisationService.logMessage(file,
				" *************************************** Debut sendPOSTDGIInsert DGI *************************************** ");
		String list_taxe = new Gson().toJson(articles);
		String cf = new Gson().toJson(cfDGI);
		String result = "";
		HttpPost post = new HttpPost(urlcalback);
		String msg = "";
		String refReglement = "";
		String codeRetour = "";
		String refcanal = "";
		String resultFormat = "";

		// TODO: add request parameters or form parameters
		List<NameValuePair> urlParameters = new ArrayList<>();

		urlParameters.add(new BasicNameValuePair("montantTotal", montantTotal));
		urlParameters.add(new BasicNameValuePair("montant", montant));
		urlParameters.add(new BasicNameValuePair("creancier_id", creancier_id));
		urlParameters.add(new BasicNameValuePair("type_creance_id", type_creance_id));
		urlParameters.add(new BasicNameValuePair("num_taxe", num_taxe));
		urlParameters.add(new BasicNameValuePair("email", email));
		urlParameters.add(new BasicNameValuePair("date_taxe", date_taxe));
		urlParameters.add(new BasicNameValuePair("list_taxe", list_taxe));
		urlParameters.add(new BasicNameValuePair("cf", cf));
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		autorisationService.logMessage(file,
				"sendPOSTDGIInsert commande / urlParameters :" + cfDGI.getCF_R_OINReference() + " / " + urlParameters);
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();

			try {
				httpClient = (CloseableHttpClient) getAllSSLClient();
			} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
				autorisationService.logMessage(file,
						"[GW-EXCEPTION-KeyManagementException] sendPOSTDGIInsert " + e1);
			}

			CloseableHttpResponse response = httpClient.execute(post);

			result = EntityUtils.toString(response.getEntity());
			autorisationService.logMessage(file, "sendPOSTDGIInsert result 1 : " + result);

		} catch (Exception ex) {
			result = "ko";
			autorisationService.logMessage(file, "sendPOSTDGIInsert result 1 : " + result + Util.formatException(ex));
		}
		if (!result.equals("ko")) {
			resultFormat = result.substring(1, result.length());
			JSONObject json = new JSONObject(resultFormat);
			// TODO: JSONObject json = new JSONObject(result);

			try {
				msg = (String) json.get("msg");
			} catch (Exception ex) {
				autorisationService.logMessage(file, "sendPOSTDGIInsert result 1 msg : " + Util.formatException(ex));
			}
			try {
				codeRetour = (String) json.get("codeRetour");
			} catch (Exception ex) {
				autorisationService.logMessage(file, "sendPOSTDGIInsert result 1 codeRetour : " + Util.formatException(ex));
			}
			try {
				refcanal = (String) json.get("refcanal");
			} catch (Exception ex) {
				autorisationService.logMessage(file, "sendPOSTDGIInsert result 1 refcanal : " + Util.formatException(ex));
			}
			try {
				refReglement = (String) json.get("refReglement");
			} catch (Exception ex) {
				autorisationService.logMessage(file, "sendPOSTDGIInsert result 1 refReglement : " + Util.formatException(ex));
			}
			autorisationService.logMessage(file,
					"sendPOSTDGIInsert resultcallbackDGI => codeRetour/refReglement/msg/refcanal : " + codeRetour + "/"
							+ refReglement + "/" + msg + "/" + refcanal);
		}
		if (!codeRetour.equals("000")) {
			try {
				Thread.sleep(10000);

				// TODO: tentative 2 apès 10 s
				autorisationService.logMessage(file, "sendPOSTDGIInsert tentative 2 apès 10 s: ");
				try {
					CloseableHttpClient httpClient = HttpClients.createDefault();

					try {
						httpClient = (CloseableHttpClient) getAllSSLClient();
					} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
						autorisationService.logMessage(file,
								"[GW-EXCEPTION-KeyManagementException] sendPOSTDGIInsert " + e1);
					}

					CloseableHttpResponse response = httpClient.execute(post);

					result = EntityUtils.toString(response.getEntity());
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 : " + result);

				} catch (Exception ex) {
					result = "ko";
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 : " + result + Util.formatException(ex));
				}
			} catch (Exception e) {
				result = "ko";
				autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 : " + result + Util.formatException(e));
			}
			if (!result.equals("ko")) {
				resultFormat = result.substring(1, result.length());
				JSONObject json = new JSONObject(resultFormat);
				// TODO: JSONObject json = new JSONObject(result);
				try {
					msg = (String) json.get("msg");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 msg : " + Util.formatException(ex));
				}
				try {
					codeRetour = (String) json.get("codeRetour");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 codeRetour : " + Util.formatException(ex));
				}
				try {
					refcanal = (String) json.get("refcanal");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 refcanal : " + Util.formatException(ex));
				}
				try {
					refReglement = (String) json.get("refReglement");
				} catch (Exception ex) {
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 2 refReglement : " + Util.formatException(ex));
				}
				autorisationService.logMessage(file,
						"sendPOSTDGIInsert resultcallbackDGI => codeRetour/refReglement/msg/refcanal : " + codeRetour
								+ "/" + refReglement + "/" + msg + "/" + refcanal);
			}

			if (!codeRetour.equals("000")) {
				try {
					Thread.sleep(10000);

					// TODO: tentative 3 apès 10 s
					autorisationService.logMessage(file, "sendPOSTDGIInsert tentative 3 apès 10 s: ");
					try {
						CloseableHttpClient httpClient = HttpClients.createDefault();

						try {
							httpClient = (CloseableHttpClient) getAllSSLClient();
						} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
							autorisationService.logMessage(file,
									"[GW-EXCEPTION-KeyManagementException] sendPOSTDGIInsert " + e1);
						}

						CloseableHttpResponse response = httpClient.execute(post);

						result = EntityUtils.toString(response.getEntity());
						autorisationService.logMessage(file, "sendPOSTDGIInsert result 3 : " + result);

					} catch (Exception ex) {
						result = "ko";
						autorisationService.logMessage(file, "sendPOSTDGIInsert result 3 : " + result + Util.formatException(ex));
					}
				} catch (Exception e) {
					result = "ko";
					autorisationService.logMessage(file, "sendPOSTDGIInsert result 3 : " + result + Util.formatException(e));
				}
			}
		}
		if (result.equals("ko")) {
			result = "{\"msg\":\"GATEFAILED\",\"refReglement\":\"\",\"codeRetour\":\"\",\"refcanal\":\"\"}";
		}
		autorisationService.logMessage(file,
				" *************************************** End sendPOSTDGIInsert DGI *************************************** ");
		return result;
	}

	@SuppressWarnings("all")
	public void confirmerTrs(DemandePaiementDto demandePaiementDto, HttpServletResponse response, String numAuto,
			String folder, String file) throws IOException {
		autorisationService.logMessage(file,
				" *************************************** Debut confirmerTrs DGI *************************************** ");
		// TODO: Fields DGI
		String link;
		String ref;
		String idService;
		String idTxMTC;
		String statut;
		String retourWSKey5 = "";
		String sec;
		String idTxSysPmt;
		String dateTX;
		int idsysPmt;
		try {

			autorisationService.logMessage(file, "Commande : " + demandePaiementDto.getCommande());
			ArticleDGIDto artdgi = new ArticleDGIDto();
			CFDGIDto cfDGI = new CFDGIDto();
			artdgi = articleDGIService.findVraiArticleByIddemande(demandePaiementDto.getIddemande());
			// TODO: retourWSKey5 = "A7D87E2HQ185BA70EBPXA017A325D777" ;
			// TODO: clé retour ws : preprod dgi
			if (dgiPreprod.equals(demandePaiementDto.getComid())) {
				retourWSKey5 = "A7D87E2HQ185BA70EBPXA017A325D777";
			}
			// TODO: clé retour ws : prod dgi
			if (dgiProd.equals(demandePaiementDto.getComid())) {
				retourWSKey5 = "543D523A710AXPBE07AB581QH2E78D8R";
			}
			cfDGI = cfdgiService.findCFDGIByIddemande(demandePaiementDto.getIddemande());
			idTxSysPmt = artdgi.getUniqueID();
			link = cfDGI.getCF_R_OIConfirmUrl();
			ref = cfDGI.getCF_R_OINReference();
			idService = cfDGI.getCF_R_OICodeOper();
			Double montant = demandePaiementDto.getMontant();
			idsysPmt = 302; // TODO: par defaut 100 selon les spec
			dateTX = demandePaiementDto.getDateRetourSWT().replaceAll("\\s+", "").replace("-", "").replaceAll(":",
					"");
			dateTX = dateTX.substring(0, Math.min(dateTX.length(), 14));
			idTxMTC = numAuto; // TODO: autorisation
			statut = "C";

			sec = Util.hachInMD5(ref + idTxSysPmt + dateTX + montant + statut + idsysPmt + idService + retourWSKey5);

			String linkDGI = link + "&ref=" + ref + "&idService=" + idService + "&statut=" + statut + "&montant="
					+ montant + "&idsysPmt=" + idsysPmt + "&idTxSysPmt=" + idTxSysPmt + "&IdTxMTC=" + idTxMTC
					+ "&dateTX=" + dateTX + "&Sec=" + sec;

			autorisationService.logMessage(file, "linkDGI : " + linkDGI);

			response.sendRedirect(linkDGI);

			return;
		} catch (Exception e) {

			autorisationService.logMessage(file, "[GW-EXCEPTION-CONFIRMERTRS] " + Util.formatException(e));

		}
		autorisationService.logMessage(file,
				" *************************************** End confirmerTrs DGI *************************************** ");
	}

	@SuppressWarnings("all")
	public void envoyerEmail(DemandePaiementDto demandePaiementDto, HttpServletResponse response, String folder,
			String file) throws IOException {

		autorisationService.logMessage(file,
				" *************************************** Debut envoie email au client DGI *************************************** ");

		Gson gson = new Gson();

		String urlSendEmailDGI = lienEnvoieEmailDgi;
		autorisationService.logMessage(file, "envoie email au client =====> urlSendEmailDGI : " + urlSendEmailDGI);

		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			httpClient = getAllSSLClient();
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
			autorisationService.logMessage(file,
					"[GW-EXCEPTION-KeyManagementException] RecapDGI envoyerEmail  " + e1);
		}

		HttpPost httpPost = new HttpPost(urlSendEmailDGI);

		RequestEnvoieEmail requestEnvoieEmail = new RequestEnvoieEmail();

		requestEnvoieEmail.setIdDemande(demandePaiementDto.getIddemande());
		requestEnvoieEmail.setIdCommande(demandePaiementDto.getCommande());
		requestEnvoieEmail.setNumCmr(demandePaiementDto.getComid());
		autorisationService.logMessage(file,
				"envoie email au client =====> requestEnvoieEmail : " + requestEnvoieEmail);

		final String jsonBody = gson.toJson(requestEnvoieEmail);

		autorisationService.logMessage(file, "envoie email au client =====> jsonBody : " + jsonBody);

		final StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {
			HttpResponse responseTheeDs = httpClient.execute(httpPost);

			StatusLine responseStatusLine = responseTheeDs.getStatusLine();
			autorisationService.logMessage(file, "RecapDGI envoyerEmail =====> RETOUR API response StatusCode : "
					+ responseTheeDs.getStatusLine().getStatusCode());
			autorisationService.logMessage(file,
					"RecapDGI envoyerEmail =====> RETOUR API responseStatusLine : " + responseStatusLine);
			String respStr = EntityUtils.toString(responseTheeDs.getEntity());

			autorisationService.logMessage(file, "RecapDGI envoyerEmail =====> RETOUR API respStr : " + respStr);

		} catch (Exception e) {

			autorisationService.logMessage(file, "[GW-EXCEPTION-ENVOYEREMAIL] " + Util.formatException(e));

		}
		autorisationService.logMessage(file,
				" *************************************** End envoie email au client DGI *************************************** ");
	}

	@SuppressWarnings("all")
	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			@SuppressWarnings({"squid:S4830", "Depreciated"}) // TODO: Suppression intentionnelle : La validation des certificats est désactivée par choix
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"}) // TODO: Suppression intentionnelle : La validation des certificats est désactivée par choix
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
