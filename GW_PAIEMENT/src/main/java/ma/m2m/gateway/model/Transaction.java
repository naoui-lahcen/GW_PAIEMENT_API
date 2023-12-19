package ma.m2m.gateway.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name = "TRANSACTION")
public class Transaction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5885509636447853241L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TRS_ID")	
	private long trs_id;
	
	@Column(name = "TRS_NUMTRAIT")
	private Double trs_numtrait;
	@Column(name = "TRS_NUMCMR")
	private String trsnumcmr;
	@Column(name = "TRS_NUMTLCOLCTE")
	private Double trs_numtlcolcte;
	@Column(name = "TRS_CODTPE")
	private Double  trs_codtpe;
	@Column(name = "TRS_NUMFACT")
	private Double  trs_numfact;
	@Column(name = "TRS_NUMBLOC")
	private Double  trs_numbloc;
	@Column(name = "TRS_NUMSEQ")
	private Double  trs_numseq;
	@Column(name = "TRS_CODPORTEUR")
    private String 	trs_codporteur;
	@Column(name = "TRS_NOCOMPTE")
	private String trs_nocompte;
	@Column(name = "TRS_TYPTOP")
	private String  trs_typtop;
	@Column(name = "TRS_PROCOD")
	private String trs_procod;
	@Column(name = "TRS_MONTANT")
	private Double  trs_montant;
	@Column(name = "TRS_ECART")	
	private Double  trs_ecart;
	@Column(name = "TRS_TYPCARTE")	
	private String  trs_typcarte;
	@Column(name = "TRS_SYSPAY")	
	private String  trs_syspay;
	@Column(name = "TRS_MODVAL")	
	private String   trs_modval;
	@Column(name = "TRS_MAPRELEAS")	
	private String trs_mapreleas;
	@Column(name = "TRS_ORIGINPOR")	
	private String  trs_originpor;
	@Column(name = "TRS_CODBQE")	
	private String  trs_codbqe;
	@Column(name = "TRS_GROUPE")	
	private String  trs_groupe;
	@Column(name = "TRS_DATTRANS")	
	private Date   trs_dattrans;
	@Column(name = "TRS_NUMAUT")	
	private String  trsnumaut;
	@Column(name = "TRS_NUMRELEVEPORT")	
	private Double   trs_numreleveport;
	@Column(name = "TRS_NUMRELEVECMR")	
	private Double trs_numrelevecmr;
	@Column(name = "TRS_NUMEDITION")	
	private Double   trs_numedition;
	@Column(name = "TRS_ANNULE")	
	private String  trs_annule;
	@Column(name = "TRS_ETAT")	
	private String  trs_etat;
	@Column(name = "TRS_ARN")	
	private String  trs_arn;
	@Column(name = "TRS_DEVISE")	
	private String  trs_devise;
	@Column(name = "TRS_ICA")	
	private String  trs_ica;
	@Column(name = "TRS_BQCMR")	
	private String  trs_bqcmr;
	@Column(name = "TRS_ORIGINE")	
	private String  trs_origine;
	@Column(name = "TRS_CHGREAS")	
	private String  trs_chgreas;
	@Column(name = "TRS_POSCAPBI")	
	private String  trs_poscapbi;
	@Column(name = "TRS_CDHLIDMT")	
	private String  trs_cdhlidmt;
	@Column(name = "TRS_POSENTRY")	
	private String  trs_posentry;
	@Column(name = "TRS_MAILIN")	
	private String  trs_mailin;
	@Column(name = "TRS_HEURETRS")	
	private String  trs_heuretrs;
	@Column(name = "TRS_MNTSRC")	
	private Double   trs_mntsrc;
	@Column(name = "TRS_DEVSRC")	
	private String  trs_devsrc;
	@Column(name = "TRS_CPTCMR")	
	private String  trs_cptcmr;
	@Column(name = "TRS_IMPDATE")	
	private Date    trs_impdate;
	@Column(name = "TRS_TRTDATE")	
	private Date     trs_trtdate;
	@Column(name = "TRS_EXPDATE")	
	private Date    trs_expdate;
	@Column(name = "TRS_TRSTYPE")	
	private String  trs_trstype;
	@Column(name = "TRS_CODOPR")	
	private String  trs_codopr;
	@Column(name = "TRS_CERTIF")	
	private String  trs_certif;
	@Column(name = "TRS_PURSEFILE")	
	private String  trs_pursefile;
	@Column(name = "TRS_DEBTRANSC")	
	private String  trs_debtransc;
	@Column(name = "TRS_CREDTRANSC")	
	private String  trs_credtransc;
	@Column(name = "TRS_PURSECOD")	
	private String  trs_pursecod;
	@Column(name = "TRS_NUMREMISE")	
	private String  trs_numremise;
	@Column(name = "TRS_DATFIN")	
	private Date    trs_datfin;
	@Column(name = "TRS_ORDRECARTE")	
	private String  trs_ordrecarte;

	@Column(name = "TRS_COMMANDE")	
	private String   trs_commande;
	
	
	
	
	
	
	public Transaction() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Transaction(Double trs_numtrait, String trs_numcmr, Double trs_numtlcolcte, Double trs_codtpe,
			Double trs_numfact, Double trs_numbloc, Double trs_numseq, String trs_codporteur, String trs_nocompte,
			String trs_typtop, String trs_procod, Double trs_montant, Double trs_ecart, String trs_typcarte,
			String trs_syspay, String trs_modval, String trs_mapreleas, String trs_originpor, String trs_codbqe,
			String trs_groupe, Date trs_dattrans, String trsnumaut, Double trs_numreleveport, Double trs_numrelevecmr,
			Double trs_numedition, String trs_annule, String trs_etat, String trs_arn, String trs_devise,
			String trs_ica, String trs_bqcmr, String trs_origine, String trs_chgreas, String trs_poscapbi,
			String trs_cdhlidmt, String trs_posentry, String trs_mailin, String trs_heuretrs, Double trs_mntsrc,
			String trs_devsrc, String trs_cptcmr, Date trs_impdate, Date trs_trtdate, Date trs_expdate,
			String trs_trstype, String trs_codopr, String trs_certif, String trs_pursefile, String trs_debtransc,
			String trs_credtransc, String trs_pursecod, String trs_numremise, Date trs_datfin, String trs_ordrecarte,
			long trs_id, String trs_commande) {
		super();
		this.trs_numtrait = trs_numtrait;
		this.trsnumcmr = trs_numcmr;
		this.trs_numtlcolcte = trs_numtlcolcte;
		this.trs_codtpe = trs_codtpe;
		this.trs_numfact = trs_numfact;
		this.trs_numbloc = trs_numbloc;
		this.trs_numseq = trs_numseq;
		this.trs_codporteur = trs_codporteur;
		this.trs_nocompte = trs_nocompte;
		this.trs_typtop = trs_typtop;
		this.trs_procod = trs_procod;
		this.trs_montant = trs_montant;
		this.trs_ecart = trs_ecart;
		this.trs_typcarte = trs_typcarte;
		this.trs_syspay = trs_syspay;
		this.trs_modval = trs_modval;
		this.trs_mapreleas = trs_mapreleas;
		this.trs_originpor = trs_originpor;
		this.trs_codbqe = trs_codbqe;
		this.trs_groupe = trs_groupe;
		this.trs_dattrans = trs_dattrans;
		this.trsnumaut = trsnumaut;
		this.trs_numreleveport = trs_numreleveport;
		this.trs_numrelevecmr = trs_numrelevecmr;
		this.trs_numedition = trs_numedition;
		this.trs_annule = trs_annule;
		this.trs_etat = trs_etat;
		this.trs_arn = trs_arn;
		this.trs_devise = trs_devise;
		this.trs_ica = trs_ica;
		this.trs_bqcmr = trs_bqcmr;
		this.trs_origine = trs_origine;
		this.trs_chgreas = trs_chgreas;
		this.trs_poscapbi = trs_poscapbi;
		this.trs_cdhlidmt = trs_cdhlidmt;
		this.trs_posentry = trs_posentry;
		this.trs_mailin = trs_mailin;
		this.trs_heuretrs = trs_heuretrs;
		this.trs_mntsrc = trs_mntsrc;
		this.trs_devsrc = trs_devsrc;
		this.trs_cptcmr = trs_cptcmr;
		this.trs_impdate = trs_impdate;
		this.trs_trtdate = trs_trtdate;
		this.trs_expdate = trs_expdate;
		this.trs_trstype = trs_trstype;
		this.trs_codopr = trs_codopr;
		this.trs_certif = trs_certif;
		this.trs_pursefile = trs_pursefile;
		this.trs_debtransc = trs_debtransc;
		this.trs_credtransc = trs_credtransc;
		this.trs_pursecod = trs_pursecod;
		this.trs_numremise = trs_numremise;
		this.trs_datfin = trs_datfin;
		this.trs_ordrecarte = trs_ordrecarte;
		this.trs_id = trs_id;
		this.trs_commande = trs_commande;
	}
	public Double getTrs_numtrait() {
		return trs_numtrait;
	}
	public void setTrs_numtrait(Double trs_numtrait) {
		this.trs_numtrait = trs_numtrait;
	}
	public String getTrsnumcmr() {
		return trsnumcmr;
	}
	public void setTrsnumcmr(String trs_numcmr) {
		this.trsnumcmr = trs_numcmr;
	}
	public Double getTrs_numtlcolcte() {
		return trs_numtlcolcte;
	}
	public void setTrs_numtlcolcte(Double trs_numtlcolcte) {
		this.trs_numtlcolcte = trs_numtlcolcte;
	}
	public Double getTrs_codtpe() {
		return trs_codtpe;
	}
	public void setTrs_codtpe(Double trs_codtpe) {
		this.trs_codtpe = trs_codtpe;
	}
	public Double getTrs_numfact() {
		return trs_numfact;
	}
	public void setTrs_numfact(Double trs_numfact) {
		this.trs_numfact = trs_numfact;
	}
	public Double getTrs_numbloc() {
		return trs_numbloc;
	}
	public void setTrs_numbloc(Double trs_numbloc) {
		this.trs_numbloc = trs_numbloc;
	}
	public Double getTrs_numseq() {
		return trs_numseq;
	}
	public void setTrs_numseq(Double trs_numseq) {
		this.trs_numseq = trs_numseq;
	}
	public String getTrs_codporteur() {
		return trs_codporteur;
	}
	public void setTrs_codporteur(String trs_codporteur) {
		this.trs_codporteur = trs_codporteur;
	}
	public String getTrs_nocompte() {
		return trs_nocompte;
	}
	public void setTrs_nocompte(String trs_nocompte) {
		this.trs_nocompte = trs_nocompte;
	}
	public String getTrs_typtop() {
		return trs_typtop;
	}
	public void setTrs_typtop(String trs_typtop) {
		this.trs_typtop = trs_typtop;
	}
	public String getTrs_procod() {
		return trs_procod;
	}
	public void setTrs_procod(String trs_procod) {
		this.trs_procod = trs_procod;
	}
	public Double getTrs_montant() {
		return trs_montant;
	}
	public void setTrs_montant(Double trs_montant) {
		this.trs_montant = trs_montant;
	}
	public Double getTrs_ecart() {
		return trs_ecart;
	}
	public void setTrs_ecart(Double trs_ecart) {
		this.trs_ecart = trs_ecart;
	}
	public String getTrs_typcarte() {
		return trs_typcarte;
	}
	public void setTrs_typcarte(String trs_typcarte) {
		this.trs_typcarte = trs_typcarte;
	}
	public String getTrs_syspay() {
		return trs_syspay;
	}
	public void setTrs_syspay(String trs_syspay) {
		this.trs_syspay = trs_syspay;
	}
	public String getTrs_modval() {
		return trs_modval;
	}
	public void setTrs_modval(String trs_modval) {
		this.trs_modval = trs_modval;
	}
	public String getTrs_mapreleas() {
		return trs_mapreleas;
	}
	public void setTrs_mapreleas(String trs_mapreleas) {
		this.trs_mapreleas = trs_mapreleas;
	}
	public String getTrs_originpor() {
		return trs_originpor;
	}
	public void setTrs_originpor(String trs_originpor) {
		this.trs_originpor = trs_originpor;
	}
	public String getTrs_codbqe() {
		return trs_codbqe;
	}
	public void setTrs_codbqe(String trs_codbqe) {
		this.trs_codbqe = trs_codbqe;
	}
	public String getTrs_groupe() {
		return trs_groupe;
	}
	public void setTrs_groupe(String trs_groupe) {
		this.trs_groupe = trs_groupe;
	}
	public Date getTrs_dattrans() {
		return trs_dattrans;
	}
	public void setTrs_dattrans(Date trs_dattrans) {
		this.trs_dattrans = trs_dattrans;
	}
	public String getTrsnumaut() {
		return trsnumaut;
	}
	public void setTrsnumaut(String trsnumaut) {
		this.trsnumaut = trsnumaut;
	}
	public Double getTrs_numreleveport() {
		return trs_numreleveport;
	}
	public void setTrs_numreleveport(Double trs_numreleveport) {
		this.trs_numreleveport = trs_numreleveport;
	}
	public Double getTrs_numrelevecmr() {
		return trs_numrelevecmr;
	}
	public void setTrs_numrelevecmr(Double trs_numrelevecmr) {
		this.trs_numrelevecmr = trs_numrelevecmr;
	}
	public Double getTrs_numedition() {
		return trs_numedition;
	}
	public void setTrs_numedition(Double trs_numedition) {
		this.trs_numedition = trs_numedition;
	}
	public String getTrs_annule() {
		return trs_annule;
	}
	public void setTrs_annule(String trs_annule) {
		this.trs_annule = trs_annule;
	}
	public String getTrs_etat() {
		return trs_etat;
	}
	public void setTrs_etat(String trs_etat) {
		this.trs_etat = trs_etat;
	}
	public String getTrs_arn() {
		return trs_arn;
	}
	public void setTrs_arn(String trs_arn) {
		this.trs_arn = trs_arn;
	}
	public String getTrs_devise() {
		return trs_devise;
	}
	public void setTrs_devise(String trs_devise) {
		this.trs_devise = trs_devise;
	}
	public String getTrs_ica() {
		return trs_ica;
	}
	public void setTrs_ica(String trs_ica) {
		this.trs_ica = trs_ica;
	}
	public String getTrs_bqcmr() {
		return trs_bqcmr;
	}
	public void setTrs_bqcmr(String trs_bqcmr) {
		this.trs_bqcmr = trs_bqcmr;
	}
	public String getTrs_origine() {
		return trs_origine;
	}
	public void setTrs_origine(String trs_origine) {
		this.trs_origine = trs_origine;
	}
	public String getTrs_chgreas() {
		return trs_chgreas;
	}
	public void setTrs_chgreas(String trs_chgreas) {
		this.trs_chgreas = trs_chgreas;
	}
	public String getTrs_poscapbi() {
		return trs_poscapbi;
	}
	public void setTrs_poscapbi(String trs_poscapbi) {
		this.trs_poscapbi = trs_poscapbi;
	}
	public String getTrs_cdhlidmt() {
		return trs_cdhlidmt;
	}
	public void setTrs_cdhlidmt(String trs_cdhlidmt) {
		this.trs_cdhlidmt = trs_cdhlidmt;
	}
	public String getTrs_posentry() {
		return trs_posentry;
	}
	public void setTrs_posentry(String trs_posentry) {
		this.trs_posentry = trs_posentry;
	}
	public String getTrs_mailin() {
		return trs_mailin;
	}
	public void setTrs_mailin(String trs_mailin) {
		this.trs_mailin = trs_mailin;
	}
	public String getTrs_heuretrs() {
		return trs_heuretrs;
	}
	public void setTrs_heuretrs(String trs_heuretrs) {
		this.trs_heuretrs = trs_heuretrs;
	}
	public Double getTrs_mntsrc() {
		return trs_mntsrc;
	}
	public void setTrs_mntsrc(Double trs_mntsrc) {
		this.trs_mntsrc = trs_mntsrc;
	}
	public String getTrs_devsrc() {
		return trs_devsrc;
	}
	public void setTrs_devsrc(String trs_devsrc) {
		this.trs_devsrc = trs_devsrc;
	}
	public String getTrs_cptcmr() {
		return trs_cptcmr;
	}
	public void setTrs_cptcmr(String trs_cptcmr) {
		this.trs_cptcmr = trs_cptcmr;
	}
	public Date getTrs_impdate() {
		return trs_impdate;
	}
	public void setTrs_impdate(Date trs_impdate) {
		this.trs_impdate = trs_impdate;
	}
	public Date getTrs_trtdate() {
		return trs_trtdate;
	}
	public void setTrs_trtdate(Date trs_trtdate) {
		this.trs_trtdate = trs_trtdate;
	}
	public Date getTrs_expdate() {
		return trs_expdate;
	}
	public void setTrs_expdate(Date trs_expdate) {
		this.trs_expdate = trs_expdate;
	}
	public String getTrs_trstype() {
		return trs_trstype;
	}
	public void setTrs_trstype(String trs_trstype) {
		this.trs_trstype = trs_trstype;
	}
	public String getTrs_codopr() {
		return trs_codopr;
	}
	public void setTrs_codopr(String trs_codopr) {
		this.trs_codopr = trs_codopr;
	}
	public String getTrs_certif() {
		return trs_certif;
	}
	public void setTrs_certif(String trs_certif) {
		this.trs_certif = trs_certif;
	}
	public String getTrs_pursefile() {
		return trs_pursefile;
	}
	public void setTrs_pursefile(String trs_pursefile) {
		this.trs_pursefile = trs_pursefile;
	}
	public String getTrs_debtransc() {
		return trs_debtransc;
	}
	public void setTrs_debtransc(String trs_debtransc) {
		this.trs_debtransc = trs_debtransc;
	}
	public String getTrs_credtransc() {
		return trs_credtransc;
	}
	public void setTrs_credtransc(String trs_credtransc) {
		this.trs_credtransc = trs_credtransc;
	}
	public String getTrs_pursecod() {
		return trs_pursecod;
	}
	public void setTrs_pursecod(String trs_pursecod) {
		this.trs_pursecod = trs_pursecod;
	}
	public String getTrs_numremise() {
		return trs_numremise;
	}
	public void setTrs_numremise(String trs_numremise) {
		this.trs_numremise = trs_numremise;
	}
	public Date getTrs_datfin() {
		return trs_datfin;
	}
	public void setTrs_datfin(Date trs_datfin) {
		this.trs_datfin = trs_datfin;
	}
	public String getTrs_ordrecarte() {
		return trs_ordrecarte;
	}
	public void setTrs_ordrecarte(String trs_ordrecarte) {
		this.trs_ordrecarte = trs_ordrecarte;
	}
	public long getTrs_id() {
		return trs_id;
	}
	public void setTrs_id(long trs_id) {
		this.trs_id = trs_id;
	}
	public String getTrs_commande() {
		return trs_commande;
	}
	public void setTrs_commande(String trs_commande) {
		this.trs_commande = trs_commande;
	}
	
	@Override
	public String toString() {
		return "Transaction [trs_numtrait=" + trs_numtrait + ", trsnumcmr=" + trsnumcmr + ", trs_numtlcolcte="
				+ trs_numtlcolcte + ", trs_codtpe=" + trs_codtpe + ", trs_numfact=" + trs_numfact + ", trs_numbloc="
				+ trs_numbloc + ", trs_numseq=" + trs_numseq + ", trs_codporteur=" + trs_codporteur + ", trs_nocompte="
				+ trs_nocompte + ", trs_typtop=" + trs_typtop + ", trs_procod=" + trs_procod + ", trs_montant="
				+ trs_montant + ", trs_ecart=" + trs_ecart + ", trs_typcarte=" + trs_typcarte + ", trs_syspay="
				+ trs_syspay + ", trs_modval=" + trs_modval + ", trs_mapreleas=" + trs_mapreleas + ", trs_originpor="
				+ trs_originpor + ", trs_codbqe=" + trs_codbqe + ", trs_groupe=" + trs_groupe + ", trs_dattrans="
				+ trs_dattrans + ", trsnumaut=" + trsnumaut + ", trs_numreleveport=" + trs_numreleveport
				+ ", trs_numrelevecmr=" + trs_numrelevecmr + ", trs_numedition=" + trs_numedition + ", trs_annule="
				+ trs_annule + ", trs_etat=" + trs_etat + ", trs_arn=" + trs_arn + ", trs_devise=" + trs_devise
				+ ", trs_ica=" + trs_ica + ", trs_bqcmr=" + trs_bqcmr + ", trs_origine=" + trs_origine
				+ ", trs_chgreas=" + trs_chgreas + ", trs_poscapbi=" + trs_poscapbi + ", trs_cdhlidmt=" + trs_cdhlidmt
				+ ", trs_posentry=" + trs_posentry + ", trs_mailin=" + trs_mailin + ", trs_heuretrs=" + trs_heuretrs
				+ ", trs_mntsrc=" + trs_mntsrc + ", trs_devsrc=" + trs_devsrc + ", trs_cptcmr=" + trs_cptcmr
				+ ", trs_impdate=" + trs_impdate + ", trs_trtdate=" + trs_trtdate + ", trs_expdate=" + trs_expdate
				+ ", trs_trstype=" + trs_trstype + ", trs_codopr=" + trs_codopr + ", trs_certif=" + trs_certif
				+ ", trs_pursefile=" + trs_pursefile + ", trs_debtransc=" + trs_debtransc + ", trs_credtransc="
				+ trs_credtransc + ", trs_pursecod=" + trs_pursecod + ", trs_numremise=" + trs_numremise
				+ ", trs_datfin=" + trs_datfin + ", trs_ordrecarte=" + trs_ordrecarte + ", trs_id=" + trs_id
				+ ", trs_commande=" + trs_commande + "]";
	}
	
	
	
	

}
