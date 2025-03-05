package ma.m2m.gateway.service;

import static ma.m2m.gateway.config.FlagActivation.ACTIVE;
import static ma.m2m.gateway.utils.StringUtils.isNullOrEmpty;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.dto.Cartes;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.model.Commercant;
import ma.m2m.gateway.model.Galerie;
import ma.m2m.gateway.repository.CommercantDao;
import ma.m2m.gateway.repository.GalerieDao;
import ma.m2m.gateway.risk.GWRiskAnalysis;
import ma.m2m.gateway.threedsecure.AuthInitRequest;
import ma.m2m.gateway.threedsecure.ThreeDSecureRequestor;
import ma.m2m.gateway.threedsecure.ThreeDSecureRequestorException;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import ma.m2m.gateway.utils.Util;
import org.springframework.ui.Model;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class AutorisationServiceImpl implements AutorisationService {

	private static final Logger logger = LogManager.getLogger(AutorisationServiceImpl.class);

	private final Gson gson;

	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDssV;

	@Value("${key.LIEN_3DSS_M}")
	private String urlThreeDssM;

	@Value("${key.LIEN_NOTIFICATION_ACS}")
	private String notificationACS;

	@Value("${key.LIEN_NOTIFICATION_CCB_ACS}")
	private String notificationCCBACS;

	@Value("${key.LIEN_NOTIFICATION_PROCESSOUT_ACS}")
	private String notificationProcessOutACS;

	//@Autowired
	private final ControlRiskCmrService controlRiskCmrService;

	//@Autowired
	private final EmetteurService emetteurService;

	//@Autowired
	private final HistoAutoGateService histoAutoGateService;

	//@Autowired
	private final InfoCommercantService infoCommercantService;

	//@Autowired
	private final GalerieDao galerieDao;

	//@Autowired
	private final CommercantDao commercantDao;

	//@Autowired
	private final CardtokenService cardtokenService;

	//@Autowired
	private final CommercantService commercantService;

	//@Autowired
	private final GalerieService galerieService;

	//@Autowired
	private final DemandePaiementService demandePaiementService;

	private String typeCarte;

	private Commercant commercant = new Commercant();
	private InfoCommercantDto infoCommercantDto = new InfoCommercantDto();

	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

	public AutorisationServiceImpl(HistoAutoGateService histoAutoGateService, CommercantDao commercantDao,
								   InfoCommercantService infoCommercantService, GalerieDao galerieDao,
								   EmetteurService emetteurService, ControlRiskCmrService controlRiskCmrService,
								   CardtokenService cardtokenService, CommercantService commercantService,
								   GalerieService galerieService, DemandePaiementService demandePaiementService) {
		this.gson = new GsonBuilder().serializeNulls().create();
		this.histoAutoGateService = histoAutoGateService;
		this.commercantDao = commercantDao;
		this.infoCommercantService = infoCommercantService;
		this.galerieDao = galerieDao;
		this.emetteurService = emetteurService;
		this.controlRiskCmrService = controlRiskCmrService;
		this.cardtokenService = cardtokenService;
		this.commercantService = commercantService;
		this.galerieService = galerieService;
		this.demandePaiementService = demandePaiementService;
	}

	@Override
	@SuppressWarnings("all")
	public String controllerDataRequest(DemandePaiementDto demandeDto) {
		String message = "";

		if (demandeDto.getComid().equals("")) {
			message = "Commerçant inexistant dans la demande";
		} else if (demandeDto.getCommande().equals("")) {
			message = "Commande inexistant dans la demande";
		} else if (demandeDto.getMontant() <= 0) {
			message = "Montant inferieur ou égale à 0";
		}
		commercant = commercantDao.findByCmrCode(demandeDto.getComid());

		if (commercant == null) {
			message = "Commerçant inexistant dans la BD";
		} else if (commercant.getCmrEtat() == "0") {
			message = "L'état du commérçant est 0";
		}

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());

		if (infoCommercantDto == null) {
			message = "InfoCommerçant inexistant dans la BD";
		} else if (infoCommercantDto.getCmrCurrency() == null) {
			message = "La devise n'est pas renseigne dans la table INFO_COMMERCANT";
		}

		Galerie galerie = galerieDao.findByCodeGalAndCodeCmr(demandeDto.getGalid(), demandeDto.getComid());

		if (galerie == null || galerie.getEtat() == "N") {
			message = "La galerie inexistant dans la BD ou l'etat égale à N";
		} else if (galerie.getDateActivation() == null) {
			message = "La galerie inactive";
		}

		return message;
	}

	@Override
	public ThreeDSecureResponse autoriser(ThreeDSecureResponse reponse, String folder, String file) {

		ThreeDSecureResponse threeDSecureResponse = new ThreeDSecureResponse();

		return threeDSecureResponse;
	}

	@Override
	@SuppressWarnings("all")
	public ThreeDSecureResponse preparerReqThree3DSS(DemandePaiementDto demandeDto, String folder, String file) {
		Util.writeInFileTransaction(folder, file, "Debut preparerReqThree3DSS()");
		logger.info("Start preparerReqThree3DSS()");

		typeCarte = demandeDto.getTypeCarte();

		ThreeDSecureRequestor threeDSecureRequestor = new ThreeDSecureRequestor(folder, file);
		AuthInitRequest authInitRequest = new AuthInitRequest();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());

		if (typeCarte.equals("2")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 2 => Master Card ");
			logger.info("typeCarte 2 => Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDssM);
		} else if (typeCarte.equals("1")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 1 => Visa ");
			logger.info("typeCarte 1 => Visa ");
			authInitRequest.setUrlThreeDSS(urlThreeDssV);
		} else {
			Util.writeInFileTransaction(folder, file, "typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			logger.info("typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDssM);
		}

		authInitRequest.setPan(demandeDto.getDemPan());
		authInitRequest.setAmount(demandeDto.getMontant());
		authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setIdCommercant(demandeDto.getComid());
		authInitRequest.setIdDemande(demandeDto.getIddemande());
		authInitRequest.setExpiry(demandeDto.getExpery());
		// authInitRequest.setAcquirerBIN("11010");
		authInitRequest.setBrowserAcceptHeader("test");
		authInitRequest.setBrowserUserAgent("test");
		if (demandeDto.getEmail() == null || demandeDto.getEmail().equals("")) {
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
		} else {
			authInitRequest.setEmail(demandeDto.getEmail());
		}
		authInitRequest.setMcc(commercant.getCmrCodactivite());
		authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());
		authInitRequest.setNotificationURL(notificationACS);

		threeDSecureRequestor.threeDSecureRequest(authInitRequest);

		try {
			threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
		} catch (ThreeDSecureRequestorException e) {
			Util.writeInFileTransaction(folder, file, "ThreeDSecureRequestorException " + Util.formatException(e));
		}

		Util.writeInFileTransaction(folder, file, "fin preparerReqThree3DSS ");

		return threeDsecureResponse;
	}

	@Override
	@SuppressWarnings("all")
	public ThreeDSecureResponse preparerReqMobileThree3DSS(DemandePaiementDto demandeDto, String folder, String file) {
		Util.writeInFileTransaction(folder, file, "Debut preparerReqMobileThree3DSS()");
		logger.info("Start preparerReqMobileThree3DSS()");

		typeCarte = demandeDto.getTypeCarte();

		ThreeDSecureRequestor threeDSecureRequestor = new ThreeDSecureRequestor(folder, file);
		AuthInitRequest authInitRequest = new AuthInitRequest();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());

		if (typeCarte.equals("2")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 2 => Master Card ");
			logger.info("typeCarte 2 => Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDssM);
		} else if (typeCarte.equals("1")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 1 => Visa ");
			logger.info("typeCarte 1 => Visa ");
			authInitRequest.setUrlThreeDSS(urlThreeDssV);
		} else {
			Util.writeInFileTransaction(folder, file, "typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			logger.info("typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDssM);
		}
		// calcule du montant avec les frais
		String montantTotal = calculMontantTotalOperation(demandeDto);
		authInitRequest.setPan(demandeDto.getDemPan());
		authInitRequest.setAmount(Double.valueOf(montantTotal == null ? "" : montantTotal));
		authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setIdCommercant(demandeDto.getComid());
		authInitRequest.setIdDemande(demandeDto.getIddemande());
		authInitRequest.setExpiry(demandeDto.getExpery());
		// authInitRequest.setAcquirerBIN("11010");
		authInitRequest.setBrowserAcceptHeader("test");
		authInitRequest.setBrowserUserAgent("test");
		if (demandeDto.getEmail() == null || demandeDto.getEmail().equals("")) {
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
		} else {
			authInitRequest.setEmail(demandeDto.getEmail());
		}
		authInitRequest.setMcc(commercant.getCmrCodactivite());
		authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());
		authInitRequest.setNotificationURL(notificationCCBACS);

		threeDSecureRequestor.threeDSecureRequest(authInitRequest);

		try {
			threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
		} catch (ThreeDSecureRequestorException e) {
			Util.writeInFileTransaction(folder, file, "ThreeDSecureRequestorException " + Util.formatException(e));
		}

		Util.writeInFileTransaction(folder, file, "fin preparerReqMobileThree3DSS ");
		logger.info("Fin preparerReqMobileThree3DSS()");

		return threeDsecureResponse;
	}

	@Override
	@SuppressWarnings("all")
	public ThreeDSecureResponse preparerProcessOutReqThree3DSS(DemandePaiementDto demandeDto, String folder, String file) {
		Util.writeInFileTransaction(folder, file, "Debut preparerProcessOutReqThree3DSS()");
		logger.info("Start preparerProcessOutReqThree3DSS()");

		typeCarte = demandeDto.getTypeCarte();

		ThreeDSecureRequestor threeDSecureRequestor = new ThreeDSecureRequestor(folder, file);
		AuthInitRequest authInitRequest = new AuthInitRequest();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());

		if (typeCarte.equals("2")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 2 => Master Card ");
			logger.info("typeCarte 2 => Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDssM);
		} else if (typeCarte.equals("1")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 1 => Visa ");
			logger.info("typeCarte 1 => Visa ");
			authInitRequest.setUrlThreeDSS(urlThreeDssV);
		} else {
			Util.writeInFileTransaction(folder, file, "typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			logger.info("typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDssM);
		}

		authInitRequest.setPan(demandeDto.getDemPan());
		authInitRequest.setAmount(demandeDto.getMontant());
		authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setIdCommercant(demandeDto.getComid());
		authInitRequest.setIdDemande(demandeDto.getIddemande());
		authInitRequest.setExpiry(demandeDto.getExpery());
		// authInitRequest.setAcquirerBIN("11010");
		authInitRequest.setBrowserAcceptHeader("test");
		authInitRequest.setBrowserUserAgent("test");
		if (demandeDto.getEmail() == null || demandeDto.getEmail().equals("")) {
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
		} else {
			authInitRequest.setEmail(demandeDto.getEmail());
		}
		authInitRequest.setMcc(commercant.getCmrCodactivite());
		authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());
		authInitRequest.setNotificationURL(notificationProcessOutACS);

		threeDSecureRequestor.threeDSecureRequest(authInitRequest);

		try {
			threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
		} catch (ThreeDSecureRequestorException e) {
			Util.writeInFileTransaction(folder, file, "ThreeDSecureRequestorException " + Util.formatException(e));
		}

		Util.writeInFileTransaction(folder, file, "fin preparerProcessOutReqThree3DSS ");

		return threeDsecureResponse;
	}

	@Override
	@SuppressWarnings("all")
	public ThreeDSecureResponse callThree3DSSAfterACS(String decodedCres, String folder, String file) {
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		Util.writeInFileTransaction(folder, file, "*********** DEBUT callThree3DSSAfterACS ***********");

		// soit visa soit mastercard il a aucun impact apres auth
		Util.writeInFileTransaction(folder, file, "UrlThreeDSS : " + urlThreeDssM);

		logger.info("UrlThreeDSS : " + urlThreeDssM);
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			httpClient = getAllSSLClient();
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-KeyManagementException] " + e1);
		}

		HttpPost httpPost = new HttpPost(urlThreeDssM);

		final StringEntity entity = new StringEntity(decodedCres, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {
			HttpResponse responseTheeDs = httpClient.execute(httpPost);
			StatusLine responseStatusLine = responseTheeDs.getStatusLine();
			Util.writeInFileTransaction(folder, file, "Response StatusCode : " + responseStatusLine.getStatusCode());

			String responseStr = EntityUtils.toString(responseTheeDs.getEntity());

			Util.writeInFileTransaction(folder, file, "Rreq String : " + responseStr);
			logger.info("Response String : " + responseStr);

			threeDsecureResponse = gson.fromJson(responseStr, ThreeDSecureResponse.class);

			Util.writeInFileTransaction(folder, file, "" + threeDsecureResponse);

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-ClientProtocolException] " + Util.formatException(e));
		}

		Util.writeInFileTransaction(folder, file, "*********** End callThree3DSSAfterACS ***********");
		return threeDsecureResponse;
	}

	@Override
	@SuppressWarnings("all")
	public String controlleRisk(DemandePaiementDto demandeDto, String folder, String file) {
		GWRiskAnalysis riskAnalysis = new GWRiskAnalysis(folder, file);
		String msg = "OK";
		String cardnumber = demandeDto.getDemPan();
		try {
			ControlRiskCmrDto controlRiskCmr = controlRiskCmrService.findByNumCommercant(demandeDto.getComid());
			List<HistoAutoGateDto> porteurFlowPerDay = null;

			Double globalFlowPerDay = 0.00;
			List<EmetteurDto> listBin = null;

			if (controlRiskCmr != null) {
				// -------- Controle des cartes internationales --------
				if (isNullOrEmpty(controlRiskCmr.getAcceptInternational())
						|| (controlRiskCmr.getAcceptInternational() != null && !ACTIVE.getFlag()
						.equalsIgnoreCase(controlRiskCmr.getAcceptInternational().trim()))) {
					String binDebutCarte = cardnumber.substring(0, 9);
					// binDebutCarte = binDebutCarte + "000";

					listBin = emetteurService.findByBindebut(binDebutCarte);
				}
				// -------- Controle de flux journalier autorisé par commerçant --------
				if (!isNullOrEmpty(controlRiskCmr.getIsGlobalFlowControlActive())
						&& ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getIsGlobalFlowControlActive())) {
					globalFlowPerDay = histoAutoGateService.getCommercantGlobalFlowPerDay(demandeDto.getComid());
				}
				// -------- Controle de flux journalier autorisé par client (porteur de carte) --------
				if ((controlRiskCmr.getFlowCardPerDay() != null && controlRiskCmr.getFlowCardPerDay() > 0)
						|| (controlRiskCmr.getNumberOfTransactionCardPerDay() != null
						&& controlRiskCmr.getNumberOfTransactionCardPerDay() > 0)) {
					porteurFlowPerDay = histoAutoGateService.getPorteurMerchantFlowPerDay(demandeDto.getComid(),
							demandeDto.getDemPan());
				}
			}
			msg = riskAnalysis.executeRiskControls(demandeDto.getComid(), demandeDto.getMontant(),
					demandeDto.getDemPan(), controlRiskCmr, globalFlowPerDay, porteurFlowPerDay, listBin);

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"ControlRiskCmr misconfigured in DB or not existing merchantid:["
							+ demandeDto.getComid() + Util.formatException(e));
			msg = "KO";
		}

		return msg;
	}

	public String calculMontantTotalOperation(DemandePaiementDto dto) {
		if (dto.getMontant() == null) {
			dto.setMontant(0.00);
		}
		if (dto.getFrais() == null) {
			dto.setFrais(0.00);
		}
		double mnttotalopp = dto.getMontant() + dto.getFrais();
		return String.format("%.2f", mnttotalopp).replace(",", ".");
	}

	@SuppressWarnings("all")
	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"})
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				// Suppression intentionnelle : La validation des certificats est désactivée par choix
			}

			@SuppressWarnings({"squid:S4830", "Depreciated"})
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				// Suppression intentionnelle : La validation des certificats est désactivée par choix
			}
		}};
		// SSLContext context = SSLContext.getInstance("SSL");
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, trustAllCerts, null);

		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(context,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		builder.setSSLSocketFactory(sslConnectionFactory);

		PlainConnectionSocketFactory plainConnectionSocketFactory = new PlainConnectionSocketFactory();

		return builder.build();

	}

	@Override
	public void processPaymentPageData(DemandePaiementDto demandeDto, String folder, String file) {
		// TODO: if cmr don't accept transaction cof demandeDto.getIs_cof() = N don't show
		// TODO: carte
		// Gestion de l'acceptation ou non des transactions COF
		if (demandeDto.getIsCof() == null || demandeDto.getIsCof().equals("N")) {
			demandeDto.setDemPan("");
			demandeDto.setDemCvv("");
		}

		// Validation et nettoyage des tokens et ID client
		if (demandeDto.getToken() != null && (demandeDto.getToken().equals("") || demandeDto.getToken().equals(" "))) {
			demandeDto.setToken(null);
		}
		if (demandeDto.getIdClient() != null && (demandeDto.getIdClient().equals("") || demandeDto.getIdClient().equals(" "))) {
			demandeDto.setIdClient(null);
		}

		// Initialisation des variables
		String idClient = demandeDto.getIdClient() != null ? demandeDto.getIdClient() : "";
		String merchantId = demandeDto.getComid();
		List<Cartes> cartes = new ArrayList<>();

		// Récupération des cartes associées au client
		if (!idClient.isEmpty() && !"null".equals(idClient)) {
			logger.info("idclient/merchantid : " + idClient + "/" + merchantId);
			try {
				List<CardtokenDto> cards = cardtokenService.findByIdMerchantAndIdMerchantClient(merchantId, idClient);
				if (cards != null && !cards.isEmpty()) {
					for (CardtokenDto card : cards) {
						if (card.getCardNumber() != null) {
							Cartes carte = new Cartes();
							String cardNumber = card.getCardNumber();
							carte.setCarte(cardNumber);
							carte.setPcidsscarte(Util.formatCard(cardNumber));

							// Formatage de la date d'expiration
							String dateExStr = dateFormatSimple.format(card.getExprDate());
							Util.formatDateExp(dateExStr, carte, folder, file);

							cartes.add(carte);
						}
					}
					logger.info("Cartes : " + cartes);
					demandeDto.setCartes(cartes);
				} else {
					demandeDto.setCartes(null);
				}
			} catch (Exception ex) {
				Util.writeInFileTransaction(folder, file, "showPagePayment 500 idclient not found: " + Util.formatException(ex));
				logger.error("Erreur lors de la récupération des cartes pour idClient : " + idClient, ex);
			}
		} else {
			demandeDto.setCartes(null);
		}
	}

	@Override
	public void processInfosMerchant(DemandePaiementDto demandeDto, String folder, String file) {
		String merchantid = demandeDto.getComid();
		String orderid = demandeDto.getCommande();
		try {
			CommercantDto merchant = commercantService.findByCmrNumcmr(merchantid);
			if (merchant != null) {
				demandeDto.setCommercantDto(merchant);
			}
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPagePayment 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(e));
		}
		try {
			GalerieDto galerie = galerieService.findByCodeCmr(merchantid);
			if (galerie != null) {
				demandeDto.setGalerieDto(galerie);
			}
		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file,
					"showPagePayment 500 Galerie misconfigured in DB or not existing orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(e));
		}
	}

	@Override
	public void logMessage(String file, String message) {
		LocalDateTime dateF;
		String folder;
		dateF = LocalDateTime.now(ZoneId.systemDefault());
		folder = dateF.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		Util.writeInFileTransaction(folder, file, message);
		logger.info(message);
	}

	@Override
	public String handleSwitchError(Exception e, String file, String orderid, String merchantid, String resp_tlv, DemandePaiementDto dmd, Model model, String page) {
		dmd.setDemCvv("");
		dmd.setEtatDemande("SW_KO");
		demandePaiementService.save(dmd);

		// Gestion spécifique en fonction du type d'exception
		if (e instanceof UnknownHostException) {
			logMessage(file, "Switch malfunction: UnknownHostException - " + e.getMessage());
		} else if (e instanceof SocketTimeoutException) {
			logMessage(file, "Switch malfunction: SocketTimeoutException - " + e.getMessage());
		} else if (e instanceof IOException) {
			logMessage(file, "Switch malfunction: IOException - " + e.getMessage());
		} else {
			logMessage(file, "Switch malfunction: General Exception - " + e.getMessage());
		}

		logMessage(file,
				"authorization 500 Error Switch communication - orderid: " + orderid + ", merchantid: " + merchantid + ", resp_tlv: " + resp_tlv);

		// Mise à jour du message utilisateur
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
		model.addAttribute("demandeDto", demandeDtoMsg);
		dmd.setMsgRefus(demandeDtoMsg.getMsgRefus() == null ? "" : demandeDtoMsg.getMsgRefus());
		return page;
	}

	@Override
	public String handleMpiError(String errmpi, String file, String idDemande, String threeDSServerTransID,
								  DemandePaiementDto dmd, Model model, String page) {
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();

		// Déterminer l'état de la demande et le message à afficher en fonction de l'erreur
		switch (errmpi) {
			case "COMMERCANT NON PARAMETRE":
				logMessage(file, "COMMERCANT NON PARAMETRE : " + idDemande);
				dmd.setDemxid(threeDSServerTransID);
				dmd.setDemCvv("");
				dmd.setEtatDemande("MPI_CMR_INEX");
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (COMMERCANT NON PARAMETRE), votre compte ne sera pas débité, merci de réessayer.");
				break;
			case "BIN NON PARAMETRE":
				logMessage(file, "BIN NON PARAMETRE : " + idDemande);
				dmd.setEtatDemande("MPI_BIN_NON_PAR");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (BIN NON PARAMETREE), votre compte ne sera pas débité, merci de réessayer.");
				break;
			case "DIRECTORY SERVER":
				logMessage(file, "DIRECTORY SERVER : " + idDemande);
				dmd.setEtatDemande("MPI_DS_ERR");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				break;
			case "CARTE ERRONEE":
				logMessage(file, "CARTE ERRONEE : " + idDemande);
				dmd.setEtatDemande("MPI_CART_ERROR");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (CARTE ERRONEE), votre compte ne sera pas débité, merci de réessayer.");
				break;
			case "CARTE NON ENROLEE":
				logMessage(file, "CARTE NON ENROLEE : " + idDemande);
				dmd.setEtatDemande("MPI_CART_NON_ENR");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti (CARTE NON ENROLLE), votre compte ne sera pas débité, merci de réessayer.");
				break;
			case "ERROR REPONSE ACS":
				logMessage(file, "ERROR REPONSE ACS : " + idDemande);
				dmd.setEtatDemande("MPI_ERR_RS_ACS");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				break;
			case "Error 3DSS":
				logMessage(file, "Error 3DSS : " + idDemande);
				dmd.setEtatDemande("MPI_ERR_3DSS");
				dmd.setDemCvv("");
				dmd.setDemxid(threeDSServerTransID);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				break;
			default:
				logMessage(file, "Erreur MPI inconnue : " + errmpi + " pour idDemande : " + idDemande);
				demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité, merci de réessayer.");
				break;
		}

		// Enregistrer la demande et ajouter le message d'erreur au modèle
		demandePaiementService.save(dmd);
		model.addAttribute("demandeDto", demandeDtoMsg);

		String pageR = "result";
		logMessage(file, "Fin handleMpiError ()");
		return pageR;
	}

	@Override
	public String handleMerchantAndInfoCommercantError(String file, String orderid, String merchantid, String websiteid,
														DemandePaiementDto demandeDtoMsg, Model model, String page, boolean isMerchantError) {
		if (isMerchantError) {
			logMessage(file, "recharger 500 Merchant misconfigured in DB or not existing orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "]");
		} else {
			logMessage(file, "recharger 500 InfoCommercantDto misconfigured in DB or not existing orderid:[" + orderid
					+ "] and merchantid:[" + merchantid + "] and websiteid:[" + websiteid + "]");
		}

		demandeDtoMsg.setMsgRefus("La transaction en cours n’a pas abouti, votre compte ne sera pas débité.");
		model.addAttribute("demandeDto", demandeDtoMsg);
		String pageR = "result";
		logMessage(file, "Fin processRequest ()");
		return pageR;
	}

	@Override
	public String handleCardValidationError(int iCardValid, String cardNumber, String orderid, String merchantid,
											String file, DemandePaiementDto demandeDtoMsg,
											Model model, String page) {
		switch (iCardValid) {
			case 1:
				logMessage(file, "Card number length is incorrect. Order ID:["
						+ orderid + "] and Merchant ID:[" + merchantid + "]");
				demandeDtoMsg.setMsgRefus("Le numéro de la carte est incomplet, merci de réessayer.");
				break;

			case 2:
				logMessage(file, "Card number is not valid (Luhn check failed). Order ID:["
						+ orderid + "] and Merchant ID:[" + merchantid + "]");
				demandeDtoMsg.setMsgRefus("Le numéro de la carte est invalide, merci de réessayer.");
				break;

			default:
				logMessage(file, "Card validation passed for Order ID:["
						+ orderid + "] and Merchant ID:[" + merchantid + "]");
				return page; // Pas d'erreur détectée
		}

		// Ajouter le message au modèle et rediriger vers la page de résultat
		model.addAttribute("demandeDto", demandeDtoMsg);
		return "result";
	}

	@Override
	public String handleSessionTimeout(HttpSession session,	String file, int timeout, DemandePaiementDto demandeDto,
			DemandePaiementDto demandeDtoMsg, Model model) {
		Long paymentStartTime = 0L;
		if(demandeDto.getTimeoutURL() != null) {
			if(demandeDto.getTimeoutURL().isEmpty()) {
				paymentStartTime = (Long) session.getAttribute("paymentStartTime");
			} else {
				paymentStartTime = Long.parseLong(demandeDto.getTimeoutURL());
			}
		} else {
			paymentStartTime = (Long) session.getAttribute("paymentStartTime");
		}

		logMessage(file, "paymentStartTime : " + paymentStartTime);

		if (paymentStartTime != null) {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - paymentStartTime;
			logMessage(file, "currentTime : " + currentTime);
			logMessage(file, "elapsedTime : " + elapsedTime);

			// Vérifier si le temps écoulé dépasse le timeout (en millisecondes)
			if (elapsedTime > timeout) {
				logMessage(file, "Page expirée Time > " + timeout + "ms");
				demandeDtoMsg.setMsgRefus("Votre session de paiement a expiré. Veuillez réessayer.");
				session.setAttribute("idDemande", demandeDto.getIddemande());
				model.addAttribute("demandeDto", demandeDtoMsg);

				// Mise à jour de l'état de la demande
				if(demandeDto.getEtatDemande() != null) {
					if(demandeDto.getEtatDemande().equals("P_CHRG_OK")
							|| demandeDto.getEtatDemande().equals("START_PAYMENT")) {
						demandeDto.setEtatDemande("TimeOut");
					}
				}

				demandeDto.setDemCvv("");
				demandePaiementService.save(demandeDto);

				logMessage(file, "*********** Session expirée **************");
				return "timeout";
			}
		}

		return null; // Pas d'expiration
	}


}