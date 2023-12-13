package ma.m2m.gateway.controller;

import static ma.m2m.gateway.Utils.StringUtils.isNullOrEmpty;
import static ma.m2m.gateway.config.FlagActivation.ACTIVE;
import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	public String processRequestMobile(HttpServletRequest request, HttpServletResponse response, Model model)
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

			// just for test
			// cleanCres.setTransStatus("N");

			if (cleanCres.getTransStatus().equals("Y")) {
				System.out.println("ACSController RETOUR ACS =====> cleanCres TransStatus = Y ");
				Util.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> cleanCres TransStatus = Y ");

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
				String montantRechgtrame = "",cartenaps = "",dateExnaps = "";
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
					demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (MPI_KO), votre compte ne sera pas débité, merci de réessayer .");
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
					demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (DemandePaiement introuvable), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
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
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				}

				if (current_merchant == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				}

				if (current_merchant.getCmrCodactivite() == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				}

				if (current_merchant.getCmrCodbqe() == null) {
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
					demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
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
					Util.writeInFileTransaction(folder, file,
							"authorization 500 InfoCommercant misconfigured in DB or not existing orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]" + e);
					demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
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
					Util.writeInFileTransaction(folder, file,
							"authorization 500 Error during  date formatting for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err2);
					demandeDtoMsg.setMsgRefus("Erreur lors du formatage de la date");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
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
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
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
						amount = calculMontantTotalOperation(dmd);
						
						if(amount.contains(",")) {
							amount = amount.replace(",", ".");
						}
						if(!amount.contains(".") && !amount.contains(",")) {
							amount = amount +"."+"00";
						}
						System.out.println("montant recharge avec frais : [" + amount + "]");
						Util.writeInFileTransaction(folder, file, "montant recharge avec frais : [" + amount + "]");
						
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
						System.out.println("montanttrame avec frais : [" + montanttrame + "]");
						Util.writeInFileTransaction(folder, file, "montanttrame avec frais : [" + montanttrame + "]");
					} catch (Exception err3) {
						Util.writeInFileTransaction(folder, file,
								"authorization 500 Error during  amount formatting for given orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + err3);
						demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
						return page;
					}
					
					try {
						montantRechgtrame = "";

						mm = new String[2];
						String amount1 = calculMontantSansOperation(dmd);
						
						if(amount1.contains(",")) {
							amount1 = amount1.replace(",", ".");
						}
						if(!amount1.contains(".") && !amount1.contains(",")) {
							amount1 = amount1 +"."+"00";
						}
						System.out.println("montant recharge sans frais : [" + amount1 + "]");
						Util.writeInFileTransaction(folder, file, "montant recharge sans frais : [" + amount1 + "]");
						
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
						System.out.println("montantRechgtrame sans frais: [" + montantRechgtrame + "]");
						Util.writeInFileTransaction(folder, file, "montantRechgtrame sans frais : [" + montantRechgtrame + "]");
					} catch (Exception err3) {
						Util.writeInFileTransaction(folder, file,
								"recharger 500 Error during  amount formatting for given orderid:[" + orderid
										+ "] and merchantid:[" + merchantid + "]" + err3);
						demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
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
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error during switch tlv buildup for given orderid:[" + orderid
											+ "] and merchantid:[" + merchantid + "]" + err4);
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Erreur lors de la création du switch tlv), votre compte ne sera pas débité, merci de réessayer .");
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
							Util.writeInFileTransaction(folder, file, "authorization 500 Error Switch communication s_conn false switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
							demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
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
						Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);
						switch_ko = 1;
						demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
						return page;
					} catch (java.net.ConnectException e) {
						Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
						switch_ko = 1;
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Un dysfonctionnement du switch), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
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
							if(codeReponseDto != null) {
								s_status = codeReponseDto.getRpcLibelle();
							}		
						} catch(Exception ee) {
							Util.writeInFileTransaction(folder, file, "authorization 500 Error codeReponseDto null");
							ee.printStackTrace();
						}	
						
						Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

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
						if(websiteid.equals("")) {
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
									"2eme tentative : authorization 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + ex);
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

					} else {

						Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
						Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

						try {
							Util.writeInFileTransaction(folder, file,
									"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

							dmd.setEtat_demande("SW_REJET");
							demandePaiementService.save(dmd);

						} catch (Exception e) {
							Util.writeInFileTransaction(folder, file,
									"authorization 500 Error during  DemandePaiement update SW_REJET for given orderid:["
											+ orderid + "]" + e);
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer .");
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
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
								"authorization 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer .");
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
						String data_noncrypt = "id_commande=" + orderid + "&nomprenom=" + fname + "&email=" + email
								+ "&montant=" + montantSansFrais + "&frais=" + frais + "&repauto=" + coderep + "&numAuto="
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
							Util.writeInFileTransaction(folder, file,
									"coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
							System.out.println("coderep 00 => Redirect to SuccessURL : " + dmd.getSuccessURL());
							if(dmd.getSuccessURL() != null) {
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
								Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
								System.out.println("Fin processRequestMobile ()");
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
								if(codeReponseDto != null) {
									libelle = codeReponseDto.getRpcLibelle();
								}		
							} catch(Exception ee) {
								Util.writeInFileTransaction(folder, file, "payer 500 Error codeReponseDto null");
								ee.printStackTrace();
							}					
							demandeDtoMsg.setMsgRefus(
									"La transaction en cours n’a pas abouti (Error during response Switch coderep " + coderep + ":" + libelle +"),"
											+ " votre compte ne sera pas débité, merci de réessayer .");						
							model.addAttribute("demandeDto", demandeDtoMsg);
							page = "result";
							Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
							System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "link_chalenge " + link_chalenge + dmd.getTokencommande());

						System.out.println("autorization api response chalenge :  [" + jso.toString() + "]");
						Util.writeInFileTransaction(folder, file,
								"autorization api response chalenge :  [" + jso.toString() + "]");
						
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
						
						return jso.toString();
					} catch (Exception ex) {
						Util.writeInFileTransaction(folder, file, "authorization 500 Error during jso out processing " + ex);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
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
						demandePaiementService.save(dmd);
						demandeDtoMsg.setMsgRefus(
								"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
						System.out.println("Fin processRequestMobile ()");
						return page;
					}
				}

			} else {
				Util.writeInFileTransaction(folder, file,
						"ACSController RETOUR ACS =====> cleanCres TransStatus = N ");
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
						demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (InfoCommercant mal configuré dans la base de données ou inexistant), votre compte ne sera pas débité, merci de réessayer .");
						model.addAttribute("demandeDto", demandeDtoMsg);
						page = "result";
						Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
						System.out.println("Fin processRequestMobile ()");
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
					demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
					System.out.println("Fin processRequestMobile ()");
					return page;
				} else {
					msgRefus = "La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .";
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
			demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (TransStatus = N), votre compte ne sera pas débité, merci de réessayer .");
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
				return getMsgError(folder, file, null, "getLinkCCB 500 malformed header " + head_err.getMessage(), null);
			}
		}

		DemandePaiementDto dmd = null;
		DemandePaiementDto dmdSaved = null;
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;

		String orderid, amount, merchantid, merchantname, websiteName, websiteid, recurring, country, phone, city,
				state, zipcode, address, expirydate, transactiondate, transactiontime, callbackUrl, fname, lname,
				email = "", securtoken24, mac_value, successURL, failURL, idDemande,
				id_client, token, cartenaps, dateexpnaps;
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
			return getMsgError(folder, file, null, "getLinkCCB 500 malformed json expression " + jerr.getMessage(), null);
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"authorization 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + e);

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"getLinkCCB 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Merchant misconfigured in DB or not existing", "15");
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

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error Already exist in PaiementRequest", "16");
		}

		String url = "", status = "", statuscode = "";

		try {
			String tokencommande = "";
			if (check_dmd != null) {
				// generer token
				tokencommande = Util.genTokenCom(check_dmd.getCommande(), check_dmd.getComid());
				url = link_ccb + check_dmd.getTokencommande();
				statuscode = "00";
				status = "OK";
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
				if (amount.equals("") || amount == null) {
					amount = "0";
				}
				if (amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				dmd.setMontant(Double.parseDouble(amount));
				// calcule des frais de recharge
				Double montantrecharge = (0 + (Double.parseDouble(amount) * 0.65) / 100);
				String fraistr = String.format("%.2f", montantrecharge).replaceAll(",", ".");
				
				dmd.setFrais(Double.parseDouble(fraistr));
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
					"getLinkCCB 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]" + err1);

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error during DEMANDE_PAIEMENT insertion", null);
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
					"getLinkCCB 500 Error during jso out processing given orderid:[" + orderid + "]" + err8);

			return getMsgError(folder, file, jsonOrequest, "getLinkCCB 500 Error during jso out processing", null);
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin getLinkCCB() ************** ");
		System.out.println("*********** Fin getLinkCCB() ************** ");

		return jso.toString();

	}

	
	@RequestMapping(value = "/napspayment/authorization/ccb/token/{token}", method = RequestMethod.GET)
	public String showPageRchg(@PathVariable(value = "token") String token, Model model) {
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

		String page = "erecharge.html";

		try {
			demandeDto = demandePaiementService.findByTokencommande(token);

			if (demandeDto != null) {
				System.out.println("DemandePaiement is found idDemande/Commande : " + demandeDto.getIddemande() + "/" + demandeDto.getCommande());
				Util.writeInFileTransaction(folder, file,
						"DemandePaiement is found iddemande/Commande : " + demandeDto.getIddemande() + "/" + demandeDto.getCommande());

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
				if(idclient == null) {
					idclient="";
				}
				//merchantid = demandeDto.getComid();
				merchantid = "";
				String cardnumber = "";
				List<Cartes> cartes = new ArrayList<>();
				if (!idclient.equals("") && idclient != null && !idclient.equals("null")) {
					System.out.println("idclient/merchantid : " + idclient + "/" + merchantid);
					try {		
						List<CardtokenDto> cards = new ArrayList<>();
						cards = cardtokenService.findByIdMerchantAndIdMerchantClient(merchantid, idclient);
						if (cards != null && cards.size() > 0) {
							for(CardtokenDto card : cards) {
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
							"La transaction en cours n’a pas abouti (Opération déjà effectuée), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDto);
					page = "operationEffectue";
				} else if (demandeDto.getEtat_demande().equals("SW_REJET")) {
					Util.writeInFileTransaction(folder, file, "Transaction rejetée");
					demandeDto.setMsgRefus(
							"La transaction en cours n’a pas abouti (Transaction rejetée), votre compte ne sera pas débité, merci de réessayer .");
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
						demandeDto.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
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
						demandeDto.setMsgRefus("Galerie mal configuré dans la base de données ou inexistant");
						model.addAttribute("demandeDto", demandeDto);
						page = "result";
					}
				}
			} else {
				Util.writeInFileTransaction(folder, file, "demandeDto not found token : " + token);
				System.out.println("demandeDto not found token : " + token);
				demandeDto = new DemandePaiementDto();
				demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
				model.addAttribute("demandeDto", demandeDto);
				page = "result";
			}

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPageRchg 500 DEMANDE_PAIEMENT misconfigured in DB or not existing token:[" + token + "]"
							+ e);

			Util.writeInFileTransaction(folder, file, "showPageRchg 500 exception" + e);
			e.printStackTrace();
			demandeDto = new DemandePaiementDto();
			demandeDto.setMsgRefus("Demande paiement mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDto);
			page = "result";
		}
		
		if(page.equals("erecharge.html")) {
			demandeDto.setEtat_demande("P_CHRG_OK");
			demandePaiementService.save(demandeDto);
			System.out.println("update Demandepaiement status to P_CHRG_OK");
			Util.writeInFileTransaction(folder, file, "update Demandepaiement status to P_CHRG_OK");
		}

		Util.writeInFileTransaction(folder, file, "*********** Fin affichage page ccb ************** ");
		System.out.println("*********** Fin affichage page ccb ************** ");

		return page;
	}
	
	public String calculMontantTotalOperation(DemandePaiementDto dto) {
		double mnttotalopp = dto.getMontant() + dto.getFrais();
		String mntttopp = String.format("%.2f", mnttotalopp).replaceAll(",", ".");
		return mntttopp;
	}
	
	public String calculMontantSansOperation(DemandePaiementDto dto) {
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

	@PostMapping("/recharger")
	public String recharger(Model model, @ModelAttribute("demandeDto") DemandePaiementDto dto,
			HttpServletRequest request, HttpServletResponse response) {
		randomWithSplittableRandom = splittableRandom.nextInt(111111111, 999999999);
		String file = "MB_PAYE_" + randomWithSplittableRandom;
		// create file log
		Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "*********** Start recharger () ************** ");
		System.out.println("*********** Start recharger () ************** ");
		
		String capture, currency, orderid, recurring, amount, promoCode, transactionid, capture_id, merchantid,
				merchantname, websiteName, websiteid, callbackUrl, cardnumber, token, expirydate, holdername, cvv,
				fname, lname, email, country, phone, city, state, zipcode, address, mesg_type, merc_codeactivite,
				acqcode, merchant_name, merchant_city, acq_type, processing_code, reason_code, transaction_condition,
				transactiondate, transactiontime, date, rrn, heure, montanttrame, montantRechgtrame,cartenaps,dateExnaps, num_trs = "", successURL, failURL,
				transactiontype;

		DemandePaiementDto demandeDto = new DemandePaiementDto();
		Objects.copyProperties(demandeDto, dto);
		System.out.println("demandeDto commande : " + demandeDto.getCommande());
		Util.writeInFileTransaction(folder, file, "demandeDto commande : " + dto.getCommande());
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		DemandePaiementDto dmd = new DemandePaiementDto();
		
		SimpleDateFormat formatter_1, formatter_2, formatheure, formatdate = null;
		Date trsdate = null;
		Integer Idmd_id = null;
		String[] mm;
		String[] m;

		String page = "chalenge";
		try {
			// Transaction info
			orderid = demandeDto.getCommande();
			if(demandeDto.getMontant() == null) {
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
			if(demandeDto.getDem_pan() != null) {
				cardnumber = demandeDto.getDem_pan();
				expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois());
			}
			// if transaction cof
			if(demandeDto.getDem_pan() == null && demandeDto.getInfoCarte() != null) {
				String infoCard = demandeDto.getInfoCarte().substring(8, demandeDto.getInfoCarte().length());
				Cartes carteFormated = fromString(infoCard);
				demandeDto.setCarte(carteFormated);
				cardnumber = demandeDto.getCarte().getCarte();
				//expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois());
				String annee = String.valueOf(demandeDto.getCarte().getYear());
				expirydate = annee.substring(2, 4).concat(demandeDto.getCarte().getMoisValue());
			}
			//cardnumber = demandeDto.getDem_pan();
			token = "";
			//expirydate = demandeDto.getAnnee().substring(2, 4).concat(demandeDto.getMois());
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
			demandeDtoMsg.setMsgRefus("données mal formées");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		CommercantDto current_merchant = null;
		try {
			current_merchant = commercantService.findByCmrNumcmr(merchantid);
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]" + e);
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodactivite() == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_merchant.getCmrCodbqe() == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid + "] and merchantid:["
							+ merchantid + "]");
			demandeDtoMsg.setMsgRefus("Commerçant mal configuré dans la base de données ou inexistant");
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

			demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (current_infoCommercant == null) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 InfoCommercantDto misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");

			demandeDtoMsg.setMsgRefus("InfoCommercant mal configuré dans la base de données ou inexistant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		int i_card_valid = Util.isCardValid(cardnumber);

		if (i_card_valid == 1) {
			Util.writeInFileTransaction(folder, file, "recharger 500 Card number length is incorrect orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("La longueur du numéro de la carte est incorrecte");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		if (i_card_valid == 2) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Card number  is not valid incorrect luhn check orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]");
			demandeDtoMsg.setMsgRefus("Le numéro de la carte est invalide");
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
			//dmdToEdit.setDateexpnaps(expirydate);
			dmdToEdit.setTransactiontype(transactiontype);

			formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
			formatter_2 = new SimpleDateFormat("HH:mm:ss");
			trsdate = new Date();
			transactiondate = formatter_1.format(trsdate);
			transactiontime = formatter_2.format(trsdate);
			dmdToEdit.setDem_date_time(dateFormat.format(new Date()));

			demandeDto = demandePaiementService.save(dmdToEdit);
			demandeDto.setExpery(expirydate);

		} catch (Exception err1) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Error during DEMANDE_PAIEMENT insertion for given orderid:[" + orderid + "]"
							+ err1);
			demandeDtoMsg.setMsgRefus("Erreur lors de l'insertion DEMANDE_PAIEMENT");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		
		// for test control risk
		GWRiskAnalysis riskAnalysis = new GWRiskAnalysis(folder, file);
		try {
			ControlRiskCmrDto controlRiskCmr = controlRiskCmrService.findByNumCommercant(demandeDto.getComid());
			List<HistoAutoGateDto> porteurFlowPerDay = null;
			
			Double globalFlowPerDay = 0.00;
			List<EmetteurDto> listBin = null;

			if (controlRiskCmr != null) {
				/* --------------------------------- Controle des cartes internationales -----------------------------------------*/
				if(isNullOrEmpty(controlRiskCmr.getAcceptInternational()) || (controlRiskCmr.getAcceptInternational() != null
					&& !ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getAcceptInternational().trim()))) {
					String binDebutCarte = cardnumber.substring(0, 9);
					//binDebutCarte = binDebutCarte+"000";
					Util.writeInFileTransaction(folder, file, "controlRiskCmr ici 1");
					listBin = emetteurService.findByBindebut(binDebutCarte);
				}		
				// --------------------------------- Controle de flux journalier autorisé par commerçant  ----------------------------------
				if(!isNullOrEmpty(controlRiskCmr.getIsGlobalFlowControlActive()) && ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getIsGlobalFlowControlActive())) {
					Util.writeInFileTransaction(folder, file, "controlRiskCmr ici 2");
					globalFlowPerDay = histoAutoGateService.getCommercantGlobalFlowPerDay(merchantid);
			 	}
				// ------------------------- Controle de flux journalier autorisé par client (porteur de carte) ----------------------------
				if((controlRiskCmr.getFlowCardPerDay() != null && controlRiskCmr.getFlowCardPerDay() > 0) 
						|| (controlRiskCmr.getNumberOfTransactionCardPerDay() != null && controlRiskCmr.getNumberOfTransactionCardPerDay() > 0)) {
					Util.writeInFileTransaction(folder, file, "controlRiskCmr ici 3");
					porteurFlowPerDay = histoAutoGateService.getPorteurMerchantFlowPerDay(demandeDto.getComid(),
							demandeDto.getDem_pan());
				}
			}
			String msg = riskAnalysis.executeRiskControls(demandeDto.getComid(), demandeDto.getMontant(),
					demandeDto.getDem_pan(), controlRiskCmr, globalFlowPerDay, porteurFlowPerDay, listBin);
			
			if(!msg.equalsIgnoreCase("OK")) {
				demandeDto.setEtat_demande("REJET_RISK_CTRL");
				demandePaiementService.save(demandeDto);
				Util.writeInFileTransaction(folder, file, "recharger 500 Error " + msg);
				demandeDto = new DemandePaiementDto();
				demandeDtoMsg.setMsgRefus(msg);
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}
			// fin control risk
		} catch (Exception e) {
			demandeDto.setEtat_demande("REJET_RISK_CTRL");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"recharger 500 ControlRiskCmr misconfigured in DB or not existing merchantid:[" + demandeDto.getComid() + e);
			demandeDto = new DemandePaiementDto();
			demandeDtoMsg.setMsgRefus("Error 500 Opération rejetée: Contrôle risque");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		
		try {

			formatheure = new SimpleDateFormat("HHmmss");
			formatdate = new SimpleDateFormat("ddMMyy");
			date = formatdate.format(new Date());
			heure = formatheure.format(new Date());
			rrn = Util.getGeneratedRRN();
		} catch (Exception err2) {
			Util.writeInFileTransaction(folder, file,
					"recharger 500 Error during  date formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + err2);
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage de la date");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}

		JSONObject jso = new JSONObject();

		// appel 3DSSecure ***********************************************************

		ThreeDSecureResponse threeDsecureResponse = autorisationService.preparerReqMobileThree3DSS(demandeDto, folder, file);
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
			Util.writeInFileTransaction(folder, file, "received idDemande from MPI is Null or Empty");
			demandeDto.setEtat_demande("MPI_KO");
			demandePaiementService.save(demandeDto);
			Util.writeInFileTransaction(folder, file,
					"demandePaiement after update MPI_KO idDemande null : " + demandeDto.toString());
			demandeDtoMsg.setMsgRefus(
					"La transaction en cours n’a pas abouti (MPI_KO), votre compte ne sera pas débité, merci de réessayer .");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
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
			return page;
		}

		if (reponseMPI.equals("Y")) {
			// ********************* Frictionless responseMPI equal Y *********************
			Util.writeInFileTransaction(folder, file,
					"********************* Cas frictionless responseMPI equal Y *********************");

			dmd.setDem_xid(threeDSServerTransID);
			demandePaiementService.save(dmd);
			
			cartenaps = dmd.getCartenaps();
			dateExnaps = dmd.getDateexpnaps();
			
			try {
				montanttrame = "";

				mm = new String[2];
				
				amount = calculMontantTotalOperation(dmd);
				
				if(amount.contains(",")) {
					amount = amount.replace(",", ".");
				}
				if(!amount.contains(".") && !amount.contains(",")) {
					amount = amount +"."+"00";
				}
				System.out.println("montant recharge avec frais : [" + amount + "]");
				Util.writeInFileTransaction(folder, file, "montant recharge avec frais : [" + amount + "]");
				
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
				System.out.println("montanttrame : [" + montanttrame +"]");
				Util.writeInFileTransaction(folder, file, "montanttrame : [" + montanttrame + "]");
			} catch (Exception err3) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during  amount formatting for given orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err3);
				demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}
			
			try {
				montantRechgtrame = "";

				mm = new String[2];
				String amount1 = calculMontantSansOperation(dmd);
				
				if(amount1.contains(",")) {
					amount1 = amount1.replace(",", ".");
				}
				if(!amount1.contains(".") && !amount1.contains(",")) {
					amount1 = amount1 +"."+"00";
				}
				System.out.println("montant recharge sans frais : [" + amount1 + "]");
				Util.writeInFileTransaction(folder, file, "montant recharge sans frais : [" + amount1 + "]");
				
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
				System.out.println("montantRechgtrame : [" + montantRechgtrame + "]");
				Util.writeInFileTransaction(folder, file, "montantRechgtrame : [" + montantRechgtrame + "]");
			} catch (Exception err3) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during  amount formatting for given orderid:[" + orderid
								+ "] and merchantid:[" + merchantid + "]" + err3);
				demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			merchantname = current_merchant.getCmrNom();
			websiteName = "";
			websiteid = "";
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
				Util.writeInFileTransaction(folder, file,
						"recharger 500 cvv not set , reccuring flag set to N, cvv must be present in normal transaction");

				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (cvv doit être présent dans la transaction normale), votre compte ne sera pas débité, merci de réessayer .");
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
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during switch tlv buildup for given orderid:[" + orderid
									+ "] and merchantid:[" + merchantid + "]" + err4);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la création du switch tlv), votre compte ne sera pas débité, merci de réessayer .");
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
					Util.writeInFileTransaction(folder, file, "Switch  malfunction cannot connect!!!");

					Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch communication s_conn false switch ip:[" + sw_s
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
				Util.writeInFileTransaction(folder, file, "Switch  malfunction UnknownHostException !!!" + e);

				demandeDtoMsg.setMsgRefus("Un dysfonctionnement du switch ne peut pas se connecter !!!");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;

			} catch (java.net.ConnectException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction ConnectException !!!" + e);
				switch_ko = 1;
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Un dysfonctionnement du switch), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (SocketTimeoutException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction  SocketTimeoutException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error Switch communication SocketTimeoutException" + "switch ip:[" + sw_s
								+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur de communication du switch SocketTimeoutException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			catch (IOException e) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction IOException !!!" + e);
				switch_ko = 1;
				e.printStackTrace();
				Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch communication IOException"
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur de communication du switch IOException), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
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
				return page;
			}

			String resp = resp_tlv;

			if (switch_ko == 0 && resp == null) {
				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp null!!!");
				switch_ko = 1;
				Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch null response" + "switch ip:["
						+ sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp null), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			if (switch_ko == 0 && resp.length() < 3) {
				switch_ko = 1;

				Util.writeInFileTransaction(folder, file, "Switch  malfunction resp < 3 !!!");
				Util.writeInFileTransaction(folder, file, "recharger 500 Error Switch short response length() < 3 "
						+ "switch ip:[" + sw_s + "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Dysfonctionnement du switch resp < 3 !!!), votre compte ne sera pas débité, merci de réessayer .");
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
							"recharger 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
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
							"recharger 500 Error during tlv Switch response parse tag1_resp tag null" + "switch ip:[" + sw_s
									+ "] and switch port:[" + port + "] resp_tlv : [" + resp_tlv + "]");
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
					if(codeReponseDto != null) {
						s_status = codeReponseDto.getRpcLibelle();
					}		
				} catch(Exception ee) {
					Util.writeInFileTransaction(folder, file, "recharger 500 Error codeReponseDto null");
					ee.printStackTrace();
				}	
				
				Util.writeInFileTransaction(folder, file, "get status Switch status : [" + s_status + "]");

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
				if(websiteid.equals("")) {
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

				histoAutoGateService.save(hist);

			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + e);
				try {
					Util.writeInFileTransaction(folder, file, "2eme tentative : HistoAutoGate Saving ... ");
					histoAutoGateService.save(hist);
				} catch (Exception ex) {
					Util.writeInFileTransaction(folder, file,
							"2eme tentative : recharger 500 Error during  insert in histoautogate for given orderid:[" + orderid + "]" + ex);
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
							"recharger 500 Error during DEMANDE_PAIEMENT update etat demande for given orderid:[" + orderid
									+ "]" + e);
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
							Util.writeInFileTransaction(folder, file, "inserting into telec ko..do nothing " + e);
						}
					}
					if (capture_status.equalsIgnoreCase("Y") && exp_flag == 1)
						capture_status.equalsIgnoreCase("N");

					Util.writeInFileTransaction(folder, file, "Automatic capture end.");
				}

			} else {

				Util.writeInFileTransaction(folder, file, "transaction declined !!! ");
				Util.writeInFileTransaction(folder, file, "SWITCH RESONSE CODE :[" + tag20_resp + "]");

				try {
					Util.writeInFileTransaction(folder, file,
							"transaction declinded ==> update Demandepaiement status to SW_REJET ...");

					dmd.setEtat_demande("SW_REJET");
					demandePaiementService.save(dmd);
					
				} catch (Exception e) {
					Util.writeInFileTransaction(folder, file,
							"recharger 500 Error during  DemandePaiement update SW_REJET for given orderid:[" + orderid + "]"
									+ e);
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Erreur lors de la mise à jour de DemandePaiement SW_REJET), votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
					return page;
				}
				Util.writeInFileTransaction(folder, file, "update Demandepaiement status to RE OK.");
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
						"La transaction en cours n’a pas abouti (Erreur lors de la génération de l'ID de paiement), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				return page;
			}

			Util.writeInFileTransaction(folder, file, "Generating paymentid OK");
			Util.writeInFileTransaction(folder, file, "paymentid :[" + paymentid + "]");

			// JSONObject jso = new JSONObject();

			Util.writeInFileTransaction(folder, file, "Preparing autorization api response");

			String authnumber, coderep, motif, merchnatidauth, dtdem = "", frais="", montantSansFrais ="";

			try {
				authnumber = hist.getHatNautemt();
				coderep = hist.getHatCoderep();
				motif = hist.getHatMtfref1();
				merchnatidauth = hist.getHatNumcmr();
				dtdem = dmd.getDem_pan();
				montantSansFrais = String.valueOf(dmd.getMontant());
				frais = String.valueOf(dmd.getFrais());
				Util.writeInFileTransaction(folder, file, "frais :[" + frais + "]");
			} catch (Exception e) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during authdata preparation orderid:[" + orderid + "]" + e);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors de la préparation des données d'authentification), votre compte ne sera pas débité, merci de réessayer .");
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
						+ authnumber + "&numCarte=" + Util.formatCard(cardnumber) + "&typecarte="
						+ dmd.getType_carte() + "&numTrans=" + transactionid;

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
					if(dmd.getSuccessURL() != null) {
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
						if(codeReponseDto != null) {
							libelle = codeReponseDto.getRpcLibelle();
						}		
					} catch(Exception ee) {
						Util.writeInFileTransaction(folder, file, "recharger 500 Error codeReponseDto null");
						ee.printStackTrace();
					}					
					demandeDtoMsg.setMsgRefus(
							"La transaction en cours n’a pas abouti (Error during response Switch coderep " + coderep + ":" + libelle +"),"
									+ " votre compte ne sera pas débité, merci de réessayer .");
					model.addAttribute("demandeDto", demandeDtoMsg);
					page = "result";
				}
			} catch (Exception jsouterr) {
				Util.writeInFileTransaction(folder, file,
						"recharger 500 Error during jso out processing given authnumber:[" + authnumber + "]" + jsouterr);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
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

				// insertion htmlCreq dans la demandePaiement
				// dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				//dmd.setCreq(
				//		"<form  action='https://acs2.sgmaroc.com:443/lacs2' method='post' enctype='application/x-www-form-urlencoded'><input type='hidden' name='creq' value='ewogICJtZXNzYWdlVmVyc2lvbiI6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIjogIjBlYmU1ODEwLTlhMDMtNGYzZi05MDgzLTJlZWNhNjhiMjY2YSIsCiAgImFjc1RyYW5zSUQiOiAiMmM5MjAxNDgtNjhiOC00ZjA0LWJhODQtY2RiYTFlOTM5MDM3IiwKICAiY2hhbGxlbmdlV2luZG93U2l6ZSI6ICIwNSIsCiAgIm1lc3NhZ2VUeXBlIjogIkNSZXEiCn0=' /></form>");
				dmd.setCreq(threeDsecureResponse.getHtmlCreq());
				dmd.setDem_xid(threeDSServerTransID);
				dmd.setEtat_demande("SND_TO_ACS");
				demandeDto = demandePaiementService.save(dmd);
				model.addAttribute("demandeDto", demandeDto);
				page = "chalenge";
				
				Util.writeInFileTransaction(folder, file, "set demandeDto model creq : " + demandeDto.getCreq());
				Util.writeInFileTransaction(folder, file, "return page : " + page);
				
				//return page;
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file, "recharger 500 Error during jso out processing " + ex);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (Erreur lors du traitement de sortie JSON), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
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
				dmd.setEtat_demande("MPI_CMR_INEX");
				demandePaiementService.save(dmd);
				demandeDtoMsg.setMsgRefus(
						"La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer .");
				model.addAttribute("demandeDto", demandeDtoMsg);
				page = "result";
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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
				System.out.println("Fin processRequestMobile ()");
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
				Util.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
				System.out.println("Fin processRequestMobile ()");
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

	public String getMsgError(String folder, String file, JSONObject jsonOrequest, String msg, String coderep) {
		Traces traces = new Traces();
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
		        //String formattedMonth = mapToFrenchMonth(month);
		        String moisStr = String.format("%s", mois);
		        List<String> list = new ArrayList<>();
		        list.add(moisStr);
		        MonthDto month = mapToFrenchMonth(moisStr);
		        carte.setMois(month.getMonth());
		        carte.setMoisValue(month.getValue());
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
