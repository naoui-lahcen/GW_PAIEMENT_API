package ma.m2m.gateway.threedsecure;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.Registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.Utils.Traces;
import ma.m2m.gateway.Utils.Util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
//import org.apache.http.config.Registry;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Slf4j
public class ThreeDSecureRequestor {

	private String logFolder;
	private String logFile;
	private Gson gson;

	private String urlThreeDSS;
	private AuthInitRequest authInitRequest;

	private Traces traces = new Traces();
	
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

	public ThreeDSecureRequestor() {
		super();
	}

	public ThreeDSecureRequestor(String logFolder, String logFile) {
		this.logFolder = logFolder;
		this.logFile = logFile;
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	public ThreeDSecureRequestor urlThreeDSS(String urlThreeDSS) {
		this.urlThreeDSS = urlThreeDSS;
		return this;
	}

	public ThreeDSecureRequestor threeDSecureRequest(final AuthInitRequest authInitRequest) {
		this.authInitRequest = authInitRequest;
		return this;
	}

	public ThreeDSecureResponse initAuth(String logFolder, String logFile) throws ThreeDSecureRequestorException {
		traces.writeInFileTransaction(logFolder, logFile, 
				"*********** DEBUT initAuth ***********");
		ThreeDSecureResponse threeDsRes = new ThreeDSecureResponse();

		if (authInitRequest == null)
			throw new ThreeDSecureRequestorException("[authInitRequest] field instance is NULL");

		try {
			threeDsRes = callThreeDSServer(logFolder, logFile);
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				traces.writeInFileTransaction(logFolder, logFile, "********* 2eme TENTATIVE ************");
				try {
					threeDsRes = callThreeDSServer(logFolder, logFile);
				} catch (Exception e2) {
					throw new ThreeDSecureRequestorException((e2.getCause() != null) ? e2.getCause() : e2);
				}
			} else
				throw new ThreeDSecureRequestorException((e.getCause() != null) ? e.getCause() : e);
		}
		traces.writeInFileTransaction(logFolder, logFile,
				"*********** FIN initAuth ***********");
		return threeDsRes;
	}

	protected ThreeDSecureResponse callThreeDSServer(String logFolder, String logFile)
			throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		traces.writeInFileTransaction(logFolder, logFile, "*********** DEBUT callThreeDSServer ***********");

		HttpClient httpClient = HttpClientBuilder.create().build();
		httpClient = getAllSSLClient();
		HttpPost httpPost = new HttpPost(authInitRequest.getUrlThreeDSS());
		ThreeDSecureResponse threeDSecureResponse = new ThreeDSecureResponse();

		final String jsonBody = gson.toJson(authInitRequest);
		// pcidss carte
		AuthInitRequest authInitReqPCIDSS = new AuthInitRequest();
		authInitReqPCIDSS.setPan(Util.displayCard(authInitRequest.getPan()));
		authInitReqPCIDSS.setAmount(authInitRequest.getAmount());
		authInitReqPCIDSS.setIdCommercant(authInitRequest.getIdCommercant());
		authInitReqPCIDSS.setCurrency(authInitRequest.getCurrency());
		authInitReqPCIDSS.setIdDemande(authInitRequest.getIdDemande());
		authInitReqPCIDSS.setExpiry(authInitRequest.getExpiry());
		authInitReqPCIDSS.setAcquirerBIN(authInitRequest.getAcquirerBIN());
		authInitReqPCIDSS.setBrowserUserAgent(authInitRequest.getBrowserUserAgent());
		authInitReqPCIDSS.setEmail(authInitRequest.getEmail());
		authInitReqPCIDSS.setMcc(authInitRequest.getMcc());
		authInitReqPCIDSS.setMerchantCountryCode(authInitRequest.getMerchantCountryCode());
		authInitReqPCIDSS.setNomCommercant(authInitRequest.getNomCommercant());
		authInitReqPCIDSS.setNotificationURL(authInitRequest.getNotificationURL());
		authInitReqPCIDSS.setThreeDSRequestorAuthenticationInd(authInitRequest.getThreeDSRequestorAuthenticationInd());
		authInitReqPCIDSS.setMessageCategory(authInitRequest.getMessageCategory());
		authInitReqPCIDSS.setUrlThreeDSS(authInitRequest.getUrlThreeDSS());
		
		final String jsonBodyPCIDSS = gson.toJson(authInitReqPCIDSS);
		traces.writeInFileTransaction(logFolder, logFile, "*********** jsonBodyPCIDSS ***********" + jsonBodyPCIDSS.toString());
		// added 2023-03-22 pcidss carte
		
		final StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);

		httpPost.setEntity(entity);
		// httpPost.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
		// httpPost.setHeader("Content-type",
		// ContentType.APPLICATION_JSON.getMimeType());
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		try {

			HttpResponse response = httpClient.execute(httpPost);

			StatusLine responseStatusLine = response.getStatusLine();
			traces.writeInFileTransaction(logFolder, logFile, "Response StatusCode : " + responseStatusLine.getStatusCode());
			
			//traces.writeInFileTransaction(logFolder, logFile, "response : " + response);
			
			String responseStr = EntityUtils.toString(response.getEntity());
			traces.writeInFileTransaction(logFolder, logFile, "Ares String : " + responseStr);

			threeDSecureResponse = gson.fromJson(responseStr, ThreeDSecureResponse.class);
			traces.writeInFileTransaction(logFolder, logFile,""+ threeDSecureResponse);

			// ((InputStream) httpClient).close();

		} catch (ClientProtocolException e) {
			traces.writeInFileTransaction(logFolder, logFile, "[GW-EXCEPTION-ClientProtocolException] " + e);
		} catch (IOException e) {
			traces.writeInFileTransaction(logFolder, logFile, "[GW-EXCEPTION-IOException] " + e);
		}

		traces.writeInFileTransaction(logFolder, logFile, "*********** FIN callThreeDSServer ***********");
		return threeDSecureResponse;
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
