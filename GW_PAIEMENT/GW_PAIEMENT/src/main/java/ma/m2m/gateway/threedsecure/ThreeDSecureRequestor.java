package ma.m2m.gateway.threedsecure;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.Registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class ThreeDSecureRequestor {

	private String logFolder;
	private String logFile;
	private Gson gson;

	private String urlThreeDSS;
	private AuthInitRequest authInitRequest;

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

	public ThreeDSecureResponse initAuth() throws ThreeDSecureRequestorException {
		Util.writeInFileTransaction(logFolder, logFile,
				"**************************** DEBUT APPEL 3DS SERVER *******************************");
		ThreeDSecureResponse threeDsRes = new ThreeDSecureResponse();
		//if (urlThreeDSS == null)
		//	throw new ThreeDSecureRequestorException("[urlThreeDSS] field value is NULL");
		if (authInitRequest == null)
			throw new ThreeDSecureRequestorException("[authInitRequest] field instance is NULL");
		// try {
		// ReflectionUtil.executeNullFieldsChecker(authInitRequest);
		// } catch(IllegalArgumentException e) {
		// throw new ThreeDSecureRequestorException(e.getMessage(), e);
		// }
		try {
			threeDsRes = callThreeDSServer();
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				Util.writeInFileTransaction(logFolder, logFile, "********* 2eme TENTATIVE ************");
				try {
					threeDsRes = callThreeDSServer();
				} catch (Exception e2) {
					throw new ThreeDSecureRequestorException((e2.getCause() != null) ? e2.getCause() : e2);
				}
			} else
				throw new ThreeDSecureRequestorException((e.getCause() != null) ? e.getCause() : e);
		}
		Util.writeInFileTransaction(logFolder, logFile,
				"**************************** FIN APPEL 3DS SERVER *********************************");
		return threeDsRes;
	}

	protected ThreeDSecureResponse callThreeDSServer()
			throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		Util.writeInFileTransaction(logFolder, logFile, "*********** DEBUT callThreeDSServer ***********");

		HttpClient httpClient = HttpClientBuilder.create().build();
		httpClient = getAllSSLClient();
		//HttpPost httpPost = new HttpPost(urlThreeDSS);
		HttpPost httpPost = new HttpPost(authInitRequest.getUrlThreeDSS());
		ThreeDSecureResponse threeDsRes = new ThreeDSecureResponse();

		final String jsonBody = gson.toJson(authInitRequest);
		// added 2023-03-22 pcidss carte
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
		Util.writeInFileTransaction(logFolder, logFile, "*********** jsonBodyPCIDSS ***********" + jsonBodyPCIDSS.toString());
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
			Util.writeInFileTransaction(logFolder, logFile,
					"response StatusCode : " + response.getStatusLine().getStatusCode());
			Util.writeInFileTransaction(logFolder, logFile, "responseStatusLine : " + responseStatusLine);
			Util.writeInFileTransaction(logFolder, logFile, "response : " + response);
			String respStr = EntityUtils.toString(response.getEntity());
			Util.writeInFileTransaction(logFolder, logFile, "respStr : " + respStr);
			// String responseStr = IOUtils.toString(is, "UTF-8");
			// responseStr = "{\"reponseMPI\":\"Y\"}";

			threeDsRes = gson.fromJson(respStr, ThreeDSecureResponse.class);
			Util.writeInFileTransaction(logFolder, logFile, "threeDsRes toString : " + threeDsRes);

			// ((InputStream) httpClient).close();

		} catch (ClientProtocolException e) {
			Util.writeInFileTransaction(logFolder, logFile, "[GW-EXCEPTION-ClientProtocolException] " + e);
		} catch (IOException e) {
			Util.writeInFileTransaction(logFolder, logFile, "[GW-EXCEPTION-IOException] " + e);
		}

		Util.writeInFileTransaction(logFolder, logFile, "*********** FIN callThreeDSServer ***********");
		return threeDsRes;
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
