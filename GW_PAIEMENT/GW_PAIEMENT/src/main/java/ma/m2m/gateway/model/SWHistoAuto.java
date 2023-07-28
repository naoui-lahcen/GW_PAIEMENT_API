package ma.m2m.gateway.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

@Entity
@Table(name = "HISTOAUTO", schema = "MXSWITCH")
//@Component
public class SWHistoAuto implements Serializable {

	private static final long serialVersionUID = 9049139417921994967L;

	public SWHistoAuto() {
		super();
	}

	@Id
	@Column(name = "HAT_NUMDEM")
	private int hat_numdem;

	@Column(name = "HAT_NUMCMR")
	private String hat_numcmr;

	@Column(name = "HAT_MCC")
	private String hat_mcc;

	@Column(name = "HAT_DATDEM")
	private Date hat_datdem;

	@Column(name = "HAT_HERDEM")
	private String hat_herdem;

	@Column(name = "HAT_CODTPE")
	private String hat_codtpe;

	@Column(name = "HAT_PORTEUR")
	private String hat_porteur;

	@Column(name = "HAT_MONTANT")
	private Double hat_montant;

	@Column(name = "HAT_DEVISE")
	private String hat_devise;

	@Column(name = "HAT_CODEREP")
	private String hat_coderep;

	@Column(name = "HAT_NAUTSRV")
	private String hat_nautsrv;

	@Column(name = "HAT_NCOMPTE")
	private String hat_ncompte;

	@Column(name = "HAT_NAUTEMT")
	private String hat_nautemt;

	@Column(name = "HAT_CODTRNS")
	private String hat_codtrns;

	@Column(name = "HAT_NREFCE")
	private String hat_nrefce;

	@Column(name = "HAT_MTFREF1")
	private String hat_mtfref1;

	@Column(name = "HAT_MTFREF2")
	private String hat_mtfref2;

	@Column(name = "HAT_PROCODE")
	private String hat_procode;

	@Column(name = "HAT_REASREQ")
	private String hat_reasreq;

	@Column(name = "HAT_ANOMALIE")
	private String hat_anomalie;

	@Column(name = "HAT_INSTANCE")
	private String hat_instance;

	@Column(name = "HAT_APPELBQE")
	private String hat_appelbqe;

	@Column(name = "HAT_CODPAY")
	private String hat_codpay;

	@Column(name = "HAT_DEMANDEUR")
	private String hat_demandeur;

	@Column(name = "HAT_REPONDEUR")
	private String hat_repondeur;

	@Column(name = "HAT_NOMDEANDEUR")
	private String hat_nomdeandeur;

	@Column(name = "HAT_TYPPROTO")
	private String hat_typproto;

	@Column(name = "HAT_ETAT")
	private String hat_etat;

	@Column(name = "HAT_EXPDATE")
	private String hat_expdate;

	@Column(name = "HAT_BQECMR")
	private String hat_bqecmr;

	@Column(name = "HAT_AIP")
	private String hat_aip;

	@Column(name = "HAT_ATC")
	private String hat_atc;

	@Column(name="HAT_AC")
	private String hat_ac;

	@Column(name="HAT_CID")
	private String hat_cid;

	@Column(name="HAT_CVMRES")
	private String hat_cvmres;

	@Column(name="HAT_SERNUM")
	private String hat_sernum;

	@Column(name="HAT_IAD")
	private String hat_iad;

	@Column(name="HAT_TERMCAPAB")
	private String hat_termcapab;

	@Column(name="HAT_TERMTYP")
	private String hat_termtyp;

	@Column(name="HAT_TVR")
	private String hat_tvr;

	@Column(name="HAT_UN")
	private String hat_un;

	@Column(name="HAT_MNTAUT")
	private String hat_mntaut;

	@Column(name="HAT_MNTOTH")
	private String hat_mntoth;

	@Column(name="HAT_APPEFFDAT")
	private Date hat_appeffdat;

	@Column(name="HAT_APPEXPDAT")
	private Date hat_appexpdat;

	@Column(name="HAT_PANSN")
	private String hat_pansn;

	@Column(name="HAT_PIN")
	private String hat_pin;

	@Column(name="HAT_PANMOD")
	private String hat_panmod;

	@Column(name="HAT_TERCOUCOD")
	private String hat_tercoucod;

	@Column(name="HAT_CODEDEVISE")
	private String hat_codedevise;

	@Column(name="HAT_TRSDATE")
	private Date hat_trsdate;

	@Column(name="HAT_TRSTYP")
	private String hat_trstyp;

	@Column(name="HAT_ISSAUTH")
	private String hat_issauth;

	@Column(name="HAT_ISSSCRIPT")
	private String hat_issscript;

	public int getHat_numdem() {
		return hat_numdem;
	}

	public void setHat_numdem(int hat_numdem) {
		this.hat_numdem = hat_numdem;
	}

	public String getHat_numcmr() {
		return hat_numcmr;
	}

	public void setHat_numcmr(String hat_numcmr) {
		this.hat_numcmr = hat_numcmr;
	}

	public String getHat_mcc() {
		return hat_mcc;
	}

	public void setHat_mcc(String hat_mcc) {
		this.hat_mcc = hat_mcc;
	}

	public Date getHat_datdem() {
		return hat_datdem;
	}

	public void setHat_datdem(Date hat_datdem) {
		this.hat_datdem = hat_datdem;
	}

	public String getHat_herdem() {
		return hat_herdem;
	}

	public void setHat_herdem(String hat_herdem) {
		this.hat_herdem = hat_herdem;
	}

	public String getHat_codtpe() {
		return hat_codtpe;
	}

	public void setHat_codtpe(String hat_codtpe) {
		this.hat_codtpe = hat_codtpe;
	}

	public String getHat_porteur() {
		return hat_porteur;
	}

	public void setHat_porteur(String hat_porteur) {
		this.hat_porteur = hat_porteur;
	}

	public Double getHat_montant() {
		return hat_montant;
	}

	public void setHat_montant(Double hat_montant) {
		this.hat_montant = hat_montant;
	}

	public String getHat_devise() {
		return hat_devise;
	}

	public void setHat_devise(String hat_devise) {
		this.hat_devise = hat_devise;
	}

	public String getHat_coderep() {
		return hat_coderep;
	}

	public void setHat_coderep(String hat_coderep) {
		this.hat_coderep = hat_coderep;
	}

	public String getHat_nautsrv() {
		return hat_nautsrv;
	}

	public void setHat_nautsrv(String hat_nautsrv) {
		this.hat_nautsrv = hat_nautsrv;
	}

	public String getHat_ncompte() {
		return hat_ncompte;
	}

	public void setHat_ncompte(String hat_ncompte) {
		this.hat_ncompte = hat_ncompte;
	}

	public String getHat_nautemt() {
		return hat_nautemt;
	}

	public void setHat_nautemt(String hat_nautemt) {
		this.hat_nautemt = hat_nautemt;
	}

	public String getHat_codtrns() {
		return hat_codtrns;
	}

	public void setHat_codtrns(String hat_codtrns) {
		this.hat_codtrns = hat_codtrns;
	}

	public String getHat_nrefce() {
		return hat_nrefce;
	}

	public void setHat_nrefce(String hat_nrefce) {
		this.hat_nrefce = hat_nrefce;
	}

	public String getHat_mtfref1() {
		return hat_mtfref1;
	}

	public void setHat_mtfref1(String hat_mtfref1) {
		this.hat_mtfref1 = hat_mtfref1;
	}

	public String getHat_mtfref2() {
		return hat_mtfref2;
	}

	public void setHat_mtfref2(String hat_mtfref2) {
		this.hat_mtfref2 = hat_mtfref2;
	}

	public String getHat_procode() {
		return hat_procode;
	}

	public void setHat_procode(String hat_procode) {
		this.hat_procode = hat_procode;
	}

	public String getHat_reasreq() {
		return hat_reasreq;
	}

	public void setHat_reasreq(String hat_reasreq) {
		this.hat_reasreq = hat_reasreq;
	}

	public String getHat_anomalie() {
		return hat_anomalie;
	}

	public void setHat_anomalie(String hat_anomalie) {
		this.hat_anomalie = hat_anomalie;
	}

	public String getHat_instance() {
		return hat_instance;
	}

	public void setHat_instance(String hat_instance) {
		this.hat_instance = hat_instance;
	}

	public String getHat_appelbqe() {
		return hat_appelbqe;
	}

	public void setHat_appelbqe(String hat_appelbqe) {
		this.hat_appelbqe = hat_appelbqe;
	}

	public String getHat_codpay() {
		return hat_codpay;
	}

	public void setHat_codpay(String hat_codpay) {
		this.hat_codpay = hat_codpay;
	}

	public String getHat_demandeur() {
		return hat_demandeur;
	}

	public void setHat_demandeur(String hat_demandeur) {
		this.hat_demandeur = hat_demandeur;
	}

	public String getHat_repondeur() {
		return hat_repondeur;
	}

	public void setHat_repondeur(String hat_repondeur) {
		this.hat_repondeur = hat_repondeur;
	}

	public String getHat_nomdeandeur() {
		return hat_nomdeandeur;
	}

	public void setHat_nomdeandeur(String hat_nomdeandeur) {
		this.hat_nomdeandeur = hat_nomdeandeur;
	}

	public String getHat_typproto() {
		return hat_typproto;
	}

	public void setHat_typproto(String hat_typproto) {
		this.hat_typproto = hat_typproto;
	}

	public String getHat_etat() {
		return hat_etat;
	}

	public void setHat_etat(String hat_etat) {
		this.hat_etat = hat_etat;
	}

	public String getHat_expdate() {
		return hat_expdate;
	}

	public void setHat_expdate(String hat_expdate) {
		this.hat_expdate = hat_expdate;
	}

	public String getHat_bqecmr() {
		return hat_bqecmr;
	}

	public void setHat_bqecmr(String hat_bqecmr) {
		this.hat_bqecmr = hat_bqecmr;
	}

	public String getHat_aip() {
		return hat_aip;
	}

	public void setHat_aip(String hat_aip) {
		this.hat_aip = hat_aip;
	}

	public String getHat_atc() {
		return hat_atc;
	}

	public void setHat_atc(String hat_atc) {
		this.hat_atc = hat_atc;
	}

	public String getHat_ac() {
		return hat_ac;
	}

	public void setHat_ac(String hat_ac) {
		this.hat_ac = hat_ac;
	}

	public String getHat_cid() {
		return hat_cid;
	}

	public void setHat_cid(String hat_cid) {
		this.hat_cid = hat_cid;
	}

	public String getHat_cvmres() {
		return hat_cvmres;
	}

	public void setHat_cvmres(String hat_cvmres) {
		this.hat_cvmres = hat_cvmres;
	}

	public String getHat_sernum() {
		return hat_sernum;
	}

	public void setHat_sernum(String hat_sernum) {
		this.hat_sernum = hat_sernum;
	}

	public String getHat_iad() {
		return hat_iad;
	}

	public void setHat_iad(String hat_iad) {
		this.hat_iad = hat_iad;
	}

	public String getHat_termcapab() {
		return hat_termcapab;
	}

	public void setHat_termcapab(String hat_termcapab) {
		this.hat_termcapab = hat_termcapab;
	}

	public String getHat_termtyp() {
		return hat_termtyp;
	}

	public void setHat_termtyp(String hat_termtyp) {
		this.hat_termtyp = hat_termtyp;
	}

	public String getHat_tvr() {
		return hat_tvr;
	}

	public void setHat_tvr(String hat_tvr) {
		this.hat_tvr = hat_tvr;
	}

	public String getHat_un() {
		return hat_un;
	}

	public void setHat_un(String hat_un) {
		this.hat_un = hat_un;
	}

	public String getHat_mntaut() {
		return hat_mntaut;
	}

	public void setHat_mntaut(String hat_mntaut) {
		this.hat_mntaut = hat_mntaut;
	}

	public String getHat_mntoth() {
		return hat_mntoth;
	}

	public void setHat_mntoth(String hat_mntoth) {
		this.hat_mntoth = hat_mntoth;
	}

	public Date getHat_appeffdat() {
		return hat_appeffdat;
	}

	public void setHat_appeffdat(Date hat_appeffdat) {
		this.hat_appeffdat = hat_appeffdat;
	}

	public Date getHat_appexpdat() {
		return hat_appexpdat;
	}

	public void setHat_appexpdat(Date hat_appexpdat) {
		this.hat_appexpdat = hat_appexpdat;
	}

	public String getHat_pansn() {
		return hat_pansn;
	}

	public void setHat_pansn(String hat_pansn) {
		this.hat_pansn = hat_pansn;
	}

	public String getHat_pin() {
		return hat_pin;
	}

	public void setHat_pin(String hat_pin) {
		this.hat_pin = hat_pin;
	}

	public String getHat_panmod() {
		return hat_panmod;
	}

	public void setHat_panmod(String hat_panmod) {
		this.hat_panmod = hat_panmod;
	}

	public String getHat_tercoucod() {
		return hat_tercoucod;
	}

	public void setHat_tercoucod(String hat_tercoucod) {
		this.hat_tercoucod = hat_tercoucod;
	}

	public String getHat_codedevise() {
		return hat_codedevise;
	}

	public void setHat_codedevise(String hat_codedevise) {
		this.hat_codedevise = hat_codedevise;
	}

	public Date getHat_trsdate() {
		return hat_trsdate;
	}

	public void setHat_trsdate(Date hat_trsdate) {
		this.hat_trsdate = hat_trsdate;
	}

	public String getHat_trstyp() {
		return hat_trstyp;
	}

	public void setHat_trstyp(String hat_trstyp) {
		this.hat_trstyp = hat_trstyp;
	}

	public String getHat_issauth() {
		return hat_issauth;
	}

	public void setHat_issauth(String hat_issauth) {
		this.hat_issauth = hat_issauth;
	}

	public String getHat_issscript() {
		return hat_issscript;
	}

	public void setHat_issscript(String hat_issscript) {
		this.hat_issscript = hat_issscript;
	}

	@Override
	public String toString() {
		return "SWHistoAuto [hat_numdem=" + hat_numdem + ", hat_numcmr=" + hat_numcmr + ", hat_mcc=" + hat_mcc
				+ ", hat_datdem=" + hat_datdem + ", hat_herdem=" + hat_herdem + ", hat_codtpe=" + hat_codtpe
				+ ", hat_porteur=" + hat_porteur + ", hat_montant=" + hat_montant + ", hat_devise=" + hat_devise
				+ ", hat_coderep=" + hat_coderep + ", hat_nautsrv=" + hat_nautsrv + ", hat_ncompte=" + hat_ncompte
				+ ", hat_nautemt=" + hat_nautemt + ", hat_codtrns=" + hat_codtrns + ", hat_nrefce=" + hat_nrefce
				+ ", hat_mtfref1=" + hat_mtfref1 + ", hat_mtfref2=" + hat_mtfref2 + ", hat_procode=" + hat_procode
				+ ", hat_reasreq=" + hat_reasreq + ", hat_anomalie=" + hat_anomalie + ", hat_instance=" + hat_instance
				+ ", hat_appelbqe=" + hat_appelbqe + ", hat_codpay=" + hat_codpay + ", hat_demandeur=" + hat_demandeur
				+ ", hat_repondeur=" + hat_repondeur + ", hat_nomdeandeur=" + hat_nomdeandeur + ", hat_typproto="
				+ hat_typproto + ", hat_etat=" + hat_etat + ", hat_expdate=" + hat_expdate + ", hat_bqecmr="
				+ hat_bqecmr + ", hat_aip=" + hat_aip + ", hat_atc=" + hat_atc + ", hat_ac=" + hat_ac + ", hat_cid="
				+ hat_cid + ", hat_cvmres=" + hat_cvmres + ", hat_sernum=" + hat_sernum + ", hat_iad=" + hat_iad
				+ ", hat_termcapab=" + hat_termcapab + ", hat_termtyp=" + hat_termtyp + ", hat_tvr=" + hat_tvr
				+ ", hat_un=" + hat_un + ", hat_mntaut=" + hat_mntaut + ", hat_mntoth=" + hat_mntoth
				+ ", hat_appeffdat=" + hat_appeffdat + ", hat_appexpdat=" + hat_appexpdat + ", hat_pansn=" + hat_pansn
				+ ", hat_pin=" + hat_pin + ", hat_panmod=" + hat_panmod + ", hat_tercoucod=" + hat_tercoucod
				+ ", hat_codedevise=" + hat_codedevise + ", hat_trsdate=" + hat_trsdate + ", hat_trstyp=" + hat_trstyp
				+ ", hat_issauth=" + hat_issauth + ", hat_issscript=" + hat_issscript + "]";
	}

}
