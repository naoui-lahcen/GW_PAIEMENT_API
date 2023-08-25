package ma.m2m.gateway.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoCommercantDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long idInfo;
	
	private String cmrCode;
	
	private String cmrEmail;
	
	private String password;
	
	private String passwordMpi;
	
	private String clePub;
	
	private String clePriv;
	
	private String cmrNom;
	
	private String cmrPwd;
	
	private String cmrBin;
	
	private String cmrUrl;
	
	private String cmrVille;
	
	private String cmrCurrency;
	
	private String cmrExponent;
	
	private String cmrPurchamont;
	
	private String cle_api;
	
    private String api_url;

	public long getIdInfo() {
		return idInfo;
	}

	public void setIdInfo(long idInfo) {
		this.idInfo = idInfo;
	}

	public String getCmrCode() {
		return cmrCode;
	}

	public void setCmrCode(String cmrCode) {
		this.cmrCode = cmrCode;
	}

	public String getCmrEmail() {
		return cmrEmail;
	}

	public void setCmrEmail(String cmrEmail) {
		this.cmrEmail = cmrEmail;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordMpi() {
		return passwordMpi;
	}

	public void setPasswordMpi(String passwordMpi) {
		this.passwordMpi = passwordMpi;
	}

	public String getClePub() {
		return clePub;
	}

	public void setClePub(String clePub) {
		this.clePub = clePub;
	}

	public String getClePriv() {
		return clePriv;
	}

	public void setClePriv(String clePriv) {
		this.clePriv = clePriv;
	}

	public String getCmrNom() {
		return cmrNom;
	}

	public void setCmrNom(String cmrNom) {
		this.cmrNom = cmrNom;
	}

	public String getCmrPwd() {
		return cmrPwd;
	}

	public void setCmrPwd(String cmrPwd) {
		this.cmrPwd = cmrPwd;
	}

	public String getCmrBin() {
		return cmrBin;
	}

	public void setCmrBin(String cmrBin) {
		this.cmrBin = cmrBin;
	}

	public String getCmrUrl() {
		return cmrUrl;
	}

	public void setCmrUrl(String cmrUrl) {
		this.cmrUrl = cmrUrl;
	}

	public String getCmrVille() {
		return cmrVille;
	}

	public void setCmrVille(String cmrVille) {
		this.cmrVille = cmrVille;
	}

	public String getCmrCurrency() {
		return cmrCurrency;
	}

	public void setCmrCurrency(String cmrCurrency) {
		this.cmrCurrency = cmrCurrency;
	}

	public String getCmrExponent() {
		return cmrExponent;
	}

	public void setCmrExponent(String cmrExponent) {
		this.cmrExponent = cmrExponent;
	}

	public String getCmrPurchamont() {
		return cmrPurchamont;
	}

	public void setCmrPurchamont(String cmrPurchamont) {
		this.cmrPurchamont = cmrPurchamont;
	}

	public String getCle_api() {
		return cle_api;
	}

	public void setCle_api(String cle_api) {
		this.cle_api = cle_api;
	}

	public String getApi_url() {
		return api_url;
	}

	public void setApi_url(String api_url) {
		this.api_url = api_url;
	}

	@Override
	public String toString() {
		return "InfoCommercantDto [idInfo=" + idInfo + ", cmrCode=" + cmrCode + ", cmrEmail=" + cmrEmail + ", password="
				+ password + ", passwordMpi=" + passwordMpi + ", clePub=" + clePub + ", clePriv=" + clePriv
				+ ", cmrNom=" + cmrNom + ", cmrPwd=" + cmrPwd + ", cmrBin=" + cmrBin + ", cmrUrl=" + cmrUrl
				+ ", cmrVille=" + cmrVille + ", cmrCurrency=" + cmrCurrency + ", cmrExponent=" + cmrExponent
				+ ", cmrPurchamont=" + cmrPurchamont + ", cle_api=" + cle_api + ", api_url=" + api_url + "]";
	}
    
    

}
