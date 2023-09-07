package ma.m2m.gateway.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.SplittableRandom;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.dto.SWHistoAutoDto;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.encryption.RSACrypto;
import ma.m2m.gateway.service.AutorisationService;
import ma.m2m.gateway.service.CardtokenService;
import ma.m2m.gateway.service.CommercantService;
import ma.m2m.gateway.service.DemandePaiementService;
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
//import ma.m2m.gateway.service.SWHistoAutoService;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Controller
public class ACSController {

	private Traces traces = new Traces();
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

	// @Autowired
	// SWHistoAutoService swHistoAutoService;

	@Autowired
	CardtokenService cardtokenService;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private InfoCommercantDto infoCommercantDto = new InfoCommercantDto();

	public ACSController() {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "RETOUR_ACS_" + randomWithSplittableRandom;
		// date of folder logs
		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	@RequestMapping(value = "/napspayment/acs", method = RequestMethod.POST)
	@ResponseBody
	public void processRequest(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		file = "RETOUR_ACS_" + randomWithSplittableRandom;
		// create file log
		traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start processRequest ()");
		CRes cleanCres = new CRes();
		try {
			String encodedCres = request.getParameter("cres");
			System.out.println("ACSController RETOUR ACS =====> encodedCres : " + encodedCres);
			traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> encodedCres : " + encodedCres);

			String decodedCres = "";

			decodedCres = new String(Base64.decodeBase64(encodedCres.getBytes()));
			if (decodedCres.indexOf("}") != -1) {
				decodedCres = decodedCres.substring(0, decodedCres.indexOf("}") + 1);
			}
			traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> decodedCres : " + decodedCres);
			System.out.println("ACSController RETOUR ACS =====> decodedCres : " + decodedCres);

			cleanCres = gson.fromJson(decodedCres, CRes.class);
			traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> cleanCres : " + cleanCres);

			String msgRefus = "";
			// just for test
			// cleanCres.setTransStatus("N");

			if (cleanCres.getTransStatus().equals("Y")) {
				System.out.println("ACSController RETOUR ACS =====> cleanCres TransStatus = Y ");
				traces.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> cleanCres TransStatus = Y ");

				System.out.println("ACSController RETOUR ACS =====> callThree3DSSAfterACS ");
				traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> callThree3DSSAfterACS ");

				ThreeDSecureResponse threeDsecureResponse = autorisationService.callThree3DSSAfterACS(decodedCres,
						folder, file);

				DemandePaiementDto dmd = null;
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
				String trameRepMPI = "";
				String idCommercant = "";
				String merchantName = "";
				String montantStr = "";

				String expirationCarte = "";
				String merchantEmail = "";
				String id_client = "";
				String idCommande = "";

				String paResSyntaxOK = "";
				String paResVerified = "";

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
					traces.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
					traces.writeInFileTransaction(folder, file, "demandePaiement after update MPI_KO idDemande null");
					// response.sendRedirect("GW-AUTO-INVALIDE-DEM");
					response.sendRedirect(link_result);
					return;
				}

				dmd = demandePaiementService.findByIdDemande(Integer.parseInt(idDemande));

				if (dmd == null) {
					Util.writeInFileTransaction(folder, file,
							"demandePaiement not found !!!! demandePaiement = null  / received idDemande from MPI => "
									+ idDemande);
					// response.sendRedirect("GW-AUTO-INVALIDE-DEM");
					response.sendRedirect(link_result);
					return;
				}

				// Merchnat info
				merchantid = dmd.getComid();
				websiteid = dmd.getGalid();

				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

				traces.writeInFileTransaction(folder, file, "authorization_" + orderid + timeStamp);

				CommercantDto current_merchant = null;
				try {
					current_merchant = commercantService.findByCmrCode(merchantid);
				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);
					response.sendRedirect(link_result);
				}

				if (current_merchant == null) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					response.sendRedirect(link_result);
				}

				if (current_merchant.getCmrCodactivite() == null) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					response.sendRedirect(link_result);
				}

				if (current_merchant.getCmrCodbqe() == null) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					response.sendRedirect(link_result);
				}
				InfoCommercantDto current_infoCommercant = null;

				try {
					current_infoCommercant = infoCommercantService.findByCmrCode(merchantid);
				} catch (Exception e) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 InfoCommercant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);
					response.sendRedirect(link_result);
				}

				if (current_infoCommercant == null) {
					traces.writeInFileTransaction(folder, file,
							"authorization 500 InfoCommercantDto misconfigured in DB or not existing orderid:["
									+ orderid + "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid
									+ "]");
					response.sendRedirect(link_result);
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
				expirydate = dmd.getDateexpnaps();
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
					traces.writeInFileTransaction(folder, file,
							"authorization 500 Error during  date formatting for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err2);
					response.sendRedirect(link_result);
				}

				if (reponseMPI.equals("") || reponseMPI == null) {
					dmd.setEtat_demande("MPI_KO");
					demandePaiementService.save(dmd);
					Util.writeInFileTransaction(folder, file,
							"demandePaiement after update MPI_KO reponseMPI null : " + dmd.toString());
					Util.writeInFileTransaction(folder, file, "Response 3DS is null");
					// response.sendRedirect(redirectFailURL(dmd, folder, file));
					response.sendRedirect(link_result);
				}

				if (reponseMPI.equals("Y")) {
					// ********************* Cas chalenge responseMPI equal C ou D
					// *********************
					traces.writeInFileTransaction(folder, file,
							"********************* responseMPI equal Y *********************");

					dmd.setDem_xid(threeDSServerTransID);
					demandePaiementService.save(dmd);

//					try {
//						mm = new String[2];
//						montanttrame = "";
//
//						mm = amount.split("\\.");
//						if (mm[0].length() == 1) {
//							montanttrame = amount + "0";
//						} else {
//							montanttrame = amount + "";
//						}
//
//						m = new String[2];
//						m = montanttrame.split("\\.");
//						if (m[0].equals("0")) {
//							montanttrame = montanttrame.replace(".", "0");
//						} else
//							montanttrame = montanttrame.replace(".", "");
//						montanttrame = Util.formatageCHamps(montanttrame, 12);
//
//					} catch (Exception err3) {
//						traces.writeInFileTransaction(folder, file,
//								"authorization 500 Error during  amount formatting for given orderid:[" + orderid
//										+ "] and merchantid:[" + merchantid + "]" + err3);
//						response.sendRedirect(link_result);
//					}
					
					try {
						montanttrame = "";

						mm = new String[2];
						System.out.println("montant v0 : " + amount);
						traces.writeInFileTransaction(folder, file, "montant v0 : " + amount);
						
						if(amount.contains(",")) {
							amount = amount.replace(",", ".");
						}
						if(!amount.contains(".") && !amount.contains(",")) {
							amount = amount +"."+"00";
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
						response.sendRedirect(link_result);
					}

					boolean cvv_present = check_cvv_presence(cvv);
					boolean is_reccuring = is_reccuring_check(recurring);
					boolean is_first_trs = true;

					String first_auth = "";
					long lrec_serie = 0;

					merchant_city = "MOROCCO        ";
					traces.writeInFileTransaction(folder, file, "merchant_city : [" + merchant_city + "]");

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
						traces.writeInFileTransaction(folder, file, "cavv == null || eci == null");
					} else if (cavv != null && eci != null) {
						champ_cavv = cavv + eci;
						traces.writeInFileTransaction(folder, file, "cavv != null && eci != null");
						traces.writeInFileTransaction(folder, file, "champ_cavv : [" + champ_cavv + "]");
					} else {
						traces.writeInFileTransaction(folder, file, "champ_cavv = null");
						champ_cavv = null;
					}

					// controls
					traces.writeInFileTransaction(folder, file, "Switch processing start ...");

					String tlv = "";
					traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request start ...");

					if (!cvv_present && !is_reccuring) {
						traces.writeInFileTransaction(folder, file,
								"authorization 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");
						response.sendRedirect(link_result);
					}

					// not reccuring , normal
					if (cvv_present && !is_reccuring) {
						traces.writeInFileTransaction(folder, file,
								"not reccuring , normal cvv_present && !is_reccuring");
						try {

							/*
							 * old sans cavv et xid tlv = new TLVEncoder().withField(Tags.tag0,
							 * mesg_type).withField(Tags.tag1, cardnumber) .withField(Tags.tag3,
							 * processing_code).withField(Tags.tag22, transaction_condition)
							 * .withField(Tags.tag49, acq_type).withField(Tags.tag14, montanttrame)
							 * .withField(Tags.tag15, currency).withField(Tags.tag23, reason_code)
							 * .withField(Tags.tag18, "761454").withField(Tags.tag42, expirydate)
							 * .withField(Tags.tag16, date).withField(Tags.tag17, heure)
							 * .withField(Tags.tag10, merc_codeactivite).withField(Tags.tag8, "0" +
							 * merchantid) .withField(Tags.tag9, merchantid).withField(Tags.tag66, rrn)
							 * .withField(Tags.tag67, cvv).withField(Tags.tag11, merchant_name)
							 * .withField(Tags.tag12, merchant_city).withField(Tags.tag90,
							 * acqcode).encode();
							 */

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

							traces.writeInFileTransaction(folder, file, "tag0_request : [" + mesg_type + "]");
							traces.writeInFileTransaction(folder, file, "tag1_request : [" + cardnumber + "]");
							traces.writeInFileTransaction(folder, file, "tag3_request : [" + processing_code + "]");
							traces.writeInFileTransaction(folder, file,
									"tag22_request : [" + transaction_condition + "]");
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
							response.sendRedirect(link_result);
						}

						traces.writeInFileTransaction(folder, file, "Switch TLV Request :[" + tlv + "]");

						// commented
						/*
						 * try {
						 * 
						 * String tlv2 = new TLVEncoder().withField(Tags.tag0, mesg_type)
						 * .withField(Tags.tag1, cardnumber).withField(Tags.tag3, processing_code)
						 * .withField(Tags.tag22, transaction_condition).withField(Tags.tag49, acq_type)
						 * .withField(Tags.tag14, montanttrame).withField(Tags.tag15, currency)
						 * .withField(Tags.tag23, reason_code).withField(Tags.tag18, "761454")
						 * .withField(Tags.tag42, expirydate).withField(Tags.tag16, "****")
						 * .withField(Tags.tag17, heure).withField(Tags.tag10, merc_codeactivite)
						 * .withField(Tags.tag8, "0" + merchantid).withField(Tags.tag9, merchantid)
						 * .withField(Tags.tag66, rrn).withField(Tags.tag67, "***")
						 * .withField(Tags.tag11, merchant_name).withField(Tags.tag12, merchant_city)
						 * .withField(Tags.tag90, acqcode).encode();
						 * 
						 * traces.writeInFileTransaction(folder, file, "tlv2 : " + tlv2);
						 * 
						 * } catch (Exception e) { traces.writeInFileTransaction(folder, file,
						 * "Switch TLV Request ecncoding error " + e); e.printStackTrace(); }
						 */
						
					}

					// reccuring
					if (is_reccuring) {
						traces.writeInFileTransaction(folder, file, "reccuring");
					}

					traces.writeInFileTransaction(folder, file, "Preparing Switch TLV Request end.");

					String resp_tlv = "";
//					SwitchTCPClient sw = SwitchTCPClient.getInstance();
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

							response.sendRedirect(redirectFailURL(dmd, folder, file));
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
						switch_ko = 1;
						response.sendRedirect(link_result);
					} catch (java.net.ConnectException e) {
						traces.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
						switch_ko = 1;
						response.sendRedirect(link_result);
					}

					catch (SocketTimeoutException e) {
						traces.writeInFileTransaction(folder, file,
								"Switch  malfunction  SocketTimeoutException !!!" + e);
						switch_ko = 1;
						e.printStackTrace();
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch communication SocketTimeoutException" + "switch ip:["
										+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						response.sendRedirect(link_result);
					}

					catch (IOException e) {
						traces.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
						switch_ko = 1;
						e.printStackTrace();
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch communication IOException" + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						response.sendRedirect(link_result);
					}

					catch (Exception e) {
						traces.writeInFileTransaction(folder, file, "Switch  malfunction Exception!!!" + e);
						switch_ko = 1;
						e.printStackTrace();
						response.sendRedirect(link_result);
					}

					String resp = resp_tlv;

					// resp debug
					// resp =
					// "000001300101652345658188287990030010008008011800920090071180092014012000000051557015003504016006200721017006152650066012120114619926018006143901019006797535023001H020002000210026108000621072009800299";

					if (switch_ko == 0 && resp == null) {
						traces.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
						switch_ko = 1;
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch null response" + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						response.sendRedirect(link_result);
					}

					if (switch_ko == 0 && resp.length() < 3) {
						switch_ko = 1;

						traces.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error Switch short response length() < 3 " + "switch ip:[" + sw_s
										+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						response.sendRedirect(link_result);
					}

					traces.writeInFileTransaction(folder, file, "Switch TLV Respnose :[" + resp + "]");

					traces.writeInFileTransaction(folder, file, "Processing Switch TLV Respnose ...");

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
							traces.writeInFileTransaction(folder, file, "Switch  malfunction tlv parsing !!!" + e);
							switch_ko = 1;
							traces.writeInFileTransaction(folder, file,
									"authorization 500 Error during tlv Switch response parse" + "switch ip:[" + sw_s
											+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
							response.sendRedirect(link_result);
						}

						// controle switch
						if (tag1_resp == null) {
							traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
							switch_ko = 1;
							traces.writeInFileTransaction(folder, file,
									"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
											+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
											+ resp_tlv + "]");
						}

						if (tag1_resp != null && tag1_resp.length() < 3) {
							traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag1_resp == null");
							switch_ko = 1;
							traces.writeInFileTransaction(folder, file, "authorization 500"
									+ "Error during tlv Switch response parse tag1_resp length tag  < 3" + "switch ip:["
									+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
						}

						if (tag20_resp == null) {
							traces.writeInFileTransaction(folder, file, "Switch  malfunction !!! tag20_resp == null");
							switch_ko = 1;
							traces.writeInFileTransaction(folder, file,
									"authorization 500 Error during tlv Switch response parse tag1_resp tag null"
											+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : ["
											+ resp_tlv + "]");
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
						traces.writeInFileTransaction(folder, file,
								"getSWHistoAuto pan_auto/rrn/amount/date/merchantid : " + pan_auto + "/" + rrn + "/"
										+ amount + "/" + date + "/" + merchantid);
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
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error during  insert in histoautogate for given orderid:[" + orderid
										+ "]" + e);
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
									traces.writeInFileTransaction(folder, file,
											"inserting into telec ko..do nothing " + e);
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
							traces.writeInFileTransaction(folder, file,
									"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
											+ orderid + "]" + e);
							response.sendRedirect(redirectFailURL(dmd, folder, file));
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
								"authorization 500 Error during  paymentid generation for given orderid:[" + orderid
										+ "]" + e);
						response.sendRedirect(redirectFailURL(dmd, folder, file));
					}

					traces.writeInFileTransaction(folder, file, "Generating paymentid OK");
					traces.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

					// JSONObject jso = new JSONObject();

					traces.writeInFileTransaction(folder, file, "Preparing autorization api response");

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
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
						response.sendRedirect(redirectFailURL(dmd, folder, file));
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

						traces.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
						System.out.println("data_noncrypt : " + data_noncrypt);

						String plainTxtSignature = orderid + current_infoCommercant.getClePub();

						traces.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
						System.out.println("plainTxtSignature : " + plainTxtSignature);

						String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt,
								current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

						traces.writeInFileTransaction(folder, file, "data encrypt : " + data);
						System.out.println("data encrypt : " + data);

						if (coderep.equals("00")) {
							// String succ =
							// "http://192.10.2.118/srv-test/API_PHP_4T//confirmation/confirmpaie.php";
							traces.writeInFileTransaction(folder, file,
									"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
							System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
							response.sendRedirect(dmd.getSuccessURL() + "?data=" + data + "==&codecmr=" + merchantid);
						} else {
							// String fail =
							// "http://192.10.2.118/srv-test/API_PHP_4T//confirmation/confirmpaie.php";
							traces.writeInFileTransaction(folder, file,
									"coderep !=00 => Redirect to failURL : " + dmd.getFailURL());
							System.out.println("coderep !=00 => Redirect to failURL : " + dmd.getFailURL());
							response.sendRedirect(dmd.getFailURL() + "?data=" + data + "==&codecmr=" + merchantid);
						}

					} catch (Exception jsouterr) {
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error during jso out processing given authnumber:[" + authnumber
										+ "]" + jsouterr);
						response.sendRedirect(redirectFailURL(dmd, folder, file));
						return;
					}

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

						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);

						System.out.println("autorization api response chalenge :  [" + jso.toString() + "]");
						traces.writeInFileTransaction(folder, file,
								"autorization api response chalenge :  [" + jso.toString() + "]");

					} catch (Exception ex) {
						traces.writeInFileTransaction(folder, file,
								"authorization 500 Error during jso out processing " + ex);
						response.sendRedirect(redirectFailURL(dmd, folder, file));
					}
				} else if (reponseMPI.equals("E")) {
					// ********************* Cas responseMPI equal E
					// *********************
					traces.writeInFileTransaction(folder, file, "****** Cas responseMPI equal E ******");
					traces.writeInFileTransaction(folder, file, "errmpi/idDemande : " + errmpi + "/" + idDemande);
					dmd.setEtat_demande("MPI_DS_ERR");
					response.sendRedirect(link_result);
				} else {
					switch (errmpi) {
					case "COMMERCANT NON PARAMETRE":
						traces.writeInFileTransaction(folder, file, "COMMERCANT NON PARAMETRE : " + idDemande);
						dmd.setDem_xid(threeDSServerTransID);
						dmd.setEtat_demande("MPI_CMR_INEX");
						demandePaiementService.save(dmd);
						// response.sendRedirect("COMMERCANT NON PARAMETRE");
						response.sendRedirect(link_result);
					case "BIN NON PARAMETRE":
						traces.writeInFileTransaction(folder, file, "BIN NON PARAMETRE : " + idDemande);
						dmd.setEtat_demande("MPI_BIN_NON_PAR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						// response.sendRedirect("BIN NON PARAMETREE");
						response.sendRedirect(link_result);
					case "DIRECTORY SERVER":
						traces.writeInFileTransaction(folder, file, "DIRECTORY SERVER : " + idDemande);
						dmd.setEtat_demande("MPI_DS_ERR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						// response.sendRedirect("MPI_DS_ERR");
						response.sendRedirect(link_result);
					case "CARTE ERRONEE":
						traces.writeInFileTransaction(folder, file, "CARTE ERRONEE : " + idDemande);
						dmd.setEtat_demande("MPI_CART_ERROR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						// response.sendRedirect("CARTE ERRONEE");
						response.sendRedirect(link_result);
					case "CARTE NON ENROLEE":
						traces.writeInFileTransaction(folder, file, "CARTE NON ENROLEE : " + idDemande);
						dmd.setEtat_demande("MPI_CART_NON_ENR");
						dmd.setDem_xid(threeDSServerTransID);
						demandePaiementService.save(dmd);
						// response.sendRedirect("CARTE NON ENROLLE");
						response.sendRedirect(link_result);
					}
				}

			} else {
				traces.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> cleanCres TransStatus = N ");
				System.out.println("ACSController RETOUR ACS =====> cleanCres TransStatus = N ");
				DemandePaiementDto demandeP = new DemandePaiementDto();
				traces.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());
				System.out.println(
						"ACSController RETOUR ACS =====> findByDem_xid : " + cleanCres.getThreeDSServerTransID());

				demandeP = demandePaiementService.findByDem_xid(cleanCres.getThreeDSServerTransID());

				if (demandeP != null) {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .";
					String data_noncrypt = "id_commande=" + demandeP.getCommande() + "&nomprenom="
							+ demandeP.getPrenom() + "&email=" + demandeP.getEmail() + "&montant="
							+ demandeP.getMontant() + "&frais=" + "" + "&repauto=" + "" + "&numAuto=" + ""
							+ "&numCarte=" + Util.formatCard(demandeP.getDem_pan()) + "&typecarte="
							+ demandeP.getType_carte() + "&numTrans=" + "";

					traces.writeInFileTransaction(folder, file, "data_noncrypt : " + data_noncrypt);
					System.out.println("data_noncrypt : " + data_noncrypt);

					InfoCommercantDto current_infoCommercant = null;
					try {
						current_infoCommercant = infoCommercantService.findByCmrCode(demandeP.getComid());
					} catch (Exception e) {
						traces.writeInFileTransaction(folder, file,
								"authorization 500 InfoCommercant misconfigured in DB or not existing orderid:["
										+ demandeP.getCommande() + "] and merchantid:[" + demandeP.getComid() + "]"
										+ e);
					}

					if (current_infoCommercant == null) {
						traces.writeInFileTransaction(folder, file,
								"authorization 500 InfoCommercantDto misconfigured in DB or not existing orderid:["
										+ demandeP.getCommande() + "] and merchantid:[" + demandeP.getComid() + "]");
						response.sendRedirect(link_result);
					}

					String plainTxtSignature = demandeP.getCommande() + current_infoCommercant.getClePub();

					traces.writeInFileTransaction(folder, file, "plainTxtSignature : " + plainTxtSignature);
					System.out.println("plainTxtSignature : " + plainTxtSignature);

					String data = RSACrypto.encryptByPublicKeyWithMD5Sign(data_noncrypt,
							current_infoCommercant.getClePub(), plainTxtSignature, folder, file);

					traces.writeInFileTransaction(folder, file, "data encrypt : " + data);
					System.out.println("data encrypt : " + data);

					traces.writeInFileTransaction(folder, file,
							"TransStatus = N => Redirect to FailURL : " + demandeP.getFailURL());
					System.out.println("TransStatus = N => Redirect to FailURL : " + demandeP.getFailURL());
					response.sendRedirect(
							demandeP.getFailURL() + "?data=" + data + "==&codecmr=" + demandeP.getComid());
				} else {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .";
					response.sendRedirect(link_result);
				}
			}
		} catch (Exception ex) {
			traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> Exception " + ex);
			System.out.println("ACSController RETOUR ACS =====> Exception " + ex);
			response.sendRedirect(link_result);
		}
	}

	public String redirectFailURL(DemandePaiementDto demandePaiementDto, String folder, String file)
			throws IOException {
		traces.writeInFileTransaction(folder, file,
				"REDIRECT FAIL URL DEMANDE PAIEMENT {" + demandePaiementDto.getIddemande() + "} => " + "Commerçant: {"
						+ demandePaiementDto.getComid() + "} Commande: {" + demandePaiementDto.getCommande() + "}");
		String signedFailUrl;
		String idCommande = demandePaiementDto.getCommande();
		infoCommercantDto = infoCommercantService.findByCmrCode(demandePaiementDto.getComid());
		String md5Signature = Util.hachInMD5(idCommande + infoCommercantDto.getClePub());

		signedFailUrl = demandePaiementDto.getFailURL() + "?id_commande=" + demandePaiementDto.getCommande() + "&token="
				+ md5Signature;
		traces.writeInFileTransaction(folder, file, "FAIL URL Signed : " + signedFailUrl);
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

}
