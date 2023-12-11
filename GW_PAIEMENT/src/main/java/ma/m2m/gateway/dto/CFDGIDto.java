package ma.m2m.gateway.dto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

public class CFDGIDto {

	private Integer idCFDGI;
	
	private String cF_R_OICodeclient;
	
	private String cF_R_OINReference;
	
	private String cF_R_OIConfirmUrl;
	
	private String cF_R_OIemail;
	
	private String cF_R_OIMtTotal;
	
	private String cF_R_OICodeOper;
	
	private String cF_R_OIUpdateURL;
	
	private String offerURL;
	
	private String cF_R_OIRefFacture;
	
	private int iddemande;
	
	private String refReglement;
	
	private String codeRtour;
	
	private String msg;
	
	private String refcanal;

	public CFDGIDto() {
		super();
	}

	public Integer getIdCFDGI() {
		return idCFDGI;
	}
	public void setIdCFDGI(Integer idCFDGI) {
		this.idCFDGI = idCFDGI;
	}
	public String getcF_R_OICodeclient() {
		return cF_R_OICodeclient;
	}
	public void setcF_R_OICodeclient(String cF_R_OICodeclient) {
		this.cF_R_OICodeclient = cF_R_OICodeclient;
	}
	public String getcF_R_OINReference() {
		return cF_R_OINReference;
	}
	public void setcF_R_OINReference(String cF_R_OINReference) {
		this.cF_R_OINReference = cF_R_OINReference;
	}
	public String getcF_R_OIConfirmUrl() {
		return cF_R_OIConfirmUrl;
	}
	public void setcF_R_OIConfirmUrl(String cF_R_OIConfirmUrl) {
		this.cF_R_OIConfirmUrl = cF_R_OIConfirmUrl;
	}
	public String getcF_R_OIemail() {
		return cF_R_OIemail;
	}
	public void setcF_R_OIemail(String cF_R_OIemail) {
		this.cF_R_OIemail = cF_R_OIemail;
	}
	public String getcF_R_OIMtTotal() {
		return cF_R_OIMtTotal;
	}
	public void setcF_R_OIMtTotal(String cF_R_OIMtTotal) {
		this.cF_R_OIMtTotal = cF_R_OIMtTotal;
	}
	public String getcF_R_OICodeOper() {
		return cF_R_OICodeOper;
	}
	public void setcF_R_OICodeOper(String cF_R_OICodeOper) {
		this.cF_R_OICodeOper = cF_R_OICodeOper;
	}
	public String getcF_R_OIUpdateURL() {
		return cF_R_OIUpdateURL;
	}
	public void setcF_R_OIUpdateURL(String cF_R_OIUpdateURL) {
		this.cF_R_OIUpdateURL = cF_R_OIUpdateURL;
	}
	public String getOfferURL() {
		return offerURL;
	}
	public void setOfferURL(String offerURL) {
		this.offerURL = offerURL;
	}
	public String getcF_R_OIRefFacture() {
		return cF_R_OIRefFacture;
	}
	public void setcF_R_OIRefFacture(String cF_R_OIRefFacture) {
		this.cF_R_OIRefFacture = cF_R_OIRefFacture;
	}
	public int getIddemande() {
		return iddemande;
	}
	public void setIddemande(int iddemande) {
		this.iddemande = iddemande;
	}
	public String getRefReglement() {
		return refReglement;
	}

	public void setRefReglement(String refReglement) {
		this.refReglement = refReglement;
	}

	public String getCodeRtour() {
		return codeRtour;
	}
	public void setCodeRtour(String codeRetour) {
		this.codeRtour = codeRetour;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getRefcanal() {
		return refcanal;
	}

	public void setRefcanal(String refcanal) {
		this.refcanal = refcanal;
	}

	public CFDGIDto(Integer idCFDGI, String cF_R_OICodeclient, String cF_R_OINReference, String cF_R_OIConfirmUrl,
			String cF_R_OIemail, String cF_R_OIMtTotal, String cF_R_OICodeOper, String cF_R_OIUpdateURL,
			String offerURL, String cF_R_OIRefFacture, int iddemande, String refReglement, String codeRtour, String msg,
			String refcanal) {
		super();
		this.idCFDGI = idCFDGI;
		this.cF_R_OICodeclient = cF_R_OICodeclient;
		this.cF_R_OINReference = cF_R_OINReference;
		this.cF_R_OIConfirmUrl = cF_R_OIConfirmUrl;
		this.cF_R_OIemail = cF_R_OIemail;
		this.cF_R_OIMtTotal = cF_R_OIMtTotal;
		this.cF_R_OICodeOper = cF_R_OICodeOper;
		this.cF_R_OIUpdateURL = cF_R_OIUpdateURL;
		this.offerURL = offerURL;
		this.cF_R_OIRefFacture = cF_R_OIRefFacture;
		this.iddemande = iddemande;
		this.refReglement = refReglement;
		this.codeRtour = codeRtour;
		this.msg = msg;
		this.refcanal = refcanal;
	}

	@Override
	public String toString() {
		return "CFDGIDto [idCFDGI=" + idCFDGI + ", cF_R_OICodeclient=" + cF_R_OICodeclient + ", cF_R_OINReference="
				+ cF_R_OINReference + ", cF_R_OIConfirmUrl=" + cF_R_OIConfirmUrl + ", cF_R_OIemail=" + cF_R_OIemail
				+ ", cF_R_OIMtTotal=" + cF_R_OIMtTotal + ", cF_R_OICodeOper=" + cF_R_OICodeOper + ", cF_R_OIUpdateURL="
				+ cF_R_OIUpdateURL + ", offerURL=" + offerURL + ", cF_R_OIRefFacture=" + cF_R_OIRefFacture
				+ ", iddemande=" + iddemande + ", refReglement=" + refReglement + ", codeRtour=" + codeRtour + ", msg="
				+ msg + ", refcanal=" + refcanal + "]";
	}

}
