package ma.m2m.gateway.threedsecure;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.model.DemandePaiement;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.X509TrustManager;
@Slf4j
@WebServlet("/listnerCRes")
public class listnerCRes extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String cres;
	private Gson gson;

	private String urlThreeDSS;

	/*
	 * ------------ DEBUT LOG PARAMS ------------
	 */
	private Traces traces = new Traces();
	private String file;
	private int int_random;
	private String folder;
	private LocalDateTime date;
	private Random rand = new Random();
	/*
	 * ------------ DEBUT ENTITIES INSTANCES ------------
	 */
	private DemandePaiement demandePaiement = new DemandePaiement();

	/*
	 * ------------ DEBUT DAO INSTANCES ------------
	 */

	static {
		// lnaoui 23-11-2022
		// this part is needed cause the endpoint has invalid SSL certificate, that
		// cannot be normally processed by Java
		TrustManager[] trustAllCertificates = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null; // Not relevant.
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// Do nothing. Just allow them all.
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				// Do nothing. Just allow them all.
			}
		} };

		HostnameVerifier trustAllHostnames = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true; // Just allow them all.
			}
		};

		try {
			System.setProperty("jsse.enableSNIExtension", "false");
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCertificates, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
		} catch (GeneralSecurityException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public listnerCRes() {
//		date = LocalDateTime.now(ZoneId.systemDefault());
//		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
//		int_random = rand.nextInt(999999999);
//		file = "ACS_" + int_random;
//		this.gson = new GsonBuilder().serializeNulls().create();
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		date = LocalDateTime.now(ZoneId.systemDefault());
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		int_random = rand.nextInt(999999999);
		file = "RETOUR_ACS_" + int_random;
		
		traces.creatFileTransaction(file);

		traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> DEBUT appel ThreeDSServer apres authentification");

		// BufferedReader reader = request.getReader();
		// Gson gson = new Gson();
		// Map<String, String> cresM = gson.fromJson(reader, Map.class);
		String encodedCres = request.getParameter("cres");
		// cres = IOUtils.toString(request.getInputStream());
		// System.out.println("listnerCRes RETOUR ACS =====> encodedCres : " +
		// encodedCres);
		traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> encodedCres : " + encodedCres);

		String decodedCres = "";

		decodedCres = new String(Base64.decodeBase64(encodedCres.getBytes()));
		if (decodedCres.indexOf("}") != -1) {
			decodedCres = decodedCres.substring(0, decodedCres.indexOf("}") + 1);
		}
		traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> decodedCres : " + decodedCres);

		//CRes cleanCres = gson.fromJson(decodedCres, CRes.class);
		//traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> cleanCres : " + cleanCres);
		//final String jsonBody = gson.toJson(cleanCres);
		//DemandePaiement demandeP = new DemandePaiement();
		//demandeP = demandePaiementDAO.findByThreeDSServerTransID(cleanCres.getThreeDSServerTransID());
		
		// get link mpi from file config
		//if(demandeP != null && !demandeP.getType_carte().equals("")) {
		//	if(demandeP.getType_carte().equals("1")) {
		//		urlThreeDSS = LIEN_3DSS_V;
		//	} else if(demandeP.getType_carte().equals("2")) {
		//		urlThreeDSS = LIEN_3DSS_M;
		//	}
		//} else {
		//	traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> DemandePaiementByThreeDSServerTransID null : " + cleanCres.getThreeDSServerTransID());
		//	response.sendRedirect(redirectFailURL(demandePaiement));
		//	return;
		//}
		//urlThreeDSS = LIEN_3DSS_V;// soit visa soit mastercard il a aucun impact apres auth
		traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> urlThreeDSS : " + urlThreeDSS);
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			httpClient = getAllSSLClient();
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e1) {
			traces.writeInFileTransaction(folder, file, "[GW-EXCEPTION-KeyManagementException] listnerCRes " + e1);
		}

		HttpPost httpPost = new HttpPost(urlThreeDSS);

		// ThreeDSecureResponse threeDsecureResponse = new ThreeDSecureResponse();

		final StringEntity entity = new StringEntity(decodedCres, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {
			HttpResponse responseTheeDs = httpClient.execute(httpPost);

			StatusLine responseStatusLine = responseTheeDs.getStatusLine();
			traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> RETOUR 3DSS response StatusCode : "
					+ responseTheeDs.getStatusLine().getStatusCode());
			traces.writeInFileTransaction(folder, file,
					"listnerCRes RETOUR ACS =====> RETOUR 3DSS responseStatusLine : " + responseStatusLine);
			String respStr = EntityUtils.toString(responseTheeDs.getEntity());

			traces.writeInFileTransaction(folder, file, "listnerCRes RETOUR ACS =====> RETOUR 3DSS respStr : " + respStr);

			ThreeDSecureResponse threeDsecureResponse = gson.fromJson(respStr, ThreeDSecureResponse.class);

			traces.writeInFileTransaction(folder, file,
					"listnerCRes RETOUR ACS =====> RETOUR 3DSS threeDsecureResponse toString : "
							+ threeDsecureResponse);
			//demandePaiement = new DemandePaiement();
			//demandePaiement = demandePaiementDAO.find(Integer.parseInt(threeDsecureResponse.getIdDemande()));

			traces.writeInFileTransaction(folder, file,
					"listnerCRes DEBUT appel handleDemandeThreeDSResponse apres authenrification");
			//AutorisationBacking3DSSC.handleDemandeACSResponse(threeDsecureResponse, response);

		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file, "[GW-EXCEPTION-ClientProtocolException] listnerCRes " + e);
			response.sendRedirect(redirectFailURL(demandePaiement));
			return;
		}

		traces.writeInFileTransaction(folder, file, "RETOUR ACS =====> FIN appel ThreeDSServer apres authenrification");
	}
	
	public String redirectFailURL(DemandePaiement demandePaiement) throws IOException {
		traces.writeInFileTransaction(folder, file,
				"REDIRECT FAIL URL DEMANDE PAIEMENT {" + demandePaiement.getIddemande() + "} => " + "Commerçant: {"
						+ demandePaiement.getComid() + "} Commande: {" + demandePaiement.getCommande() + "}");
		String signedFailUrl;
		String idCommande = demandePaiement.getCommande();
		//infoCommercant = dao.getCommercantInfo(demandePaiement.getComid());
		//String md5Signature = hachInMD5(idCommande + infoCommercant.getClePub());
		String md5Signature ="";
		signedFailUrl = demandePaiement.getFailURL() + "?id_commande=" + demandePaiement.getCommande() + "&token="
				+ md5Signature;
		traces.writeInFileTransaction(folder, file, "FAIL URL Signed : " + signedFailUrl);
		return signedFailUrl;
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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
