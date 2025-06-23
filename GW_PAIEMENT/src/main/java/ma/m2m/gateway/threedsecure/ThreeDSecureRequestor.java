package ma.m2m.gateway.threedsecure;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.utils.Util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Slf4j
public class ThreeDSecureRequestor {

    private Gson gson;

    private AuthInitRequest authInitRequest;

	public ThreeDSecureRequestor() {
		super();
	}

	public ThreeDSecureRequestor(String logFolder, String logFile) {
        this.gson = new GsonBuilder().serializeNulls().create();
	}

	public void threeDSecureRequest(final AuthInitRequest authInitRequest) {
		this.authInitRequest = authInitRequest;
	}

	@SuppressWarnings("all")
	public ThreeDSecureResponse initAuth(String logFolder, String logFile) throws ThreeDSecureRequestorException {
		Util.writeInFileTransaction(logFolder, logFile, 
				"*********** DEBUT initAuth ***********");
		ThreeDSecureResponse threeDsRes = new ThreeDSecureResponse();

		if (authInitRequest == null)
			throw new ThreeDSecureRequestorException("[authInitRequest] field instance is NULL");

		try {
			threeDsRes = callThreeDSServer(logFolder, logFile);
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				Util.writeInFileTransaction(logFolder, logFile, "********* 2eme TENTATIVE ************");
				try {
					threeDsRes = callThreeDSServer(logFolder, logFile);
				} catch (Exception e2) {
					throw new ThreeDSecureRequestorException((e2.getCause() != null) ? e2.getCause() : e2);
				}
			} else
				throw new ThreeDSecureRequestorException((e.getCause() != null) ? e.getCause() : e);
		}
		Util.writeInFileTransaction(logFolder, logFile,
				"*********** End initAuth ***********");
		return threeDsRes;
	}

	@SuppressWarnings("all")
	protected ThreeDSecureResponse callThreeDSServer(String logFolder, String logFile)
			throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		Util.writeInFileTransaction(logFolder, logFile, "*********** DEBUT callThreeDSServer ***********");

		HttpClient httpClient = HttpClientBuilder.create().build();
		httpClient = getAllSSLClient();
		ThreeDSecureResponse threeDSecureResponse = new ThreeDSecureResponse();

		try {
			
			HttpPost httpPost = new HttpPost(authInitRequest.getUrlThreeDSS());

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
			// synchronisation du gw avec mpi
			authInitReqPCIDSS.setMerchantName(authInitRequest.getMerchantName());
			authInitReqPCIDSS.setPurchaseAmount(authInitRequest.getPurchaseAmount());
			authInitReqPCIDSS.setAcctNumber(Util.displayCard(authInitRequest.getAcctNumber()));
			authInitReqPCIDSS.setPurchaseCurrency(authInitRequest.getPurchaseCurrency().trim());
			authInitReqPCIDSS.setAcquirerMerchantID(authInitRequest.getAcquirerMerchantID());
			authInitReqPCIDSS.setCardExpiryDate(authInitRequest.getCardExpiryDate());
			authInitReqPCIDSS.setPurchaseDate(authInitRequest.getPurchaseDate());
			
			final String jsonBodyPCIDSS = gson.toJson(authInitReqPCIDSS);
			Util.writeInFileTransaction(logFolder, logFile, "*********** jsonBodyPCIDSS ***********" + jsonBodyPCIDSS.toString());
			// added 2023-03-22 pcidss carte
			
			final StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);

			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			HttpResponse response = httpClient.execute(httpPost);

			StatusLine responseStatusLine = response.getStatusLine();
			Util.writeInFileTransaction(logFolder, logFile, "Response StatusCode : " + responseStatusLine.getStatusCode());
			
			//Util.writeInFileTransaction(logFolder, logFile, "response : " + response);
			
			String responseStr = EntityUtils.toString(response.getEntity());
			Util.writeInFileTransaction(logFolder, logFile, "Ares String : " + responseStr);

			threeDSecureResponse = gson.fromJson(responseStr, ThreeDSecureResponse.class);
			Util.writeInFileTransaction(logFolder, logFile,""+ threeDSecureResponse);

			// ((InputStream) httpClient).close();

		} catch (ClientProtocolException e) {
			Util.writeInFileTransaction(logFolder, logFile, "[GW-EXCEPTION-ClientProtocolException] " + Util.formatException(e));
		} catch (IOException e) {
			Util.writeInFileTransaction(logFolder, logFile, "[GW-EXCEPTION-IOException] " + Util.formatException(e));
		}

		Util.writeInFileTransaction(logFolder, logFile, "*********** End callThreeDSServer ***********");
		return threeDSecureResponse;
	}
	
	@SuppressWarnings("all")
	public static HttpClient getAllSSLClient()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

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
		} };
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

}
