package ma.m2m.gateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
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
import javax.xml.namespace.QName;

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
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.dto.ArticleDGIDto;
import ma.m2m.gateway.dto.CFDGIDto;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.FactureLDDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.dto.responseDto;
import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.lydec.DemandesReglements;
import ma.m2m.gateway.lydec.GererEncaissement;
import ma.m2m.gateway.lydec.GererEncaissementService;
import ma.m2m.gateway.lydec.Impaye;
import ma.m2m.gateway.lydec.MoyenPayement;
import ma.m2m.gateway.lydec.Portefeuille;
import ma.m2m.gateway.lydec.ReponseReglements;
import ma.m2m.gateway.lydec.Transaction;
import ma.m2m.gateway.service.ArticleDGIService;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CFDGIService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CodeReponseService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
import ma.m2m.gateway.service.FactureLDService;
import ma.m2m.gateway.service.HistoAutoGateService;
import ma.m2m.gateway.service.InfoCommercantService;
import ma.m2m.gateway.service.TelecollecteService;
import ma.m2m.gateway.service.TransactionService;
import ma.m2m.gateway.switching.SwitchTCPClient;
import ma.m2m.gateway.switching.SwitchTCPClientV2;
import ma.m2m.gateway.threedsecure.CRes;
import ma.m2m.gateway.threedsecure.RequestEnvoieEmail;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.tlv.TLVEncoder;
import ma.m2m.gateway.tlv.TLVParser;
import ma.m2m.gateway.tlv.Tags;
import static ma.m2m.gateway.encryption.HashingHelper.hachInMD5;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Controller
public class ACSController {

	// private Traces traces = new Traces();
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

	@Value("${key.LINK_CHALENGE}")
	private String link_chalenge;

	@Value("${key.LINK_RESULT}")
	private String link_result;

	@Value("${key.LYDEC_PREPROD}")
	private String LYDEC_PREPROD;

	@Value("${key.LYDEC_PROD}")
	private String LYDEC_PROD;

	@Value("${key.URL_WSDL_LYDEC}")
	private String URL_WSDL_LYDEC;

	@Value("${key.DGI_PREPROD}")
	private String DGI_PREPROD;

	@Value("${key.DGI_PROD}")
	private String DGI_PROD;

	@Value("${key.LIEN_ENVOIE_EMAIL_DGI}")
	private String LIEN_ENVOIE_EMAIL_DGI;

	@Autowired
	private DemandePaiementService demandePaiementService;

	@Autowired
	AutorisationService autorisationService;

	@Autowired
	private InfoCommercantService infoCommercantService;

	@Autowired
	CommercantService commercantService;

	@Autowired
	HistoAutoGateService histoAutoGateService;

	@Autowired
	TransactionService transactionService;

	@Autowired
	TelecollecteService telecollecteService;

	@Autowired
	CardtokenService cardtokenService;

	@Autowired
	CodeReponseService codeReponseService;

	@Autowired
	FactureLDService factureLDService;

	@Autowired
	ArticleDGIService articleDGIService;

	@Autowired
	CFDGIService cfdgiService;

	// Fields DGI
	private String link, ref, idService, IdTxMTC, statut, retourWSKey5, Sec;
	private String idTxSysPmt;
	private String dateTX;
	private int idsysPmt;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd");

	private InfoCommercantDto infoCommercantDto = new InfoCommercantDto();

	private static final QName SERVICE_NAME = new QName("http://service.lydec.com", "GererEncaissementService");

	public ACSController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "R_ACS_" + randomWithSplittableRandom;
		// date of folder logs
		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	@PostMapping("/napspayment/acs")
	public String processRequest(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "R_ACS_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "Start processRequest ()");
		System.out.println("Start processRequest ()");
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

			// just for test
			// cleanCres.setTransStatus("N");

			if (cleanCres.getTransStatus().equals("Y")) {
				System.out.println("ACSController RETOUR ACS =====> cleanCres TransStatus = Y ");
				Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> cleanCres TransStatus = Y ");

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
				String natbin = "";
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
				//
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
					Util.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
					Util.writeInFileTransaction(folder, file, "demandePaiement after update MPI_KO idDemande null");
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (MPI_KO), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

				if (dmd == null) {
					Util.writeInFileTransaction(folder, file,
							"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
									+ idDemande);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (DemandePaiement introuvable), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				// Merchnat info
				merchantid = dmd.getComid();
				websiteid = dmd.getGalid();

				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

				Util.writeInFileTransaction(folder, file, "authorization_" + orderid + timeStamp);

				CommercantDto current_merchant = null;
				try {
					current_merchant = commercantService.findByCmrNumcmr(merchantid);
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				if (current_merchant == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				if (current_merchant.getCmrCodactivite() == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				if (current_merchant.getCmrCodbqe() == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}
				InfoCommercantDto current_infoCommercant = null;

				try {
					current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 InfoCommercant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);
					demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				if (current_infoCommercant == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 InfoCommercantDto misconfigured in DB or not existing orderid:["
									+ orderid + "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid
									+ "]");
					demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
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
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during  date formatting for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err2);
					demandeDtoMsg.setMsgRefus("Erreur lors du formatage de la date");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				if (reponseMPI.equals("") || reponseMPI == null) {
					dmd.setEtat_demande("MPI_KO");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file,
							"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
					Util.writeInFileTransaction(folder, file, "Response 3DS is null");
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (MPI_KO reponseMPI null), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}

				if (reponseMPI.equals("Y")) {
					// ********************* Frictionless responseMPI equal Y *********************
					Util.writeInFileTransaction(folder, file,
							"********************* responseMPI equal Y *********************");

					dmd.setDem_xid(threeDSServerTransID);
					dmd.setEtat_demande("RETOUR_ACS_AUTH_OK");
					demandePaiementService.save(dmd);

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
						demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

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
						Util.writeInFileTransaction(folder, file,
								"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (cvv doit être présent dans la transaction normale), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					// not reccuring , normal
					if (cvv_present && !is_reccuring) {
						Util.writeInFileTransaction(folder, file,
								"not reccuring , normal cvv_present && !is_reccuring");
						try {

							tlv = new TLVEncoder().withField(Tags.tag0, mesg_type).withField(Tags.tag1, cardnumber)
									.withField(Tags.tag3, processing_code).withField(Tags.tag22, transaction_condition)
									.withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
									.withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
									.withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
									.withField(Tags.tag16, date).withField(Tags.tag17, heure)
									.withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" + merchantid)
									.withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn)
									.withField(Tags.tag67, cvv).withField(Tags.tag11, merchant_name)
									.withField(Tags.tag12, merchant_city).withField(Tags.tag90, acqcode)
									.withField(Tags.tag167, champ_cavv).withField(Tags.tag168, xid).encode();

							Util.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
							Util.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
							Util.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
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
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Erreur lors de la création du switch tlv), votre compte ne sera pas débité, merci de réessayer .");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
							System.out.println("Fin processRequest ()");
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
//					SwitchTCPClient sw = SwitchTCPClient.getInstance();
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
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error Switch communication s_conn false switch ip:[" + sw_s
											+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
							demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
							System.out.println("Fin processRequest ()");
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
						Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);
						switch_ko = 1;
						demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					} catch (java.net.ConnectException e) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
						switch_ko = 1;
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Un dysfonctionnement du switch), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					catch (SocketTimeoutException e) {
						Util.writeInFileTransaction(folder, file,
								"Switch  malfunction  SocketTimeoutException !!!" + e);
						switch_ko = 1;
						e.printStackTrace();
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch communication SocketTimeoutException" + "switch ip:["
										+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur de communication du switch SocketTimeoutException), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					catch (IOException e) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
						switch_ko = 1;
						e.printStackTrace();
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch communication IOException" + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur de communication du switch IOException), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					catch (Exception e) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
						switch_ko = 1;
						e.printStackTrace();
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Dysfonctionnement du switch Exception), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					String resp = resp_tlv;

					// resp debug
					// resp =
					// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

					if (switch_ko == 0 && resp == null) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
						switch_ko = 1;
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch null response" + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp null), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					if (switch_ko == 0 && resp.length() < 3) {
						switch_ko = 1;

						Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch short response length() < 3 " + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp < 3 !!!), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					Util.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

					Util.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

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
									"authorization 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
											+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer .");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
							System.out.println("Fin processRequest ()");
							return page;
						}

						// controle switch
						if (tag1_resp == null) {
							Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
							switch_ko = 1;
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
											+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
											+ resp_tlv + "]");
						}

						if (tag1_resp != null && tag1_resp.length() < 3) {
							Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
							switch_ko = 1;
							Util.writeInFileTransaction(folder, file, "authorization 500"
									+ "Error during tlv Switch response parse tag1_resp length tag  < 3" + "switch ip:["
									+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						}

						if (tag20_resp == null) {
							Util.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
							switch_ko = 1;
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
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

					// SWHistoAutoDto swhist = null;

					if (switch_ko == 1) {
						pan_auto = Util.formatagePan(cardnumber);
						Util.writeInFileTransaction(folder, file,
								"getSWHistoAuto pan_auto/rrn/amount/date/merchantid : " + pan_auto + "/" + rrn + "/"
										+ amount + "/" + date + "/" + merchantid);
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

						Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

						Util.writeInFileTransaction(folder, file, "get max id ...");

						// Ihist_id = hist.getMAX_ID("HISTOAUTO_GATE", "HAT_ID");
						// Ihist_id = histoAutoGateService.getMAX_ID();
						// long currentid = Ihist_id.longValue() + 1;
						// hist.setId(currentid);

						Util.writeInFileTransaction(folder, file, "max id : [" + Ihist_id + "]");

						Util.writeInFileTransaction(folder, file, "formatting pan...");

						pan_auto = Util.formatagePan(cardnumber);
						Util.writeInFileTransaction(folder, file, "formatting pan Ok pan_auto :[" + pan_auto + "]");

						Util.writeInFileTransaction(folder, file, "HistoAutoGate data filling start ...");

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

					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error during  insert in histoautogate for given orderid:[" + orderid
										+ "]" + e);
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
										Util.writeInFileTransaction(folder, file, "n_tlc !null ");

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
									Util.writeInFileTransaction(folder, file, "inserting into telec ok");
									capture_status = "Y";

								} catch (Exception e) {
									exp_flag = 1;
									Util.writeInFileTransaction(folder, file,
											"inserting into telec ko..do nothing " + e);
								}

							}
							if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
								capture_status.equalsIgnoreCase("N");

							Util.writeInFileTransaction(folder, file, "Automatic capture end.");
						}
						// 2023-11-27 preparation reconciliation Ecom Lydec
						if (LYDEC_PREPROD.equals(merchantid) || LYDEC_PROD.equals(merchantid)) {
							List<FactureLDDto> listFactureLD = new ArrayList<>();
							listFactureLD = factureLDService.findFactureByIddemande(dmd.getIddemande());
							java.util.Calendar date_pai = Calendar.getInstance();
							// Date date = Calendar.getInstance().getTime();
							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String strDate_Pai = dateFormat.format(date_pai.getTime());

							ReponseReglements reponseRegelemnt = preparerReglementLydec(listFactureLD,
									hist.getHatNautemt(), date_pai, dmd, current_infoCommercant, folder, file);
							Util.writeInFileTransaction(folder, file, "commande/reponseRegelemnt : " + dmd.getCommande()
									+ "/" + reponseRegelemnt.getMessage());

							if (reponseRegelemnt.getMessage() != null && !reponseRegelemnt.isOk()) {
								
								Util.writeInFileTransaction(folder, file, "reponseRegelemnt KO ");
								Util.writeInFileTransaction(folder, file, "Annulation auto LYDEC Start ... ");
								
								String repAnnu = AnnulationAuto(dmd, current_merchant, hist, folder, file);
								
								Util.writeInFileTransaction(folder, file, "Annulation auto LYDEC end");
								s_status = "";
								try {
									CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(repAnnu);
									System.out.println("codeReponseDto annulation : " + codeReponseDto);
									Util.writeInFileTransaction(folder, file,
											"codeReponseDto annulation : " + codeReponseDto);
									if (codeReponseDto != null) {
										s_status = codeReponseDto.getRpcLibelle();
									}
								} catch (Exception ee) {
									Util.writeInFileTransaction(folder, file,
											"Annulation auto 500 Error codeReponseDto null");
									ee.printStackTrace();
								}
								Util.writeInFileTransaction(folder, file,
										"Switch status annulation : [" + s_status + "]");
								
								if (repAnnu.equals("00")) {
									dmd.setEtat_demande("SW_ANNUL_AUTO");
									demandePaiementService.save(dmd);
									demandeDtoMsg.setMsgRefus(
											"La transaction en cours n’a pas abouti (Web service LYDEC Hors service), votre compte ne sera pas débité, merci de réessayer .");
									model.addAttribute("demandeDto", demandeDtoMsg);
									page = "operationAnnulee";
								} else {
									page = "error";
								}
								Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
								System.out.println("Fin processRequest ()");
								return page;
							} else {
								Util.writeInFileTransaction(folder, file, "reponseRegelemnt OK ");
								for (FactureLDDto facLD : listFactureLD) {
									facLD.setEtat("O");
									facLD.setDatepai(strDate_Pai);
									facLD.setTrxFactureLydec(String.valueOf(reponseRegelemnt.getNumeroTransaction()));
									factureLDService.save(facLD);
									Util.writeInFileTransaction(folder, file,
											"facLD commande/etat/numrecnaps/TrxFactureLydec : " + facLD.getNumCommande()
													+ "/" + facLD.getEtat() + "/" + facLD.getNumrecnaps() + "/"
													+ facLD.getTrxFactureLydec());
								}
								responseDto responseDto = new responseDto();
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
								responseDto.setNumTransLydec(String.valueOf(reponseRegelemnt.getNumeroTransaction()));

								model.addAttribute("responseDto", responseDto);

								page = "recapLydec";
								Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
								System.out.println("Fin processRequest ()");
								return page;
							}
						}
						// 2023-12-27 confirmation DGI
						if (DGI_PREPROD.equals(merchantid) || DGI_PROD.equals(merchantid)) {

							String resultcallback = envoyerConfirmation(dmd, response, hist.getHatNautemt(), folder,
									file);
							if (!resultcallback.equals("")) {
								JSONObject json = new JSONObject(resultcallback);
								String msg = (String) json.get("msg");
								String refReglement = (String) json.get("refReglement");
								String codeRetour = (String) json.get("codeRetour");
								String refcanal = (String) json.get("refcanal");

								// fin enregistrement des infos de retour WS de la DGI
								if (codeRetour.equals("000")) {
									Util.writeInFileTransaction(folder, file,
											" ******** coreRetour 000 : Envoyer email au client ******** ");
									// pour envoyer un email au client
									envoyerEmail(dmd, response, folder, file);
									// envoyer le lien de recu au client
									Util.writeInFileTransaction(folder, file,
											" ******** coreRetour 000 : envoyer le lien de recu au client ******** ");
									confirmerTrs(dmd, response, hist.getHatNautemt(), folder, file);

									responseDto responseDto = new responseDto();
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
									Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
									System.out.println("Fin processRequest ()");
									return page;
								} else {
									Util.writeInFileTransaction(folder, file, "Annulation auto DGI start ...");

									String repAnnu = AnnulationAuto(dmd, current_merchant, hist, folder, file);
									
									Util.writeInFileTransaction(folder, file, "Annulation auto DGI end");
									s_status = "";
									try {
										CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(repAnnu);
										System.out.println("codeReponseDto annulation : " + codeReponseDto);
										Util.writeInFileTransaction(folder, file,
												"codeReponseDto annulation : " + codeReponseDto);
										if (codeReponseDto != null) {
											s_status = codeReponseDto.getRpcLibelle();
										}
									} catch (Exception ee) {
										Util.writeInFileTransaction(folder, file,
												"Annulation auto 500 Error codeReponseDto null");
										ee.printStackTrace();
									}
									Util.writeInFileTransaction(folder, file,
											"Switch status annulation : [" + s_status + "]");
									if (repAnnu.equals("00")) {
										dmd.setEtat_demande("SW_ANNUL_AUTO");
										demandePaiementService.save(dmd);
										demandeDtoMsg.setMsgRefus(
												"La transaction en cours n’a pas abouti (Web service DGI Hors service), votre compte ne sera pas débité, merci de réessayer .");
										model.addAttribute("demandeDto", demandeDtoMsg);
										page = "operationAnnulee";
									} else {
										page = "error";
									}

									Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
									System.out.println("Fin processRequest ()");
									return page;
								}
							}
							// fin confirmation DGI
						}
						// 2023-01-03 confirmation par Callback URL
						String resultcallback = "";
						String callbackURL = dmd.getCallbackURL();
						Util.writeInFileTransaction(folder, file, "Call Back URL: " + callbackURL);
						if(dmd.getCallbackURL() != null && !dmd.getCallbackURL().equals("") && dmd.getCallbackURL().equals("NA")) {
							String clesigne = infoCommercantDto.getClePub();

							String montanttrx = String.format("%.2f", dmd.getMontant()).replaceAll(",", ".");
							String token_gen = "";
							
							Util.writeInFileTransaction(folder, file,
									"sendPOST(" + callbackURL + "," + clesigne + "," + dmd.getCommande() + ","
											+ tag20_resp + "," + montanttrx + "," + hist.getHatNautemt() + ","
											+ hist.getHatNumdem() + ")");
							
							resultcallback = sendPOST(callbackURL, clesigne, dmd.getCommande(), tag20_resp,
									montanttrx, hist.getHatNautemt(), hist.getHatNumdem(), token_gen,
									Util.formatCard(cardnumber), dmd.getType_carte(), folder, file);
							
							Util.writeInFileTransaction(folder, file, "resultcallback :[+" + resultcallback + "]");

							boolean repsucces = resultcallback.indexOf("GATESUCCESS") != -1 ? true : false;

							boolean repfailed = resultcallback.indexOf("GATEFAILED") != -1 ? true : false;

							Util.writeInFileTransaction(folder, file, "repsucces : " + repsucces);
							Util.writeInFileTransaction(folder, file, "repfailed : " + repfailed);
							if (repsucces) {
								Util.writeInFileTransaction(folder, file,
										"Reponse recallURL OK => GATESUCCESS");
								dmd.setRecallRep("Y");
								demandePaiementService.save(dmd);
							} else {
								if (repfailed) {
									Util.writeInFileTransaction(folder, file,
											"Reponse recallURL KO => GATEFAILED");
									dmd.setRecallRep("N");
									demandePaiementService.save(dmd);
								} else {
									if (!DGI_PREPROD.equals(merchantid) && !DGI_PROD.equals(merchantid)) {
										Util.writeInFileTransaction(folder, file, "Annulation auto start ...");

										String repAnnu = AnnulationAuto(dmd, current_merchant, hist, folder, file);
										
										Util.writeInFileTransaction(folder, file, "Annulation auto end");
										s_status = "";
										try {
											CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(repAnnu);
											System.out.println("codeReponseDto annulation : " + codeReponseDto);
											Util.writeInFileTransaction(folder, file,
													"codeReponseDto annulation : " + codeReponseDto);
											if (codeReponseDto != null) {
												s_status = codeReponseDto.getRpcLibelle();
											}
										} catch (Exception ee) {
											Util.writeInFileTransaction(folder, file,
													"Annulation auto 500 Error codeReponseDto null");
											ee.printStackTrace();
										}
										Util.writeInFileTransaction(folder, file,
												"Switch status annulation : [" + s_status + "]");
										if (repAnnu.equals("00")) {
											dmd.setEtat_demande("SW_ANNUL_AUTO");
											demandePaiementService.save(dmd);
											demandeDtoMsg.setMsgRefus(
													"La transaction en cours n’a pas abouti (Web service CallBack Hors service), votre compte ne sera pas débité, merci de réessayer .");
											model.addAttribute("demandeDto", demandeDtoMsg);
											page = "operationAnnulee";
										} else {
											page = "error";
										}

										Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
										System.out.println("Fin processRequest ()");
										return page;

									}
								}
							}
						}

					} else {

						Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
						Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

						try {
							Util.writeInFileTransaction(folder, file,
									"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

							dmd.setEtat_demande("SW_REJET");
							demandePaiementService.save(dmd);
							
							hist.setHatEtat('A');
							histoAutoGateService.save(hist);

						} catch (Exception e) {
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
											+ orderid + "]" + e);
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer .");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
							System.out.println("Fin processRequest ()");
							return page;
						}
						Util.writeInFileTransaction(folder, file, "update Demandepaiement status to SW_REJET OK.");
					}

					Util.writeInFileTransaction(folder, file, "Generating paymentid...");

					String uuid_paymentid, paymentid = "";
					try {
						uuid_paymentid = String.format("%040d",
								new BigInteger(UUID.randomUUID().toString().replace("-", ""), 22));
						paymentid = uuid_paymentid.substring(uuid_paymentid.length() - 22);
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error during  paymentid generation for given orderid:[" + orderid
										+ "]" + e);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur lors de la génération de l'ID de paiement), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
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

					try {
						authnumber = hist.getHatNautemt();
						coderep = hist.getHatCoderep();
						motif = hist.getHatMtfref1();
						merchnatidauth = hist.getHatNumcmr();
						dtdem = dmd.getDem_pan();
						transactionid = String.valueOf(hist.getHatNumdem());
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
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
								+ "&montant=" + amount + "&frais=" + "" + "&repauto=" + coderep + "&numAuto="
								+ authnumber + "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte="
								+ dmd.getType_carte() + "&numTrans=" + transactionid;

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
							if (dmd.getSuccessURL() != null) {
								// generation et envoie du token de la carte enregitré dans le successURL
								// apres le paiment de 1 DH de check porteur carte
								if (dmd.getIs_addcard().equals("Y") && dmd.getIs_tokenized().equals("Y")
										&& dmd.getIs_withsave().equals("Y") && dmd.getIs_cof().equals("Y")) {
									Util.writeInFileTransaction(folder, file,
											"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
									System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
									boolean flag = false;
									String tokencard = "";
									String data_noncrypt_token = "";
									String data_token = "";
									CardtokenDto cardtokenDto = new CardtokenDto();
									try {
										// insert new cardToken
										tokencard = Util.generateCardToken(merchantid);
										// test if token not exist in DB
										CardtokenDto checkCardToken = cardtokenService
												.findByIdMerchantAndToken(merchantid, tokencard);

										while (checkCardToken != null) {
											tokencard = Util.generateCardToken(merchantid);
											System.out.println(
													"checkCardToken exist => generate new tokencard : " + tokencard);
											Util.writeInFileTransaction(folder, file,
													"checkCardToken exist => generate new tokencard : " + tokencard);
											checkCardToken = cardtokenService.findByIdMerchantAndToken(merchantid,
													tokencard);
										}
										System.out.println("tokencard : " + tokencard);
										Util.writeInFileTransaction(folder, file, "tokencard : " + tokencard);

										cardtokenDto.setToken(tokencard);
										String tokenid = UUID.randomUUID().toString();
										cardtokenDto.setIdToken(tokenid);
										Calendar dateCalendar = Calendar.getInstance();
										Date dateToken = dateCalendar.getTime();
										expirydate = dmd.getDateexpnaps();
										Util.writeInFileTransaction(folder, file,
												"cardtokenDto expirydate : " + expirydate);
										String anne = String.valueOf(dateCalendar.get(Calendar.YEAR));
										// get year from date
										String xx = anne.substring(0, 2) + expirydate.substring(0, 2);
										String MM = expirydate.substring(2, expirydate.length());
										// format date to "yyyy-MM-dd"
										String expirydateFormated = xx + "-" + MM + "-" + "01";
										System.out.println("cardtokenDto expirydate formated : " + expirydateFormated);
										Util.writeInFileTransaction(folder, file,
												"cardtokenDto expirydate formated : " + expirydateFormated);
										Date dateExp = dateFormatSimple.parse(expirydateFormated);
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
										data_noncrypt_token = "id_commande=" + orderid + "&montant=" + amount
												+ "&repauto=" + coderep + "&numAuto=" + authnumber + "&numCarte="
												+ Util.formatCard(cardnumber) + "&numTrans=" + transactionid + "&token="
												+ cardtokenSaved.getToken();

										data_token = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt_token,
												current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

										Util.writeInFileTransaction(folder, file, "data_token encrypt : " + data_token);
										System.out.println("data_token encrypt : " + data_token);

										response.sendRedirect(dmd.getSuccessURL() + "?data=" + data_token
												+ "==&codecmr=" + merchantid);
									} catch (Exception e) {
										Util.writeInFileTransaction(folder, file,
												"authorization 500 Error during saving CRADTOKEN given authnumber:["
														+ authnumber + "]" + e);
										flag = true;
									}
									if (flag) {
										data_noncrypt_token = "id_commande=" + orderid + "&montant=" + amount
												+ "&repauto=" + coderep + "&numAuto=" + authnumber + "&numCarte="
												+ Util.formatCard(cardnumber) + "&numTrans=" + transactionid + "&token="
												+ tokencard;

										data_token = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt_token,
												current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

										Util.writeInFileTransaction(folder, file, "data_token encrypt : " + data_token);
										System.out.println("data_token encrypt : " + data_token);
										response.sendRedirect(dmd.getSuccessURL() + "?data=" + data_token
												+ "==&codecmr=" + merchantid);
									}
								} else {
									// envoie de la reponse normal
									Util.writeInFileTransaction(folder, file,
											"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
									System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());

									response.sendRedirect(
											dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
								}
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
								Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
								System.out.println("Fin processRequest ()");
								return page;
							}
						} else {
							Util.writeInFileTransaction(folder, file,
									"coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
							System.out
									.println("coderep = " + coderep + " => Redirect to failURL : " + dmd.getFailURL());
							String libelle = "";
							try {
								CodeReponseDto codeReponseDto = codeReponseService.findByRpcCode(coderep);
								System.out.println("codeReponseDto : " + codeReponseDto);
								Util.writeInFileTransaction(folder, file, "codeReponseDto : " + codeReponseDto);
								if (codeReponseDto != null) {
									libelle = codeReponseDto.getRpcLibelle();
								}
							} catch (Exception ee) {
								Util.writeInFileTransaction(folder, file, "payer 500 Error codeReponseDto null");
								ee.printStackTrace();
							}
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Error during response Switch coderep "
											+ coderep + ":" + libelle + "),"
											+ " votre compte ne sera pas débité, merci de réessayer .");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
							System.out.println("Fin processRequest ()");
							return page;
						}

					} catch (Exception jsouterr) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error during jso out processing given authnumber:[" + authnumber
										+ "]" + jsouterr);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

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

						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");

						return jso.toString();
					} catch (Exception ex) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error during jso out processing " + ex);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
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
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "BIN NON PARAMETRE":
						Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
						dmd.setEtat_demande("MPI_BIN_NON_PAR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "DIRECTORY SERVER":
						Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
						dmd.setEtat_demande("MPI_DS_ERR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (MPI_DS_ERR), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "CARTE ERRONEE":
						Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
						dmd.setEtat_demande("MPI_CART_ERROR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "CARTE NON ENROLEE":
						Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
						dmd.setEtat_demande("MPI_CART_NON_ENR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "ERROR REPONSE ACS":
						Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
						dmd.setEtat_demande("MPI_ERR_RS_ACS");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (ERROR REPONSE ACS), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "Error 3DSS":
						Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
						dmd.setEtat_demande("MPI_ERR_3DSS");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (ERROR 3DSS), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin process ()");
						return page;
					}
				} else {
					switch (errmpi) {
					case "COMMERCANT NON PARAMETRE":
						Util.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
						dmd.setDem_xid(threeDSServerTransID);
						dmd.setEtat_demande("MPI_CMR_INEX");
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "BIN NON PARAMETRE":
						Util.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
						dmd.setEtat_demande("MPI_BIN_NON_PAR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "DIRECTORY SERVER":
						Util.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
						dmd.setEtat_demande("MPI_DS_ERR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (MPI_DS_ERR), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "CARTE ERRONEE":
						Util.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
						dmd.setEtat_demande("MPI_CART_ERROR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "CARTE NON ENROLEE":
						Util.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
						dmd.setEtat_demande("MPI_CART_NON_ENR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "ERROR REPONSE ACS":
						Util.writeInFileTransaction(folder, file, "ERROR REPONSE ACS : " + idDemande);
						dmd.setEtat_demande("MPI_ERR_RS_ACS");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (ERROR REPONSE ACS), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					case "Error 3DSS":
						Util.writeInFileTransaction(folder, file, "Error 3DSS : " + idDemande);
						dmd.setEtat_demande("MPI_ERR_3DSS");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (ERROR 3DSS), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequest ()");
						return page;
					}
				}

			} else {
				Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> cleanCres TransStatus = N ");
				System.out.println("ACSController RETOUR ACS =====> cleanCres TransStatus = N ");
				DemandePaiementDto demandeP = new DemandePaiementDto();
				Util.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());
				System.out.println(
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());

				demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());

				if (demandeP != null) {

					demandeP.setEtat_demande("RETOUR_ACS_NON_AUTH");
					demandePaiementService.save(demandeP);

					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .";
					String data_noncrypt = "id_commande=" + demandeP.getCommande() + "&nomprenom="
							+ demandeP.getPrenom() + "&email=" + demandeP.getEmail() + "&montant="
							+ demandeP.getMontant() + "&frais=" + "" + "&repauto=" + "" + "&numAuto=" + ""
							+ "&numCarte=" + Util.formatCard(demandeP.getDem_pan()) + "&typecarte="
							+ demandeP.getType_carte() + "&numTrans=" + "";

					Util.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
					System.out.println("data_noncrypt : " + data_noncrypt);

					InfoCommercantDto current_infoCommercant = null;
					try {
						current_infoCommercant = infoCommercantService.findByCmrCode(demandeP.getComid());
					} catch (Exception e) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 InfoCommercant misconfigured in DB or not existing orderid:["
										+ demandeP.getCommande() + "] and merchantid:[" + demandeP.getComid() + "]"
										+ e);
					}

					if (current_infoCommercant == null) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 InfoCommercantDto misconfigured in DB or not existing orderid:["
										+ demandeP.getCommande() + "] and merchantid:[" + demandeP.getComid() + "]");
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (InfoCommercant mal configuré dans la base de données ou inexistant), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
						System.out.println("Fin processRequest ()");
						return page;
					}

					String plainTxtSignature = demandeP.getCommande() + current_infoCommercant.getClePub();

					Util.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
					System.out.println("plainTxtSignature : " + plainTxtSignature);

					String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt,
							current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

					Util.writeInFileTransaction(folder, file, "data encrypt : " + data);
					System.out.println("data encrypt : " + data);

					Util.writeInFileTransaction(folder, file,
							"TransStatus = N => Redirect to FailURL : " + demandeP.getFailURL());
					System.out.println("TransStatus = N => Redirect to FailURL : " + demandeP.getFailURL());
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				} else {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .";
					demandeDtoMsg.setMsgRefus(msgRefus);
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
					System.out.println("Fin processRequest ()");
					return page;
				}
			}
		} catch (Exception ex) {
			Util.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> Exception " + ex);
			System.out.println("ACSController RETOUR ACS =====> Exception " + ex);
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
			System.out.println("Fin processRequest ()");
			return page;
		}
		Util.writeInFileTransaction(folder, file, "Fin processRequest ()");
		System.out.println("Fin processRequest ()");

		return page;
	}

	public String AnnulationAuto(DemandePaiementDto current_dmd, CommercantDto current_merchant,
			HistoAutoGateDto current_hist, String folder, String file) {

		SimpleDateFormat formatheure, formatdate = null;
		String date, heure, jul = "";

		String[] mm;
		String[] m;
		String montanttrame = "";
		String amount = String.valueOf(current_dmd.getMontant());
		String orderid = current_dmd.getCommande();
		String merchantid = current_dmd.getComid();
		try {
			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			jul = Util.convertToJulian(new Date()) + "";
		} catch (Exception err3) {
			Util.writeInFileTransaction(folder, file,
					"annulation auto 500 Error during date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err3);

			return "annulation auto 500 Error during date formatting for given orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]" + err3;
		}

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
		} catch (Exception err4) {
			Util.writeInFileTransaction(folder, file,
					"annulation auto 500 Error during amount formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err4);

			return "annulation auto 500 Error during amount formatting";
		}

		Util.writeInFileTransaction(folder, file, "Switch processing start ...");

		String tlv = "";
		Util.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

		// controls
		String merc_codeactivite = current_merchant.getCmrCodactivite();
		String acqcode = current_merchant.getCmrCodbqe();

		String merchantname = current_merchant.getCmrAbrvnom();
		String cardnumber = current_dmd.getDem_pan();
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
			Util.writeInFileTransaction(folder, file,
					"annulation auto 500 Error during switch tlv buildu for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err4);

			return "annulation auto 500 Error during switch tlv buildu";
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

				return "annulation auto 500 Error Switch communication s_conn false";
			}

		} catch (SocketTimeoutException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch communication SocketTimeoutException";
		} catch (UnknownHostException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch communication UnknownHostException";
		}

		catch (IOException e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);
			return "annulation auto 500 Error Switch communication IOException";
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!" + e);
			return "annulation auto 500 Error Switch communication General Exception switch";
		}

		String resp = resp_tlv;
		if (resp == null) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch null response switch";
		}

		if (resp.length() < 3) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error Switch short response length() < 3 switch";
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
			return "annulation auto 500 Error during tlv Switch response parse switch";
		}

		// controle switch
		if (tag1_resp == null) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error during tlv Switch response parse tag1_resp tag null";
		}

		if (tag1_resp.length() < 3) {
			Util.writeInFileTransaction(folder, file, "Switch  malfunction !!!");
			return "annulation auto 500 Error during tlv Switch response parse tag1_resp length tag  < 3 switch";
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
			return "annulation auto 500 Switch malfunction response code not present";
		}
		if (tag20_resp.length() < 1) {
			return "annulation auto 500 Switch malfunction response code length incorrect";
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
						"annulation auto 500 Error during  demandepaiement update  A for given orderid:[" + orderid
								+ "]" + e);

				return "annulation auto 500 Error during  demandepaiement update A";
			}

			Util.writeInFileTransaction(folder, file, "Setting DemandePaiement status OK.");

			Util.writeInFileTransaction(folder, file, "Setting HistoAutoGate status A ...");

			try {
				current_hist.setHatEtat('A');
				histoAutoGateService.save(current_hist);
			} catch (Exception e) {
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file,
						"annulation auto 500 Error during  HistoAutoGate update  A for given orderid:[" + orderid + "]"
								+ e);

				return "annulation auto 500 Error during  HistoAutoGate update A";
			}

			Util.writeInFileTransaction(folder, file, "Setting HistoAutoGate status OK.");
		} else {

			Util.writeInFileTransaction(folder, file, "Transaction annulation auto declined.");
			Util.writeInFileTransaction(folder, file, "Switch CODE REP : [" + tag20_resp + "]");
		}

		return tag20_resp;
	}

	public String redirectFailURL(DemandePaiementDto demandePaiementDto, String folder, String file)
			throws IOException {

		Util.writeInFileTransaction(folder, file,
				"REDIRECT FAIL URL DEMANDE PAIEMENT {" + demandePaiementDto.getIddemande() + "} => " + "Commerçant: {"
						+ demandePaiementDto.getComid() + "} Commande: {" + demandePaiementDto.getCommande() + "}");
		String signedFailUrl;
		String idCommande = demandePaiementDto.getCommande();
		infoCommercantDto = infoCommercantService.findByCmrCode(demandePaiementDto.getComid());
		String md5Signature = Util.hachInMD5(idCommande + infoCommercantDto.getClePub());

		signedFailUrl = demandePaiementDto.getFailURL() + "?id_commande=" + demandePaiementDto.getCommande() + "&token="
				+ md5Signature;
		Util.writeInFileTransaction(folder, file, "FAIL URL Signed : " + signedFailUrl);
		return signedFailUrl;
	}

	private boolean is_reccuring_check(String recurring) {
		if (recurring.equalsIgnoreCase("Y"))
			return true;
		else
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
	
	public static String sendPOST(String urlcalback, String clepub, String idcommande, String repauto, String montant,
			String numAuto, Long numTrans, String token_gen, String pan_trame, String typecarte, String folder,
			String file) throws IOException {

		String result = "";
		HttpPost post = new HttpPost(urlcalback);

		String reqenvoi = idcommande + repauto + clepub + montant;
		String signature = Util.hachInMD5(reqenvoi);
		Util.writeInFileTransaction(folder, file, "Signature : " + signature);
		// add request parameters or form parameters
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
			Util.writeInFileTransaction(folder, file, " sendPOST Exception => {} tv 1 :" + ex.getMessage() + "ex : " + ex);
			result = "ko";
		}
		boolean repsucces = result.indexOf("GATESUCCESS") != -1 ? true : false;
		boolean repfailed = result.indexOf("GATEFAILED") != -1 ? true : false;
		if (!repsucces && !repfailed) {
			try {
				Thread.sleep(10000);
				Util.writeInFileTransaction(folder, file, idcommande + " Recall URL tentative 2");
				// tentative 2 apès 10 s
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse response = httpClient.execute(post)) {

					result = EntityUtils.toString(response.getEntity());
				}
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file, " sendPOST Exception => {} tv 2 :" + ex.getMessage() + "ex : " + ex);
				result = "ko";
			}
			boolean repsucces2 = result.indexOf("GATESUCCESS") != -1 ? true : false;
			boolean repfailed2 = result.indexOf("GATEFAILED") != -1 ? true : false;
			if (!repsucces2 && !repfailed2) {
				try {
					Thread.sleep(10000);
					// tentative 3 après 10s
					Util.writeInFileTransaction(folder, file, idcommande + "Recall URL tentative 3");
					try (CloseableHttpClient httpClient = HttpClients.createDefault();
							CloseableHttpResponse response = httpClient.execute(post)) {

						result = EntityUtils.toString(response.getEntity());
					}
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file, " sendPOST Exception => {} tv 3 :" + ex.getMessage() + "ex : " + ex);
					result = "ko";
				}
			}
		}

		return result;
	}

	public ReponseReglements preparerReglementLydec(List<FactureLDDto> listFactureLD, String num_auto,
			java.util.Calendar date_pai, DemandePaiementDto demandePaiement, InfoCommercantDto infoCommercant,
			String folder, String file) throws IOException {
		Util.writeInFileTransaction(folder, file, "Debut preparerReglementLydec");
		ReponseReglements reponseReglement = null;
		try {
			// java.util.Calendar date = Calendar.getInstance();
			DemandesReglements demReglement = new DemandesReglements();
			demReglement.setAgc_Cod((short) 840);
			BigDecimal b2 = new BigDecimal("-1");

			List<Impaye> factListImpayes = new ArrayList<Impaye>();
			BigDecimal montant = new BigDecimal(0);
			BigDecimal montantTimbre = new BigDecimal(0);
			BigDecimal montantTotalSansTimbre = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
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
				imp.setMontantTTC(new BigDecimal(facLD.getMontantTtc()).setScale(2, BigDecimal.ROUND_HALF_UP));
				imp.setMontantTimbre(new BigDecimal(facLD.getMontantTbr()).setScale(2, BigDecimal.ROUND_HALF_UP));
				imp.setMontantTVA(new BigDecimal(facLD.getMontantTva()).setScale(2, BigDecimal.ROUND_HALF_UP));
				montant = montant.add(new BigDecimal(facLD.getMontantTtc()).setScale(2, BigDecimal.ROUND_HALF_UP));
				montantTimbre = montantTimbre
						.add(new BigDecimal(facLD.getMontantTbr()).setScale(2, BigDecimal.ROUND_HALF_UP));
				Util.writeInFileTransaction(folder, file, "preparerReglementLydec imp  : " + imp.toString());
				factListImpayes.add(imp);
			}
			Util.writeInFileTransaction(folder, file,
					"preparerReglementLydec factListImpayes size : " + factListImpayes.size());

			montantTotalSansTimbre = montant.subtract(montantTimbre);

			Portefeuille[] listePortefeuilles = preparerTabEcritureLydecListe(factListImpayes);
			Util.writeInFileTransaction(folder, file,
					"preparerReglementLydec listePortefeuilles size : " + listePortefeuilles.length);

			MoyenPayement[] listeMoyensPayement = new MoyenPayement[1];

			MoyenPayement ecr = new MoyenPayement();

			ecr.setType_Moy_Pai("C");
			ecr.setBanq_Cod("NPS");

			ecr.setDate_Pai(date_pai);
			ecr.setMontant(montantTotalSansTimbre);
			ecr.setMoyen_Pai(num_auto);
			listeMoyensPayement[0] = ecr;
			Util.writeInFileTransaction(folder, file,
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
			transaction.setMt_Facture(new BigDecimal(0));
			transaction.setMt_Credite_Cred(new BigDecimal(0));
			transaction.setMt_Credite_Vers(new BigDecimal(0));
			transaction.setMt_Credite_Prov(new BigDecimal(0));
			transaction.setMt_Remb_Cheq(new BigDecimal(0));
			transaction.setMt_Od(new BigDecimal(0));
			transaction.setMt_Enc_Mp(montant.subtract(montantTimbre));
			transaction.setMt_Debite(montant.multiply(b2));
			transaction.setMt_Enc_Esp(new BigDecimal(0));
			transaction.setTr_Recu("");

			demReglement.setTransaction(transaction);
			demReglement.setListeMoyensPayement(listeMoyensPayement);
			demReglement.setListePortefeuilles(listePortefeuilles);

			// URL wsdlURL = GererEncaissementService.WSDL_LOCATION;
			URL wsdlURL = new URL(URL_WSDL_LYDEC);
			Util.writeInFileTransaction(folder, file, "wsdlURL : " + wsdlURL);

			GererEncaissementService ss = new GererEncaissementService(wsdlURL, SERVICE_NAME);
			GererEncaissement port = ss.getGererEncaissement();
			Util.writeInFileTransaction(folder, file, "preparerReglementLydec transaction : " + transaction.toString());
			Util.writeInFileTransaction(folder, file,
					"preparerReglementLydec demReglement : " + demReglement.toString());

			reponseReglement = port.ecrireReglements(demReglement);

			if (reponseReglement != null) {
				System.out.println("reponseReglement isOk/message : " + reponseReglement.isOk() + "/"
						+ reponseReglement.getMessage());
				Util.writeInFileTransaction(folder, file, "reponseReglement isOk/message : " + reponseReglement.isOk()
						+ "/" + reponseReglement.getMessage());
			} else {
				System.out.println("reponseReglement : " + null);
				Util.writeInFileTransaction(folder, file, "reponseReglement : " + null);
			}

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "preparerReglementLydec Exception =>" + e.getMessage());
			Util.writeInFileTransaction(folder, file, "preparerReglementLydec Exception =>" + e.getStackTrace());
		}
		Util.writeInFileTransaction(folder, file, "Fin preparerReglementLydec");
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

	public String envoyerConfirmation(DemandePaiementDto demandePaiementDto, HttpServletResponse response,
			String numAuto, String folder, String file) throws IOException {

		Util.writeInFileTransaction(folder, file,
				" *************************************** Debut envoyerConfirmation DGI *************************************** ");
		Util.writeInFileTransaction(folder, file, "Commande : " + demandePaiementDto.getCommande());
		CFDGIDto cfDGI = new CFDGIDto();
		cfDGI = cfdgiService.findCFDGIByIddemande(demandePaiementDto.getIddemande());
		List<ArticleDGIDto> articles = articleDGIService
				.getArticlesByIddemandeSansFrais(demandePaiementDto.getIddemande());

		String num_taxe = cfDGI.getcF_R_OINReference();
		String montantTotal = String.valueOf(demandePaiementDto.getMontant());
		String montantTrans = cfDGI.getcF_R_OIMtTotal();
		String concatcreanceConfirmesIds = "";
		// concatcreanceConfirmesIds = la concaténation de la valeur 'UniqueID' des
		// balises <Article>
		// en excluant celle avec la valeur 111111111111 qui correspond au frais.
		// (cet
		// UniqueID est sur 13 caractère)
		for (ArticleDGIDto art : articles) {
			concatcreanceConfirmesIds = concatcreanceConfirmesIds + art.getUniqueID();
		}
		String creancier_id = concatcreanceConfirmesIds;
		String email = cfDGI.getcF_R_OIemail();
		dateTX = demandePaiementDto.getDateRetourSWT().replaceAll("\\s+", "").replaceAll("-", "").replaceAll(":", "");
		dateTX = dateTX.substring(0, Math.min(dateTX.length(), 14));
		String date_taxe = dateTX;
		String type_creance_id = cfDGI.getcF_R_OICodeOper(); // "03";
		String callbackURL = demandePaiementDto.getCallbackURL();

		String resultcallback = sendPOSTDGIInsert(callbackURL, montantTrans, montantTotal, creancier_id,
				type_creance_id, num_taxe, email, date_taxe, cfDGI, articles);

		Util.writeInFileTransaction(folder, file,
				"envoyerConfirmation resultcallbackDGI : " + resultcallback.toString());
		if (!resultcallback.equals("")) {
			JSONObject json = new JSONObject(resultcallback);
			String msg = (String) json.get("msg");
			String refReglement = (String) json.get("refReglement");
			String codeRetour = (String) json.get("codeRetour");
			String refcanal = (String) json.get("refcanal");
			Util.writeInFileTransaction(folder, file,
					"envoyerConfirmation resultcallbackDGI => codeRetour/refReglement/msg/refcanal : " + codeRetour
							+ "/" + refReglement + "/" + msg + "/" + refcanal);
			// enregistrement des infos de retour WS de la DGI
			cfDGI.setRefReglement(refReglement);
			cfDGI.setCodeRtour(codeRetour);
			cfDGI.setMsg(msg);
			cfDGI.setRefcanal(refcanal);
			cfdgiService.save(cfDGI);
			Util.writeInFileTransaction(folder, file, "update cfDGI apres retour WS de la DGI : " + cfDGI.toString());

			// fin enregistrement des infos de retour WS de la DGI
			/*
			 * if(codeRetour.equals("000")) { Util.writeInFileTransaction(folder,
			 * file," *************************************** coreRetour 000 : Envoyer email au client *************************************** "
			 * ); // pour envoyer un email au client
			 * envoyerEmail(demandePaiementDto,response, folder, file); // envoyer le lien
			 * de recu au client Util.writeInFileTransaction(folder,
			 * file," *************************************** coreRetour 000 : envoyer le lien de recu au client *************************************** "
			 * ); confirmerTrs(demandePaiementDto, response, numAuto, folder, file); } else
			 * { // annulation a developper -------------------- ????? }
			 */

		}
		Util.writeInFileTransaction(folder, file,
				" *************************************** Fin envoyerConfirmation DGI *************************************** ");

		return resultcallback;
	}

	public String sendPOSTDGIInsert(String urlcalback, String montant, String montantTotal, String creancier_id,
			String type_creance_id, String num_taxe, String email, String date_taxe, CFDGIDto cfDGI,
			List<ArticleDGIDto> articles) throws IOException {
		Util.writeInFileTransaction(folder, file,
				" *************************************** Debut sendPOSTDGIInsert DGI *************************************** ");
		String list_taxe = new Gson().toJson(articles);
		String cf = new Gson().toJson(cfDGI);
		String result = "";
		HttpPost post = new HttpPost(urlcalback);

		// add request parameters or form parameters
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
		Util.writeInFileTransaction(folder, file,
				"sendPOSTDGIInsert commande / urlParameters :" + cfDGI.getcF_R_OINReference() + " / " + urlParameters);
		try {
			try (CloseableHttpClient httpClient = HttpClients.createDefault();

					CloseableHttpResponse response = httpClient.execute(post)) {

				result = EntityUtils.toString(response.getEntity());
				Util.writeInFileTransaction(folder, file, "sendPOSTDGIInsert result : " + result);
			}
		} catch (Exception ex) {
			result = "ko";
			Util.writeInFileTransaction(folder, file, "sendPOSTDGIInsert result 1 : " + result + ex);
		}
		JSONObject json = new JSONObject(result);
		String msg = (String) json.get("msg");
		String refReglement = (String) json.get("refReglement");
		String codeRetour = (String) json.get("codeRetour");
		String refcanal = (String) json.get("refcanal");
		Util.writeInFileTransaction(folder, file,
				"sendPOSTDGIInsert resultcallbackDGI => codeRetour/refReglement/msg/refcanal : " + codeRetour + "/"
						+ refReglement + "/" + msg + "/" + refcanal);
		if (!codeRetour.equals("000")) {
			try {
				Thread.sleep(10000);

				// tentative 2 apès 10 s
				Util.writeInFileTransaction(folder, file, "sendPOSTDGIInsert tentative 2 apès 10 s: ");
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse response = httpClient.execute(post)) {

					result = EntityUtils.toString(response.getEntity());
					Util.writeInFileTransaction(folder, file,
							"sendPOSTDGIInsert tentative 2 apès 10 s result: " + result);
				}
			} catch (Exception ex) {
				result = "ko";
				Util.writeInFileTransaction(folder, file, "sendPOSTDGIInsert result 2 : " + result + ex);
			}
		}
		json = new JSONObject(result);
		msg = (String) json.get("msg");
		refReglement = (String) json.get("refReglement");
		codeRetour = (String) json.get("codeRetour");
		if (!codeRetour.equals("000")) {
			try {
				Thread.sleep(10000);

				// tentative 3 après 10s
				Util.writeInFileTransaction(folder, file, "sendPOSTDGIInsert tentative 3 apès 10 s: ");
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse response = httpClient.execute(post)) {

					result = EntityUtils.toString(response.getEntity());
					Util.writeInFileTransaction(folder, file,
							"sendPOSTDGIInsert tentative 3 apès 10 s result: " + result);
				}
			} catch (Exception ex) {
				result = "ko";
				Util.writeInFileTransaction(folder, file, "sendPOSTDGIInsert result 3 : " + result + ex);
			}
		}
		Util.writeInFileTransaction(folder, file,
				" *************************************** Fin sendPOSTDGIInsert DGI *************************************** ");

		return result;
	}

	public void confirmerTrs(DemandePaiementDto demandePaiementDto, HttpServletResponse response, String numAuto,
			String folder, String file) throws IOException {
		Util.writeInFileTransaction(folder, file,
				" *************************************** Debut confirmerTrs DGI *************************************** ");

		try {

			Util.writeInFileTransaction(folder, file, "Commande : " + demandePaiementDto.getCommande());
			ArticleDGIDto artdgi = new ArticleDGIDto();
			CFDGIDto cfDGI = new CFDGIDto();
			artdgi = articleDGIService.findVraiArticleByIddemande(demandePaiementDto.getIddemande());
			// retourWSKey5 = "A7D87E2HQ185BA70EBPXA017A325D777" ;
			// clé retour ws : preprod dgi
			if (DGI_PREPROD.equals(demandePaiementDto.getComid())) {
				retourWSKey5 = "A7D87E2HQ185BA70EBPXA017A325D777";
			}
			// clé retour ws : prod dgi
			if (DGI_PROD.equals(demandePaiementDto.getComid())) {
				retourWSKey5 = "543D523A710AXPBE07AB581QH2E78D8R";
			}
			cfDGI = cfdgiService.findCFDGIByIddemande(demandePaiementDto.getIddemande());
			idTxSysPmt = artdgi.getUniqueID();
			link = cfDGI.getcF_R_OIConfirmUrl();
			ref = cfDGI.getcF_R_OINReference();
			idService = cfDGI.getcF_R_OICodeOper();
			Double montant = demandePaiementDto.getMontant();
			idsysPmt = 302; // par defaut 100 selon les spec
			dateTX = demandePaiementDto.getDateRetourSWT().replaceAll("\\s+", "").replaceAll("-", "").replaceAll(":",
					"");
			dateTX = dateTX.substring(0, Math.min(dateTX.length(), 14));
			IdTxMTC = numAuto; // autorisation
			statut = "C";

			Sec = Util.hachInMD5(ref + idTxSysPmt + dateTX + montant + statut + idsysPmt + idService + retourWSKey5);

			String linkDGI = link + "&ref=" + ref + "&idService=" + idService + "&statut=" + statut + "&montant="
					+ montant + "&idsysPmt=" + idsysPmt + "&idTxSysPmt=" + idTxSysPmt + "&IdTxMTC=" + IdTxMTC
					+ "&dateTX=" + dateTX + "&Sec=" + Sec;

			Util.writeInFileTransaction(folder, file, "linkDGI : " + linkDGI);

			response.sendRedirect(linkDGI);

			return;
		} catch (Exception e) {

			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-CONFIRMERTRS] " + e);

		}
		Util.writeInFileTransaction(folder, file,
				" *************************************** Fin confirmerTrs DGI *************************************** ");
	}

	public void envoyerEmail(DemandePaiementDto demandePaiementDto, HttpServletResponse response, String folder,
			String file) throws IOException {

		Util.writeInFileTransaction(folder, file,
				" *************************************** Debut envoie email au client DGI *************************************** ");

		Gson gson = new Gson();

		String urlSendEmailDGI = LIEN_ENVOIE_EMAIL_DGI;
		Util.writeInFileTransaction(folder, file, "envoie email au client =====> urlSendEmailDGI : " + urlSendEmailDGI);

		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			httpClient = getAllSSLClient();
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
			Util.writeInFileTransaction(folder, file,
					"[GW-EXCEPTION-KeyManagementException] RecapDGI envoyerEmail  " + e1);
		}

		HttpPost httpPost = new HttpPost(urlSendEmailDGI);

		RequestEnvoieEmail requestEnvoieEmail = new RequestEnvoieEmail();

		requestEnvoieEmail.setIdDemande(demandePaiementDto.getIddemande());
		requestEnvoieEmail.setIdCommande(demandePaiementDto.getCommande());
		requestEnvoieEmail.setNumCmr(demandePaiementDto.getComid());
		Util.writeInFileTransaction(folder, file,
				"envoie email au client =====> requestEnvoieEmail : " + requestEnvoieEmail);

		final String jsonBody = gson.toJson(requestEnvoieEmail);

		Util.writeInFileTransaction(folder, file, "envoie email au client =====> jsonBody : " + jsonBody);

		final StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {
			HttpResponse responseTheeDs = httpClient.execute(httpPost);

			StatusLine responseStatusLine = responseTheeDs.getStatusLine();
			Util.writeInFileTransaction(folder, file, "RecapDGI envoyerEmail =====> RETOUR API response StatusCode : "
					+ responseTheeDs.getStatusLine().getStatusCode());
			Util.writeInFileTransaction(folder, file,
					"RecapDGI envoyerEmail =====> RETOUR API responseStatusLine : " + responseStatusLine);
			String respStr = EntityUtils.toString(responseTheeDs.getEntity());

			Util.writeInFileTransaction(folder, file, "RecapDGI envoyerEmail =====> RETOUR API respStr : " + respStr);

		} catch (Exception e) {

			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-ENVOYEREMAIL] " + e);

		}
		Util.writeInFileTransaction(folder, file,
				" *************************************** Fin envoie email au client DGI *************************************** ");
	}

	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(null, trustAllCerts, null);

		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		builder.setSSLSocketFactory(sslConnectionFactory);

		PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();

		return builder.build();

	}
}
