package ma.m2m.gateway.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="INFO_COMMERCANT")
public class InfoCommercant implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ID_INFO")
	private long idInfo;
	
	@Column(name="CMR_CODE")
	private String cmrCode;
	
	@Column(name="CMR_EMAIL")
	private String cmrEmail;
	
	@Column(name="PASSWORD")
	private String password;
	
	@Column(name="PASSWORD_MPI")
	private String passwordMpi;
	
	@Column(name="CLE_PUB")
	private String clePub;
	
	@Column(name="CLE_PRIV")
	private String clePriv;
	
	@Column(name="CMR_NOM")
	private String cmrNom;
	
	@Column(name="CMR_PWD")
	private String cmrPwd;
	
	@Column(name="CMR_BIN")
	private String cmrBin;
	
	@Column(name="CMR_URL")
	private String cmrUrl;
	
	@Column(name="CMR_VILLE")
	private String cmrVille;
	
	@Column(name="CMR_CURRENCY")
	private String cmrCurrency;
	
	@Column(name="CMR_EXPONENT")
	private String cmrExponent;
	
	@Column(name="CMR_PURCHAMONT")
	private String cmrPurchamont;
	
	@Column(name="cle_api")
	private String cle_api;
	
	@Column(name="api_url")
    private String api_url;


	public InfoCommercant() {
	}

	public InfoCommercant(long idInfo) {
		this.idInfo = idInfo;
	}



	@Override
	public String toString() {
		return "InfoCommercant [idInfo=" + idInfo + ", cmrCode=" + cmrCode + ", cmrEmail=" + cmrEmail + ", password="
				+ password + ", passwordMpi=" + passwordMpi + ", clePub=" + clePub + ", clePriv=" + clePriv
				+ ", cmrNom=" + cmrNom + ", cmrPwd=" + cmrPwd + ", cmrBin=" + cmrBin + ", cmrUrl=" + cmrUrl
				+ ", cmrVille=" + cmrVille + ", cmrCurrency=" + cmrCurrency + ", cmrExponent=" + cmrExponent
				+ ", cmrPurchamont=" + cmrPurchamont + ", cle_api=" + cle_api
				+ ", api_url=" + api_url + "]";
	}

	

	
	
	public InfoCommercant(long idInfo, String cmrCode, String cmrEmail, String password, String passwordMpi,
			String clePub, String clePriv, String cmrNom, String cmrPwd, String cmrBin, String cmrUrl, String cmrVille,
			String cmrCurrency, String cmrExponent, String cmrPurchamont, String cle_api,
			String api_url) {
		super();
		this.idInfo = idInfo;
		this.cmrCode = cmrCode;
		this.cmrEmail = cmrEmail;
		this.password = password;
		this.passwordMpi = passwordMpi;
		this.clePub = clePub;
		this.clePriv = clePriv;
		this.cmrNom = cmrNom;
		this.cmrPwd = cmrPwd;
		this.cmrBin = cmrBin;
		this.cmrUrl = cmrUrl;
		this.cmrVille = cmrVille;
		this.cmrCurrency = cmrCurrency;
		this.cmrExponent = cmrExponent;
		this.cmrPurchamont = cmrPurchamont;
		this.cle_api = cle_api;
		this.api_url = api_url;
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

	public long getIdInfo() {
		return this.idInfo;
	}

	public void setIdInfo(long idInfo) {
		this.idInfo = idInfo;
	}

	public String getCmrCode() {
		return this.cmrCode;
	}

	public void setCmrCode(String cmrCode) {
		this.cmrCode = cmrCode;
	}

	public String getCmrEmail() {
		return this.cmrEmail;
	}

	public void setCmrEmail(String cmrEmail) {
		this.cmrEmail = cmrEmail;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordMpi() {
		return this.passwordMpi;
	}

	public void setPasswordMpi(String passwordMpi) {
		this.passwordMpi = passwordMpi;
	}

	public String getClePub() {
		return this.clePub;
	}

	public void setClePub(String clePub) {
		this.clePub = clePub;
	}

	public String getClePriv() {
		return this.clePriv;
	}

	public void setClePriv(String clePriv) {
		this.clePriv = clePriv;
	}

	public String getCmrNom() {
		return this.cmrNom;
	}

	public void setCmrNom(String cmrNom) {
		this.cmrNom = cmrNom;
	}

	public String getCmrPwd() {
		return this.cmrPwd;
	}

	public void setCmrPwd(String cmrPwd) {
		this.cmrPwd = cmrPwd;
	}

	public String getCmrBin() {
		return this.cmrBin;
	}

	public void setCmrBin(String cmrBin) {
		this.cmrBin = cmrBin;
	}

	public String getCmrUrl() {
		return this.cmrUrl;
	}

	public void setCmrUrl(String cmrUrl) {
		this.cmrUrl = cmrUrl;
	}

	public String getCmrVille() {
		return this.cmrVille;
	}

	public void setCmrVille(String cmrVille) {
		this.cmrVille = cmrVille;
	}

	public String getCmrCurrency() {
		return this.cmrCurrency;
	}

	public void setCmrCurrency(String cmrCurrency) {
		this.cmrCurrency = cmrCurrency;
	}

	public String getCmrExponent() {
		return this.cmrExponent;
	}

	public void setCmrExponent(String cmrExponent) {
		this.cmrExponent = cmrExponent;
	}

	public String getCmrPurchamont() {
		return this.cmrPurchamont;
	}

	public void setCmrPurchamont(String cmrPurchamont) {
		this.cmrPurchamont = cmrPurchamont;
	}


}
