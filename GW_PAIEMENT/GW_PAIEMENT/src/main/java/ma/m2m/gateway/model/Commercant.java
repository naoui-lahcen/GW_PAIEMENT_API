package ma.m2m.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="COMMERCANT")
public class Commercant  implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="CMR_CODE")
	private String cmrCode;
	
	@Column(name="CMR_DATCRT")
	private Date cmrDatcrt;
	
	@Column(name="CMR_NOM")
	private String cmrNom;
	
	@Column(name="CMR_ABRVNOM")
	private String cmrAbrvnom;
	
	@Column(name="CMR_ADRS1")
	private String cmrAdrs1;
	
	@Column(name="CMR_ABRVADRS")
	private String cmrAbrvadrs;
	
	@Column(name="CMR_ADRS2")
	private String cmrAdrs2;
	
	@Column(name="CMR_CODPOSTAL")
	private String cmrCodpostal;
	
	@Column(name="CMR_TEL")
	private String cmrTel;
	
	@Column(name="CMR_FAX")
	private String cmrFax;
	
	@Column(name="CMR_CODACTIVITE")
	private String cmrCodactivite;
	
	@Column(name="CMR_CODPAYS")
	private String cmrCodpays;
	
	@Column(name="CMR_CODZONE")
	private String cmrCodzone;
	
	@Column(name="CMR_CODBQE")
	private String cmrCodbqe;
	
	@Column(name="CMR_CODAGENCE")
	private String cmrCodagence;
	
	@Column(name="CMR_NUMBADGE")
	private String cmrNumbadge;
	
	@Column(name="CMR_NUMCMR")
	private String cmrNumcmr;
	
	@Column(name="CMR_TYPCRT")
	private String cmrTypcrt;
	
	@Column(name="CMR_DATDPR")
	private Date cmrDatdpr;
	
	@Column(name="CMR_DATDEBUT")
	private Date cmrDatdebut;
	
	@Column(name="CMR_DATFIN")
	private Date cmrDatfin;
	
	@Column(name="CMR_TEST")
	private Character cmrTest;
	
	@Column(name="CMR_ETAT")
	private String cmrEtat;
	
	@Column(name="CMR_CMRIN")
	private Character cmrCmrin;
	
	@Column(name="CMR_UPSTAT")
	private Character cmrUpstat;
	
	@Column(name="CMR_PATENT")
	private String cmrPatent;
	
	@Column(name="CMR_CODVIL")
	private String cmrCodvil;
	
	@Column(name="CMR_SURVEL")
	private Character cmrSurvel;
	
	@Column(name="CMR_NOMCMR")
	private String cmrNomcmr;
	
	@Column(name="CMR_LIBCHEQ")
	private String cmrLibcheq;
	
	@Column(name="CMR_TYPE")
	private Character cmrType;
	
	@Column(name="CMR_CPTRELEVE")
	private Integer cmrCptreleve;
	
	@Column(name="CMR_GNRFRAIS")
	private Character cmrGnrfrais;
	
	@Column(name="CMR_ETATPRD")
	private String cmrEtatprd;
	
	@Column(name="CMR_ACTIF")
	private Character cmrActif;
	
	@Column(name="CMR_ADRSP1")
	private String cmrAdrsp1;
	
	@Column(name="CMR_ADRSP2")
	private String cmrAdrsp2;
	
	@Column(name="CMR_ADRSRLV")
	private Character cmrAdrsrlv;
	
	@Column(name="CMR_CODPOSTP")
	private String cmrCodpostp;
	
	@Column(name="CMR_LOCALITE")
	private String cmrLocalite;
	
	@Column(name="CMR_PAYS")
	private String cmrPays;
	
	private transient String email;

	public Commercant() {
	}

	public Commercant(String cmrCode, Date cmrDatcrt, String cmrNom, String cmrAbrvnom, String cmrAdrs1,
			String cmrAbrvadrs, String cmrAdrs2, String cmrCodpostal, String cmrTel, String cmrFax,
			String cmrCodactivite, String cmrCodpays, String cmrCodzone, String cmrCodbqe, String cmrCodagence,
			String cmrNumbadge, String cmrNumcmr, String cmrTypcrt, Date cmrDatdpr, Date cmrDatdebut, Date cmrDatfin,
			Character cmrTest, String cmrEtat, Character cmrCmrin, Character cmrUpstat, String cmrPatent,
			String cmrCodvil, Character cmrSurvel, String cmrNomcmr, String cmrLibcheq, Character cmrType,
			Integer cmrCptreleve, Character cmrGnrfrais, String cmrEtatprd, Character cmrActif, String cmrAdrsp1,
			String cmrAdrsp2, Character cmrAdrsrlv, String cmrCodpostp, String cmrLocalite, String cmrPays) {
		this.cmrCode = cmrCode;
		this.cmrDatcrt = cmrDatcrt;
		this.cmrNom = cmrNom;
		this.cmrAbrvnom = cmrAbrvnom;
		this.cmrAdrs1 = cmrAdrs1;
		this.cmrAbrvadrs = cmrAbrvadrs;
		this.cmrAdrs2 = cmrAdrs2;
		this.cmrCodpostal = cmrCodpostal;
		this.cmrTel = cmrTel;
		this.cmrFax = cmrFax;
		this.cmrCodactivite = cmrCodactivite;
		this.cmrCodpays = cmrCodpays;
		this.cmrCodzone = cmrCodzone;
		this.cmrCodbqe = cmrCodbqe;
		this.cmrCodagence = cmrCodagence;
		this.cmrNumbadge = cmrNumbadge;
		this.cmrNumcmr = cmrNumcmr;
		this.cmrTypcrt = cmrTypcrt;
		this.cmrDatdpr = cmrDatdpr;
		this.cmrDatdebut = cmrDatdebut;
		this.cmrDatfin = cmrDatfin;
		this.cmrTest = cmrTest;
		this.cmrEtat = cmrEtat;
		this.cmrCmrin = cmrCmrin;
		this.cmrUpstat = cmrUpstat;
		this.cmrPatent = cmrPatent;
		this.cmrCodvil = cmrCodvil;
		this.cmrSurvel = cmrSurvel;
		this.cmrNomcmr = cmrNomcmr;
		this.cmrLibcheq = cmrLibcheq;
		this.cmrType = cmrType;
		this.cmrCptreleve = cmrCptreleve;
		this.cmrGnrfrais = cmrGnrfrais;
		this.cmrEtatprd = cmrEtatprd;
		this.cmrActif = cmrActif;
		this.cmrAdrsp1 = cmrAdrsp1;
		this.cmrAdrsp2 = cmrAdrsp2;
		this.cmrAdrsrlv = cmrAdrsrlv;
		this.cmrCodpostp = cmrCodpostp;
		this.cmrLocalite = cmrLocalite;
		this.cmrPays = cmrPays;
	}

	public String getCmrCode() {
		return this.cmrCode;
	}

	public void setCmrCode(String cmrCode) {
		this.cmrCode = cmrCode;
	}

	public Date getCmrDatcrt() {
		return this.cmrDatcrt;
	}

	public void setCmrDatcrt(Date cmrDatcrt) {
		this.cmrDatcrt = cmrDatcrt;
	}

	public String getCmrNom() {
		return this.cmrNom;
	}

	public void setCmrNom(String cmrNom) {
		this.cmrNom = cmrNom;
	}

	public String getCmrAbrvnom() {
		return this.cmrAbrvnom;
	}

	public void setCmrAbrvnom(String cmrAbrvnom) {
		this.cmrAbrvnom = cmrAbrvnom;
	}

	public String getCmrAdrs1() {
		return this.cmrAdrs1;
	}

	public void setCmrAdrs1(String cmrAdrs1) {
		this.cmrAdrs1 = cmrAdrs1;
	}

	public String getCmrAbrvadrs() {
		return this.cmrAbrvadrs;
	}

	public void setCmrAbrvadrs(String cmrAbrvadrs) {
		this.cmrAbrvadrs = cmrAbrvadrs;
	}

	public String getCmrAdrs2() {
		return this.cmrAdrs2;
	}

	public void setCmrAdrs2(String cmrAdrs2) {
		this.cmrAdrs2 = cmrAdrs2;
	}

	public String getCmrCodpostal() {
		return this.cmrCodpostal;
	}

	public void setCmrCodpostal(String cmrCodpostal) {
		this.cmrCodpostal = cmrCodpostal;
	}

	public String getCmrTel() {
		return this.cmrTel;
	}

	public void setCmrTel(String cmrTel) {
		this.cmrTel = cmrTel;
	}

	public String getCmrFax() {
		return this.cmrFax;
	}

	public void setCmrFax(String cmrFax) {
		this.cmrFax = cmrFax;
	}

	public String getCmrCodactivite() {
		return this.cmrCodactivite;
	}

	public void setCmrCodactivite(String cmrCodactivite) {
		this.cmrCodactivite = cmrCodactivite;
	}

	public String getCmrCodpays() {
		return this.cmrCodpays;
	}

	public void setCmrCodpays(String cmrCodpays) {
		this.cmrCodpays = cmrCodpays;
	}

	public String getCmrCodzone() {
		return this.cmrCodzone;
	}

	public void setCmrCodzone(String cmrCodzone) {
		this.cmrCodzone = cmrCodzone;
	}

	public String getCmrCodbqe() {
		return this.cmrCodbqe;
	}

	public void setCmrCodbqe(String cmrCodbqe) {
		this.cmrCodbqe = cmrCodbqe;
	}

	public String getCmrCodagence() {
		return this.cmrCodagence;
	}

	public void setCmrCodagence(String cmrCodagence) {
		this.cmrCodagence = cmrCodagence;
	}

	public String getCmrNumbadge() {
		return this.cmrNumbadge;
	}

	public void setCmrNumbadge(String cmrNumbadge) {
		this.cmrNumbadge = cmrNumbadge;
	}

	public String getCmrNumcmr() {
		return this.cmrNumcmr;
	}

	public void setCmrNumcmr(String cmrNumcmr) {
		this.cmrNumcmr = cmrNumcmr;
	}

	public String getCmrTypcrt() {
		return this.cmrTypcrt;
	}

	public void setCmrTypcrt(String cmrTypcrt) {
		this.cmrTypcrt = cmrTypcrt;
	}

	public Date getCmrDatdpr() {
		return this.cmrDatdpr;
	}

	public void setCmrDatdpr(Date cmrDatdpr) {
		this.cmrDatdpr = cmrDatdpr;
	}

	public Date getCmrDatdebut() {
		return this.cmrDatdebut;
	}

	public void setCmrDatdebut(Date cmrDatdebut) {
		this.cmrDatdebut = cmrDatdebut;
	}

	public Date getCmrDatfin() {
		return this.cmrDatfin;
	}

	public void setCmrDatfin(Date cmrDatfin) {
		this.cmrDatfin = cmrDatfin;
	}

	public Character getCmrTest() {
		return this.cmrTest;
	}

	public void setCmrTest(Character cmrTest) {
		this.cmrTest = cmrTest;
	}

	public String getCmrEtat() {
		return this.cmrEtat;
	}

	public void setCmrEtat(String cmrEtat) {
		this.cmrEtat = cmrEtat;
	}

	public Character getCmrCmrin() {
		return this.cmrCmrin;
	}

	public void setCmrCmrin(Character cmrCmrin) {
		this.cmrCmrin = cmrCmrin;
	}

	public Character getCmrUpstat() {
		return this.cmrUpstat;
	}

	public void setCmrUpstat(Character cmrUpstat) {
		this.cmrUpstat = cmrUpstat;
	}

	public String getCmrPatent() {
		return this.cmrPatent;
	}

	public void setCmrPatent(String cmrPatent) {
		this.cmrPatent = cmrPatent;
	}

	public String getCmrCodvil() {
		return this.cmrCodvil;
	}

	public void setCmrCodvil(String cmrCodvil) {
		this.cmrCodvil = cmrCodvil;
	}

	public Character getCmrSurvel() {
		return this.cmrSurvel;
	}

	public void setCmrSurvel(Character cmrSurvel) {
		this.cmrSurvel = cmrSurvel;
	}

	public String getCmrNomcmr() {
		return this.cmrNomcmr;
	}

	public void setCmrNomcmr(String cmrNomcmr) {
		this.cmrNomcmr = cmrNomcmr;
	}

	public String getCmrLibcheq() {
		return this.cmrLibcheq;
	}

	public void setCmrLibcheq(String cmrLibcheq) {
		this.cmrLibcheq = cmrLibcheq;
	}

	public Character getCmrType() {
		return this.cmrType;
	}

	public void setCmrType(Character cmrType) {
		this.cmrType = cmrType;
	}

	public Integer getCmrCptreleve() {
		return this.cmrCptreleve;
	}

	public void setCmrCptreleve(Integer cmrCptreleve) {
		this.cmrCptreleve = cmrCptreleve;
	}

	public Character getCmrGnrfrais() {
		return this.cmrGnrfrais;
	}

	public void setCmrGnrfrais(Character cmrGnrfrais) {
		this.cmrGnrfrais = cmrGnrfrais;
	}

	public String getCmrEtatprd() {
		return this.cmrEtatprd;
	}

	public void setCmrEtatprd(String cmrEtatprd) {
		this.cmrEtatprd = cmrEtatprd;
	}

	public Character getCmrActif() {
		return this.cmrActif;
	}

	public void setCmrActif(Character cmrActif) {
		this.cmrActif = cmrActif;
	}

	public String getCmrAdrsp1() {
		return this.cmrAdrsp1;
	}

	public void setCmrAdrsp1(String cmrAdrsp1) {
		this.cmrAdrsp1 = cmrAdrsp1;
	}

	public String getCmrAdrsp2() {
		return this.cmrAdrsp2;
	}

	public void setCmrAdrsp2(String cmrAdrsp2) {
		this.cmrAdrsp2 = cmrAdrsp2;
	}

	public Character getCmrAdrsrlv() {
		return this.cmrAdrsrlv;
	}

	public void setCmrAdrsrlv(Character cmrAdrsrlv) {
		this.cmrAdrsrlv = cmrAdrsrlv;
	}

	public String getCmrCodpostp() {
		return this.cmrCodpostp;
	}

	public void setCmrCodpostp(String cmrCodpostp) {
		this.cmrCodpostp = cmrCodpostp;
	}

	public String getCmrLocalite() {
		return this.cmrLocalite;
	}

	public void setCmrLocalite(String cmrLocalite) {
		this.cmrLocalite = cmrLocalite;
	}

	public String getCmrPays() {
		return this.cmrPays;
	}

	public void setCmrPays(String cmrPays) {
		this.cmrPays = cmrPays;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Commercant [cmrCode=" + cmrCode + ", cmrDatcrt=" + cmrDatcrt + ", cmrNom=" + cmrNom + ", cmrAbrvnom="
				+ cmrAbrvnom + ", cmrAdrs1=" + cmrAdrs1 + ", cmrAbrvadrs=" + cmrAbrvadrs + ", cmrAdrs2=" + cmrAdrs2
				+ ", cmrCodpostal=" + cmrCodpostal + ", cmrTel=" + cmrTel + ", cmrFax=" + cmrFax + ", cmrCodactivite="
				+ cmrCodactivite + ", cmrCodpays=" + cmrCodpays + ", cmrCodzone=" + cmrCodzone + ", cmrCodbqe="
				+ cmrCodbqe + ", cmrCodagence=" + cmrCodagence + ", cmrNumbadge=" + cmrNumbadge + ", cmrNumcmr="
				+ cmrNumcmr + ", cmrTypcrt=" + cmrTypcrt + ", cmrDatdpr=" + cmrDatdpr + ", cmrDatdebut=" + cmrDatdebut
				+ ", cmrDatfin=" + cmrDatfin + ", cmrTest=" + cmrTest + ", cmrEtat=" + cmrEtat + ", cmrCmrin="
				+ cmrCmrin + ", cmrUpstat=" + cmrUpstat + ", cmrPatent=" + cmrPatent + ", cmrCodvil=" + cmrCodvil
				+ ", cmrSurvel=" + cmrSurvel + ", cmrNomcmr=" + cmrNomcmr + ", cmrLibcheq=" + cmrLibcheq + ", cmrType="
				+ cmrType + ", cmrCptreleve=" + cmrCptreleve + ", cmrGnrfrais=" + cmrGnrfrais + ", cmrEtatprd="
				+ cmrEtatprd + ", cmrActif=" + cmrActif + ", cmrAdrsp1=" + cmrAdrsp1 + ", cmrAdrsp2=" + cmrAdrsp2
				+ ", cmrAdrsrlv=" + cmrAdrsrlv + ", cmrCodpostp=" + cmrCodpostp + ", cmrLocalite=" + cmrLocalite
				+ ", cmrPays=" + cmrPays + ", email=" + email + "]";
	}


}
