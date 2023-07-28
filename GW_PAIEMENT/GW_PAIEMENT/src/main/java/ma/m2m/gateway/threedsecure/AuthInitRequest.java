package ma.m2m.gateway.threedsecure;

import lombok.Data;
import ma.m2m.gateway.Utils.Util;
//@Data
public class AuthInitRequest {
	
	private String pan;
	
	private Double amount;
	
	private String currency;
	
	private String idCommercant;
	
	private Integer idDemande;
	
	/*
	 * Format: YYMM
	 */
	private String expiry; 
	
	private String acquirerBIN;
	
	private String browserAcceptHeader;
	
	private String browserUserAgent;
	
	private String email;
	
	private String mcc;
	
	private String merchantCountryCode;
	
	private String nomCommercant;
	
	private String notificationURL;
	
	private String threeDSRequestorAuthenticationInd = "01";
	
	private String messageCategory = "01";
	
	private String urlThreeDSS;

	public AuthInitRequest() {
		super();
	}

	public AuthInitRequest(String pan, Double amount, String currency, String idCommercant, Integer idDemande,
			String expiry, String acquirerBIN, String browserAcceptHeader, String browserUserAgent, String email,
			String mcc, String merchantCountryCode, String nomCommercant, String notificationURL,
			String threeDSRequestorAuthenticationInd, String messageCategory, String urlThreeDSS) {
		super();
		this.pan = pan;
		this.amount = amount;
		this.currency = currency;
		this.idCommercant = idCommercant;
		this.idDemande = idDemande;
		this.expiry = expiry;
		this.acquirerBIN = acquirerBIN;
		this.browserAcceptHeader = browserAcceptHeader;
		this.browserUserAgent = browserUserAgent;
		this.email = email;
		this.mcc = mcc;
		this.merchantCountryCode = merchantCountryCode;
		this.nomCommercant = nomCommercant;
		this.notificationURL = notificationURL;
		this.threeDSRequestorAuthenticationInd = threeDSRequestorAuthenticationInd;
		this.messageCategory = messageCategory;
		this.urlThreeDSS = urlThreeDSS;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getIdCommercant() {
		return idCommercant;
	}

	public void setIdCommercant(String idCommercant) {
		this.idCommercant = idCommercant;
	}

	public Integer getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(Integer idDemande) {
		this.idDemande = idDemande;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getAcquirerBIN() {
		return acquirerBIN;
	}

	public void setAcquirerBIN(String acquirerBIN) {
		this.acquirerBIN = acquirerBIN;
	}

	public String getBrowserAcceptHeader() {
		return browserAcceptHeader;
	}

	public void setBrowserAcceptHeader(String browserAcceptHeader) {
		this.browserAcceptHeader = browserAcceptHeader;
	}

	public String getBrowserUserAgent() {
		return browserUserAgent;
	}

	public void setBrowserUserAgent(String browserUserAgent) {
		this.browserUserAgent = browserUserAgent;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getMerchantCountryCode() {
		return merchantCountryCode;
	}

	public void setMerchantCountryCode(String merchantCountryCode) {
		this.merchantCountryCode = merchantCountryCode;
	}

	public String getNomCommercant() {
		return nomCommercant;
	}

	public void setNomCommercant(String nomCommercant) {
		this.nomCommercant = nomCommercant;
	}

	public String getNotificationURL() {
		return notificationURL;
	}

	public void setNotificationURL(String notificationURL) {
		this.notificationURL = notificationURL;
	}

	public String getThreeDSRequestorAuthenticationInd() {
		return threeDSRequestorAuthenticationInd;
	}

	public void setThreeDSRequestorAuthenticationInd(String threeDSRequestorAuthenticationInd) {
		this.threeDSRequestorAuthenticationInd = threeDSRequestorAuthenticationInd;
	}

	public String getMessageCategory() {
		return messageCategory;
	}

	public void setMessageCategory(String messageCategory) {
		this.messageCategory = messageCategory;
	}

	public String getUrlThreeDSS() {
		return urlThreeDSS;
	}

	public void setUrlThreeDSS(String urlThreeDSS) {
		this.urlThreeDSS = urlThreeDSS;
	}
	
	// modified by lnaoui 2023-03-22 Util.displayCard(pan) display carte pcidss
	@Override
	public String toString() {
		return "AuthInitRequest [pan=" + Util.displayCard(pan) + ", amount=" + amount + ", currency=" + currency + ", idCommercant="
				+ idCommercant + ", idDemande=" + idDemande + ", expiry=" + expiry + ", acquirerBIN=" + acquirerBIN
				+ ", browserAcceptHeader=" + browserAcceptHeader + ", browserUserAgent=" + browserUserAgent + ", email="
				+ email + ", mcc=" + mcc + ", merchantCountryCode=" + merchantCountryCode + ", nomCommercant="
				+ nomCommercant + ", notificationURL=" + notificationURL + ", threeDSRequestorAuthenticationInd="
				+ threeDSRequestorAuthenticationInd + ", messageCategory=" + messageCategory + ", urlThreeDSS="
				+ urlThreeDSS + "]";
	}
	

}
