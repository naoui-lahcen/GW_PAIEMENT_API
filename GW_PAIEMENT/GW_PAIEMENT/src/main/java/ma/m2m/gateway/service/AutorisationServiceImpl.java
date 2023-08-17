package ma.m2m.gateway.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.SplittableRandom;

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
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.model.Commercant;
import ma.m2m.gateway.model.Galerie;
import ma.m2m.gateway.model.InfoCommercant;
import ma.m2m.gateway.repository.CommercantDao;
import ma.m2m.gateway.repository.GalerieDao;
import ma.m2m.gateway.repository.InfoCommercantDao;
import ma.m2m.gateway.threedsecure.AuthInitRequest;
import ma.m2m.gateway.threedsecure.ThreeDSecureRequestor;
import ma.m2m.gateway.threedsecure.ThreeDSecureRequestorException;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;

@Service
public class AutorisationServiceImpl implements AutorisationService {
	
	private Traces traces = new Traces();
	
	private Gson gson;
	
	@Value("${key.LIEN_3DSS_V}")
	private String urlThreeDSS_V;
	
	@Value("${key.LIEN_3DSS_M}")
	private String urlThreeDSS_M;
	
	@Value("${key.LIEN_NOTIFICATION_ACS}")
	private String notificationACS;
	
	private String typeCarte;
	
	private Galerie galerie = new Galerie();
	private Commercant commercant = new Commercant();
	private InfoCommercantDto infoCommercantDto = new InfoCommercantDto();
	
	@Autowired
	private InfoCommercantDao infoCommercantDao;
	
	@Autowired
	private InfoCommercantService infoCommercantService;
	
	@Autowired
	private GalerieDao galerieDao;
	
	@Autowired
	private CommercantDao commercantDao;
	
	public AutorisationServiceImpl() {
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
	public ThreeDSecureResponse callThree3DSS(DemandePaiementDto demandeDto,String folder,String file) {
		//traces.creatFileTransaction(file);
		traces.writeInFileTransaction(folder, file, "Start payer Service ()");
		System.out.println("Start payer Service ()");
		
		traces.writeInFileTransaction(folder, file, "demandeDto dem_pan : " + demandeDto.getDem_pan());
		traces.writeInFileTransaction(folder, file, "demandeDto dem_cvv : " + demandeDto.getDem_cvv());
		traces.writeInFileTransaction(folder, file, "demandeDto type_carte : " + demandeDto.getType_carte());

		typeCarte = demandeDto.getType_carte();
		traces.writeInFileTransaction(folder, file, "demandeDto enum type_carte : " + demandeDto.getType_carte());

		System.out.println("demandeDto enum type_carte : " + demandeDto.getType_carte());
		//demandeDto.setExpery(demandeDto.getAnnee().concat(demandeDto.getMois()));
		demandeDto.setExpery(demandeDto.getDateexpnaps());
		traces.writeInFileTransaction(folder, file, "demandeDto expery : " + demandeDto.getExpery());

		System.out.println("demandeDto expery : " + demandeDto.getExpery());
		ThreeDSecureRequestor threeDSecureRequestor = new ThreeDSecureRequestor(folder,file);
		AuthInitRequest authInitRequest= new AuthInitRequest();
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		infoCommercantDto = infoCommercantService.findByCmrCode(demandeDto.getComid());
		
		if(typeCarte.equals("2")) {
			authInitRequest.setPan(demandeDto.getDem_pan());
			authInitRequest.setAmount(demandeDto.getMontant());				
			authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());				
			authInitRequest.setIdCommercant(demandeDto.getComid());
			authInitRequest.setIdDemande(demandeDto.getIddemande());					
			authInitRequest.setExpiry(demandeDto.getExpery());
			//authInitRequest.setAcquirerBIN("11010");
			authInitRequest.setBrowserAcceptHeader("test");
			authInitRequest.setBrowserUserAgent("test");
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
			authInitRequest.setMcc(commercant.getCmrCodactivite());
			authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
			authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());	
			authInitRequest.setNotificationURL(notificationACS);
			authInitRequest.setUrlThreeDSS(urlThreeDSS_M);
			
			traces.writeInFileTransaction(folder, file,"authInitRequest : " + authInitRequest);
			
			threeDSecureRequestor.threeDSecureRequest(authInitRequest);
			
			traces.writeInFileTransaction(folder, file,"Debut appel ThreeDSecure ");
			
			try {
				threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
			} catch (ThreeDSecureRequestorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			traces.writeInFileTransaction(folder, file,"fin appel ThreeDSecure response : " + threeDsecureResponse);
			
		} else if(typeCarte.equals("1")) {
			authInitRequest.setPan(demandeDto.getDem_pan());
			authInitRequest.setAmount(demandeDto.getMontant());				
			authInitRequest.setCurrency(infoCommercantDto.getCmrCurrency().trim());				
			authInitRequest.setIdCommercant(demandeDto.getComid());
			authInitRequest.setIdDemande(demandeDto.getIddemande());					
			authInitRequest.setExpiry(demandeDto.getExpery());
			//authInitRequest.setAcquirerBIN("11010");
			authInitRequest.setBrowserAcceptHeader("test");
			authInitRequest.setBrowserUserAgent("test");
			authInitRequest.setEmail(infoCommercantDto.getCmrEmail());
			authInitRequest.setMcc(commercant.getCmrCodactivite());
			authInitRequest.setMerchantCountryCode(infoCommercantDto.getCmrCurrency().trim());
			authInitRequest.setNomCommercant(infoCommercantDto.getCmrNom());	
			authInitRequest.setUrlThreeDSS(urlThreeDSS_V);
			authInitRequest.setNotificationURL(notificationACS);
			
			traces.writeInFileTransaction(folder, file,"authInitRequest : " + authInitRequest);
			
			threeDSecureRequestor.threeDSecureRequest(authInitRequest);
			
			traces.writeInFileTransaction(folder, file,"Debut appel ThreeDSecure ");
			
			try {
				threeDsecureResponse = threeDSecureRequestor.initAuth(folder, file);
			} catch (ThreeDSecureRequestorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			traces.writeInFileTransaction(folder, file,"fin appel ThreeDSecure response : " + threeDsecureResponse);
			
		}

		
		return threeDsecureResponse;
	}
	
	@Override
	public ThreeDSecureResponse callThree3DSSAfterACS(String decodedCres, String folder, String file) {
		ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();
		
		// soit visa soit mastercard il a aucun impact apres auth
		traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> urlThreeDSS : " + urlThreeDSS_V);
		
		System.out.println("ACSController RETOUR ACS =====> urlThreeDSS : " + urlThreeDSS_V);
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			httpClient = getAllSSLClient();
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
			traces.writeInFileTransaction(folder, file, "[GW-EXCEPTION-KeyManagementException] ACSController " + e1);
		}

		HttpPost httpPost = new HttpPost(urlThreeDSS_V);

		final StringEntity entity = new StringEntity(decodedCres, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {
			HttpResponse responseTheeDs = httpClient.execute(httpPost);
			//HttpResponse responseTheeDs=null;
			StatusLine responseStatusLine = responseTheeDs.getStatusLine();
			traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> RETOUR 3DSS response StatusCode : "
					+ responseTheeDs.getStatusLine().getStatusCode());
			traces.writeInFileTransaction(folder, file,
					"ACSController RETOUR ACS =====> RETOUR 3DSS responseStatusLine : " + responseStatusLine);
			String respStr = EntityUtils.toString(responseTheeDs.getEntity());

			traces.writeInFileTransaction(folder, file, "ACSController RETOUR ACS =====> RETOUR 3DSS respStr : " + respStr);

			threeDsecureResponse = gson.fromJson(respStr, ThreeDSecureResponse.class);

			traces.writeInFileTransaction(folder, file,
					"ACSController RETOUR ACS =====> RETOUR 3DSS threeDsecureResponse toString : "
							+ threeDsecureResponse);

			traces.writeInFileTransaction(folder, file,
					"ACSController DEBUT appel handleDemandeThreeDSResponse apres authenrification");

		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file, "[GW-EXCEPTION-ClientProtocolException] ACSController " + e);
		}

		traces.writeInFileTransaction(folder, file, "RETOUR ACS =====> FIN appel ThreeDSServer apres authenrification");
		return threeDsecureResponse;
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
