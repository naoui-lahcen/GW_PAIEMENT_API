package ma.m2m.gateway.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;
@Entity
@Table(name = "TELECOLLECTE")
//@Component
public class Telecollecte implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 248680037522737855L;

	
	

	
	@Column(name = "TLC_NUMCMR")
	private String tlc_numcmr;
	@Column(name = "TLC_NUMTPE")
	private String tlc_numtpe;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TLC_NUMTLCOLCTE")
	private Long tlc_numtlcolcte;
	@Column(name = "TLC_NUMREMISE")
	private Double tlc_numremise;
	@Column(name = "TLC_NUMFICH")
	private Double tlc_numfich;
	@Column(name = "TLC_AKWNBR")
	private String tlc_akwnbr;
	@Column(name = "TLC_MESIMP")
	private String tlc_mesimp;
	@Column(name = "TLC_DATREMISE")
	private Date tlc_datremise;
	@Column(name = "TLC_HEUREMISE")
	private String tlc_heuremise;
	@Column(name = "TLC_DATCRTFICH")
	private Date tlc_datcrtfich;
	@Column(name = "TLC_NBRTRANS")
	private Double tlc_nbrtrans;
	@Column(name = "TLC_GEST")
	private String tlc_gest;
	@Column(name = "TLC_TYPENTRE")
	private String tlc_typentre;
	@Column(name = "TLC_ESCOMPTE")
	private String tlc_escompte;
	@Column(name = "TLC_CODBQ")
	private String tlc_codbq;
	@Column(name = "TLC_FILEID")
	private String tlc_fileid;
	
	
	
	
	
	
	
	public Telecollecte() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	
	
	
	
	public Telecollecte(String tlc_numcmr, String tlc_numtpe, Long tlc_numtlcolcte, Double tlc_numremise,
			Double tlc_numfich, String tlc_akwnbr, String tlc_mesimp, Date tlc_datremise, String tlc_heuremise,
			Date tlc_datcrtfich, Double tlc_nbrtrans, String tlc_gest, String tlc_typentre, String tlc_escompte,
			String tlc_codbq, String tlc_fileid) {
		super();
		this.tlc_numcmr = tlc_numcmr;
		this.tlc_numtpe = tlc_numtpe;
		this.tlc_numtlcolcte = tlc_numtlcolcte;
		this.tlc_numremise = tlc_numremise;
		this.tlc_numfich = tlc_numfich;
		this.tlc_akwnbr = tlc_akwnbr;
		this.tlc_mesimp = tlc_mesimp;
		this.tlc_datremise = tlc_datremise;
		this.tlc_heuremise = tlc_heuremise;
		this.tlc_datcrtfich = tlc_datcrtfich;
		this.tlc_nbrtrans = tlc_nbrtrans;
		this.tlc_gest = tlc_gest;
		this.tlc_typentre = tlc_typentre;
		this.tlc_escompte = tlc_escompte;
		this.tlc_codbq = tlc_codbq;
		this.tlc_fileid = tlc_fileid;
	}








	public String getTlc_numcmr() {
		return tlc_numcmr;
	}
	public void setTlc_numcmr(String tlc_numcmr) {
		this.tlc_numcmr = tlc_numcmr;
	}
	public String getTlc_numtpe() {
		return tlc_numtpe;
	}
	public void setTlc_numtpe(String tlc_numtpe) {
		this.tlc_numtpe = tlc_numtpe;
	}
	public Long getTlc_numtlcolcte() {
		return tlc_numtlcolcte;
	}
	public void setTlc_numtlcolcte(Long tlc_numtlcolcte) {
		this.tlc_numtlcolcte = tlc_numtlcolcte;
	}
	public Double getTlc_numremise() {
		return tlc_numremise;
	}
	public void setTlc_numremise(Double tlc_numremise) {
		this.tlc_numremise = tlc_numremise;
	}
	public Double getTlc_numfich() {
		return tlc_numfich;
	}
	public void setTlc_numfich(Double tlc_numfich) {
		this.tlc_numfich = tlc_numfich;
	}
	public String getTlc_akwnbr() {
		return tlc_akwnbr;
	}
	public void setTlc_akwnbr(String tlc_akwnbr) {
		this.tlc_akwnbr = tlc_akwnbr;
	}
	public String getTlc_mesimp() {
		return tlc_mesimp;
	}
	public void setTlc_mesimp(String tlc_mesimp) {
		this.tlc_mesimp = tlc_mesimp;
	}
	public Date getTlc_datremise() {
		return tlc_datremise;
	}
	public void setTlc_datremise(Date tlc_datremise) {
		this.tlc_datremise = tlc_datremise;
	}
	public String getTlc_heuremise() {
		return tlc_heuremise;
	}
	public void setTlc_heuremise(String tlc_heuremise) {
		this.tlc_heuremise = tlc_heuremise;
	}
	public Date getTlc_datcrtfich() {
		return tlc_datcrtfich;
	}
	public void setTlc_datcrtfich(Date tlc_datcrtfich) {
		this.tlc_datcrtfich = tlc_datcrtfich;
	}
	public Double getTlc_nbrtrans() {
		return tlc_nbrtrans;
	}
	public void setTlc_nbrtrans(Double tlc_nbrtrans) {
		this.tlc_nbrtrans = tlc_nbrtrans;
	}
	public String getTlc_gest() {
		return tlc_gest;
	}
	public void setTlc_gest(String tlc_gest) {
		this.tlc_gest = tlc_gest;
	}
	public String getTlc_typentre() {
		return tlc_typentre;
	}
	public void setTlc_typentre(String tlc_typentre) {
		this.tlc_typentre = tlc_typentre;
	}
	public String getTlc_escompte() {
		return tlc_escompte;
	}
	public void setTlc_escompte(String tlc_escompte) {
		this.tlc_escompte = tlc_escompte;
	}
	public String getTlc_codbq() {
		return tlc_codbq;
	}
	public void setTlc_codbq(String tlc_codbq) {
		this.tlc_codbq = tlc_codbq;
	}
	public String getTlc_fileid() {
		return tlc_fileid;
	}
	public void setTlc_fileid(String tlc_fileid) {
		this.tlc_fileid = tlc_fileid;
	}








	@Override
	public String toString() {
		return "Telecollecte [tlc_numcmr=" + tlc_numcmr + ", tlc_numtpe=" + tlc_numtpe + ", tlc_numtlcolcte="
				+ tlc_numtlcolcte + ", tlc_numremise=" + tlc_numremise + ", tlc_numfich=" + tlc_numfich
				+ ", tlc_akwnbr=" + tlc_akwnbr + ", tlc_mesimp=" + tlc_mesimp + ", tlc_datremise=" + tlc_datremise
				+ ", tlc_heuremise=" + tlc_heuremise + ", tlc_datcrtfich=" + tlc_datcrtfich + ", tlc_nbrtrans="
				+ tlc_nbrtrans + ", tlc_gest=" + tlc_gest + ", tlc_typentre=" + tlc_typentre + ", tlc_escompte="
				+ tlc_escompte + ", tlc_codbq=" + tlc_codbq + ", tlc_fileid=" + tlc_fileid + "]";
	}
	
	
	
	
	
	
	
	
}
