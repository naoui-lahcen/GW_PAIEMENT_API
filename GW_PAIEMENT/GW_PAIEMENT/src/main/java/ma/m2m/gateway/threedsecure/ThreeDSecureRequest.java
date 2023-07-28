package ma.m2m.gateway.threedsecure;

import lombok.Data;

@Data
public class ThreeDSecureRequest {

	private String threeDSSURL;
	private String notificationURL;
	private String threeDSRequestorAuthenticationInd;
	private String messageCategory;

	private String pan;
	private Double amount;
	private String currency;
	private String idComercant;
	private Integer idDemande;
	private String expiry; // YYMM
	private String acquirerBIN;
	private String browserAcceptHeader;
	private String browserUserAgent;
	private String email;
	private String mcc;
	private String merchantCountryCode;
	private String nomCommercant;
	
	public String getThreeDSSURL() {
		return threeDSSURL;
	}
	public void setThreeDSSURL(String threeDSSURL) {
		this.threeDSSURL = threeDSSURL;
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
	public String getIdComercant() {
		return idComercant;
	}
	public void setIdComercant(String idComercant) {
		this.idComercant = idComercant;
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

}
