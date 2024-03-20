package ma.m2m.gateway.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.model.Commercant;
import ma.m2m.gateway.model.Galerie;
import ma.m2m.gateway.repository.CommercantDao;
import ma.m2m.gateway.repository.GalerieDao;
import ma.m2m.gateway.threedsecure.AuthInitRequest;
import ma.m2m.gateway.threedsecure.ThreeDSecureRequestor;
import ma.m2m.gateway.threedsecure.ThreeDSecureRequestorException;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class AutorisationServiceImpl implements AutorisationService {
		
	private Gson gson;
	
	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDSS_V;
	
	@Value("${key.LIEN_3DSS_M}")
	private String urlThreeDSS_M;
	
	@Value("${key.LIEN_NOTIFICATION_ACS}")
	private String notificationACS;
	
	@Value("${key.LIEN_NOTIFICATION_CCB_ACS}")
	private String notificationCCBACS;
	
	private String typeCarte;
	
	private Galerie galerie = new Galerie();
	private Commercant commercant = new Commercant();
	private InfoCommercantDto infoCommercantDto = new InfoCommercantDto();
	
	@Autowired
	private InfoCommercantService infoCommercantService;
	
	@Autowired
	private GalerieDao galerieDao;
	
	@Autowired
	private CommercantDao commercantDao;
	
	public AutorisationServiceImpl() {
		this.gson = new GsonBuilder().serializeNulls().create();
	}
	
	
	@Override
	public String controllerDataRequest(DemandePaiementDto demandeDto) {
		String message = "";
		
		if(demandeDto.getComid().equals("")) {
			message="Commerçant inexistant dans la demande";
		}
		else if(demandeDto.getCommande().equals("")) {
			message="Commande inexistant dans la demande";
		}
		else if(demandeDto.getMontant() <= 0) {
			message="Montant inferieur ou égale à 0";
		}
		commercant = commercantDao.findByCmrCode(demandeDto.getComid());

		if (commercant == null) {
			message="Commerçant inexistant dans la BD";
		}
		else if (commercant.getCmrEtat() == "0") {
			message="L'état du commérçant est 0";
		}

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());

		if (infoCommercantDto == null) {
			message="InfoCommerçant inexistant dans la BD";
		}
		else if (infoCommercantDto.getCmrCurrency() == null) {
			message="La devise n'est pas renseigne dans la table INFO_COMMERCANT";
		}

		galerie = galerieDao.findByCodeGalAndCodeCmr(demandeDto.getGalid(), demandeDto.getComid());

		if (galerie == null || galerie.getEtat() == "N") {
			message="La galerie inexistant dans la BD ou l'etat égale à N";
		}
		else if (galerie.getDateActivation() == null) {
			message="La galerie inactive";
		}
		
		return message;
	}

	@Override
	public ThreeDSecureResponse autoriser(ThreeDSecureResponse reponse, String folder, String file) {
		
		ThreeDSecureResponse threeDSecureResponse = new ThreeDSecureResponse();
		
		return threeDSecureResponse;
	}
	

	@Override
	public ThreeDSecureResponse preparerReqThree3DSS(DemandePaiementDto demandeDto,String folder,String file) {
		//Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "Debut preparerReqThree3DSS()");
		System.out.println("Start preparerReqThree3DSS()");

		typeCarte = demandeDto.getType_carte();
		
		//demandeDto.setExpery(demandeDto.getAnnee().concat(demandeDto.getMois()));
		//demandeDto.setExpery(demandeDto.getDateexpnaps());

		ThreeDSecureRequestor threeDSecureRequestor = new ThreeDSecureRequestor(folder,file);
		AuthInitRequest authInitRequest= new AuthInitRequest();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());
		
		if(typeCarte.equals("2")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 2 => Master Card ");
			System.out.println("typeCarte 2 => Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDSS_M);		
		} else if(typeCarte.equals("1")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 1 => Visa ");
			System.out.println("typeCarte 1 => Visa ");
			authInitRequest.setUrlThreeDSS(urlThreeDSS_V);
		} else {
			Util.writeInFileTransaction(folder, file, "typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			System.out.println("typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDSS_M);
		}
		
		authInitRequest.setPan(demandeDto.getDem_pan());
		authInitRequest.setAmount(demandeDto.getMontant());				
		authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());				
		authInitRequest.setIdCommercant(demandeDto.getComid());
		authInitRequest.setIdDemande(demandeDto.getIddemande());					
		authInitRequest.setExpiry(demandeDto.getExpery());
		//authInitRequest.setAcquirerBIN("11010");
		authInitRequest.setBrowserAcceptHeader("test");
		authInitRequest.setBrowserUserAgent("test");
		if(demandeDto.getEmail() == null || demandeDto.getEmail().equals("")) {
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
		} else {
			authInitRequest.setEmail(demandeDto.getEmail());
		}
		authInitRequest.setMcc(commercant.getCmrCodactivite());
		authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());	
		authInitRequest.setNotificationURL(notificationACS);
		
		Util.writeInFileTransaction(folder, file,"" + authInitRequest);
		
		threeDSecureRequestor.threeDSecureRequest(authInitRequest);
		
		//Util.writeInFileTransaction(folder, file,"Debut appel ThreeDSecure ");
		
		try {
			threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
		} catch (ThreeDSecureRequestorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Util.writeInFileTransaction(folder, file,"fin preparerReqThree3DSS ");

		
		return threeDsecureResponse;
	}
	
	@Override
	public ThreeDSecureResponse preparerReqMobileThree3DSS(DemandePaiementDto demandeDto,String folder,String file) {
		//Util.creatFileTransaction(file);
		Util.writeInFileTransaction(folder, file, "Debut preparerReqMobileThree3DSS()");
		System.out.println("Start preparerReqMobileThree3DSS()");

		typeCarte = demandeDto.getType_carte();
		
		//demandeDto.setExpery(demandeDto.getAnnee().concat(demandeDto.getMois()));
		//demandeDto.setExpery(demandeDto.getDateexpnaps());

		ThreeDSecureRequestor threeDSecureRequestor = new ThreeDSecureRequestor(folder,file);
		AuthInitRequest authInitRequest= new AuthInitRequest();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());
		
		if(typeCarte.equals("2")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 2 => Master Card ");
			System.out.println("typeCarte 2 => Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDSS_M);		
		} else if(typeCarte.equals("1")) {
			Util.writeInFileTransaction(folder, file, "typeCarte 1 => Visa ");
			System.out.println("typeCarte 1 => Visa ");
			authInitRequest.setUrlThreeDSS(urlThreeDSS_V);
		} else {
			Util.writeInFileTransaction(folder, file, "typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			System.out.println("typeCarte ni 1 ni 2 => on donne par defaut Master Card ");
			authInitRequest.setUrlThreeDSS(urlThreeDSS_M);
		}
		// calcule du montant avec les frais
		String montantTotal = calculMontantTotalOperation(demandeDto);
		authInitRequest.setPan(demandeDto.getDem_pan());
		authInitRequest.setAmount(Double.valueOf(montantTotal == null ? "" : montantTotal));			
		authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());				
		authInitRequest.setIdCommercant(demandeDto.getComid());
		authInitRequest.setIdDemande(demandeDto.getIddemande());					
		authInitRequest.setExpiry(demandeDto.getExpery());
		//authInitRequest.setAcquirerBIN("11010");
		authInitRequest.setBrowserAcceptHeader("test");
		authInitRequest.setBrowserUserAgent("test");
		if(demandeDto.getEmail() == null || demandeDto.getEmail().equals("")) {
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
		} else {
			authInitRequest.setEmail(demandeDto.getEmail());
		}
		authInitRequest.setMcc(commercant.getCmrCodactivite());
		authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
		authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());	
		authInitRequest.setNotificationURL(notificationCCBACS);
		
		Util.writeInFileTransaction(folder, file,"" + authInitRequest);
		
		threeDSecureRequestor.threeDSecureRequest(authInitRequest);
		
		//Util.writeInFileTransaction(folder, file,"Debut appel ThreeDSecure ");
		
		try {
			threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
		} catch (ThreeDSecureRequestorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Util.writeInFileTransaction(folder, file,"fin preparerReqMobileThree3DSS ");
		System.out.println("Fin preparerReqMobileThree3DSS()");

		
		return threeDsecureResponse;
	}
	
	@Override
	public ThreeDSecureResponse callThree3DSSAfterACS(String decodedCres, String folder, String file) {
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();
		
		Util.writeInFileTransaction(folder, file, "*********** DEBUT callThree3DSSAfterACS ***********");
		
		// soit visa soit mastercard il a aucun impact apres auth
		Util.writeInFileTransaction(folder, file, "UrlThreeDSS : " + urlThreeDSS_M);
		
		System.out.println("UrlThreeDSS : " + urlThreeDSS_M);
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			httpClient = getAllSSLClient();
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-KeyManagementException] " + e1);
		}

		HttpPost httpPost = new HttpPost(urlThreeDSS_M);

		final StringEntity entity = new StringEntity(decodedCres, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {
			HttpResponse responseTheeDs = httpClient.execute(httpPost);
			//HttpResponse responseTheeDs=null;
			StatusLine responseStatusLine = responseTheeDs.getStatusLine();
			Util.writeInFileTransaction(folder, file, "Response StatusCode : " + responseStatusLine.getStatusCode());
			
			String responseStr = EntityUtils.toString(responseTheeDs.getEntity());

			Util.writeInFileTransaction(folder, file, "Rreq String : " + responseStr);
			System.out.println("Response String : " + responseStr);

			threeDsecureResponse = gson.fromJson(responseStr, ThreeDSecureResponse.class);

			Util.writeInFileTransaction(folder, file,"" + threeDsecureResponse);

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION-ClientProtocolException] " + e);
		}

		Util.writeInFileTransaction(folder, file, "*********** FIN callThree3DSSAfterACS ***********");
		return threeDsecureResponse;
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
//		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
//				.register("https", sslConnectionFactory).register("http", plainConnectionSocketFactory).build();
//
//		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
//
//		builder.setConnectionManager(ccm);

		return builder.build();

	}



}
