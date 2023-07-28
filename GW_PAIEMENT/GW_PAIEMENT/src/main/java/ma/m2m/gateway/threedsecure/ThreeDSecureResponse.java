package ma.m2m.gateway.threedsecure;

import java.io.Serializable;

import lombok.Data;

@Data
public class ThreeDSecureResponse implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String reponseMPI;
	private String eci;
	private String cavv;
	private String threeDSServerTransID; /* xid*/
	private String idDemande;
	private String errmpi;
	private String htmlCreq;
	private String expiry; // YYMM
	
	
	
	public String getReponseMPI() {
		return reponseMPI;
	}

	public void setReponseMPI(String reponseMPI) {
		this.reponseMPI = reponseMPI;
	}

	public String getEci() {
		return eci;
	}

	public void setEci(String eci) {
		this.eci = eci;
	}

	public String getCavv() {
		return cavv;
	}

	public void setCavv(String cavv) {
		this.cavv = cavv;
	}

	public String getThreeDSServerTransID() {
		return threeDSServerTransID;
	}

	public void setThreeDSServerTransID(String threeDSServerTransID) {
		this.threeDSServerTransID = threeDSServerTransID;
	}

	public String getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(String idDemande) {
		this.idDemande = idDemande;
	}

	public String getErrmpi() {
		return errmpi;
	}

	public void setErrmpi(String errmpi) {
		this.errmpi = errmpi;
	}

	public String getHtmlCreq() {
		return htmlCreq;
	}

	public void setHtmlCreq(String htmlCreq) {
		this.htmlCreq = htmlCreq;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public ThreeDSecureResponse() {
		super();
	}
	
	public ThreeDSecureResponse(String reponseMPI, String eci, String cavv, String threeDSServerTransID,
			String idDemande, String errmpi, String htmlCreq, String expiry) {
		super();
		this.reponseMPI = reponseMPI;
		this.eci = eci;
		this.cavv = cavv;
		this.threeDSServerTransID = threeDSServerTransID;
		this.idDemande = idDemande;
		this.errmpi = errmpi;
		this.htmlCreq = htmlCreq;
		this.expiry = expiry;
	}

	@Override
	public String toString() {
		return "ThreeDSecureResponse [reponseMPI=" + reponseMPI + ", eci=" + eci + ", cavv=" + cavv
				+ ", threeDSServerTransID=" + threeDSServerTransID + ", idDemande=" + idDemande + ", errmpi=" + errmpi
				+ ", htmlCreq=" + htmlCreq + ", expiry=" + expiry + "]";
	}

}
