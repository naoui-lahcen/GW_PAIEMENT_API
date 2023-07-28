package ma.m2m.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

@Entity
@Table(name = "HISTOAUTO_GATE")
@Component
public class HistoAutoGate implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2972203304646222112L;


	@Id
    @Column(name="HAT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	@Column(name = "HAT_NUMDEM")
	private Long hatNumdem;
	@Column(name = "HAT_NUMCMR")
	private String hatNumcmr;
	@Column(name = "HAT_DATDEM")
	private Date hatDatdem;
	@Column(name = "HAT_HERDEM")
	private String hatHerdem;
	@Column(name = "HAT_CODTPE")
	private String hatCodtpe;
	@Column(name = "HAT_PORTEUR")
	private String hatPorteur;
	@Column(name = "HAT_MONTANT")
	private Double hatMontant;
	@Column(name = "HAT_DEVISE")
	private String hatDevise;
	@Column(name = "HAT_CODEREP")
	private String hatCoderep;
	@Column(name = "HAT_NAUTSRV")
	private String hatNautsrv;
	@Column(name = "HAT_NCOMPTE")
	private String hatNcompte;
	@Column(name = "HAT_NAUTEMT")
	private String hatNautemt;
	@Column(name = "HAT_CODTRNS")
	private Character hatCodtrns;
	@Column(name = "HAT_NREFCE")
	private String hatNrefce;
	@Column(name = "HAT_MTFREF1")
	private String hatMtfref1;
	@Column(name = "HAT_MTFREF2")
	private String hatMtfref2;
	@Column(name = "HAT_PROCODE")
	private Character hatProcode;
	@Column(name = "HAT_REASREQ")
	private Character hatReasreq;
	@Column(name = "HAT_ANOMALIE")
	private Character hatAnomalie;
	@Column(name = "HAT_INSTANCE")
	private Character hatInstance;
	@Column(name = "HAT_APPELBQE")
	private Character hatAppelbqe;
	@Column(name = "HAT_CODPAY")
	private String hatCodpay;
	@Column(name = "HAT_DEMANDEUR")
	private String hatDemandeur;
	@Column(name = "HAT_REPONDEUR")
	private String hatRepondeur;
	@Column(name = "HAT_NOMDEANDEUR")
	private String hatNomdeandeur;
	@Column(name = "HAT_TYPPROTO")
	private Character hatTypproto;
	@Column(name = "HAT_ETAT")
	private Character hatEtat;
	@Column(name = "HAT_GESTIS")
	private Character hatGestis;
	@Column(name = "HAT_GESTAC")
	private Character hatGestac;
	@Column(name = "HAT_ARN")
	private String hatArn;
	@Column(name = "HAT_BQCMR")
	private String hatBqcmr;
	@Column(name = "HAT_BQPRP")
	private String hatBqprp;
	@Column(name = "HAT_RRN")
	private String hatRrn;
	@Column(name = "HAT_EXPDATE")
	private String hatExpdate;
	@Column(name = "HAT_TYPMSG")
	private String hatTypmsg;
	@Column(name = "HAT_MCC")
	private String hatMcc;
	@Column(name = "HAT_COMMANDE")
	private String hatNumCommande;
	@Column(name = "HAT_OPERATEUR_ANNULTLC")
	private String operateurAnnultlc;
	@Column(name = "HAT_OPERATEUR_TLC")
	private String operateurtlc;
	@Column(name = "HAT_DATE_TELEC")
	private Date hatdatetlc;
	@Column(name = "HAT_DATE_ANNULTELEC")
	private Date dateAnnulationTelecollecte;
	@Column(name = "HAT_COF")
	private String is_cof;
	@Column(name = "HAT_TOKENIZED")
	private String is_tokenized;
	@Column(name = "HAT_CVV_VERIFIED")
	private String is_cvv_verified;
	@Column(name = "HAT_WHITELIST")
	private String is_whitelist;
	@Column(name = "HAT_3DS")
	private String is_3ds;
	@Column(name = "HAT_NATIONAL")
	private String is_national;
	@Column(name = "HAT_ADDCARD")
	private String is_addcard;
	@Column(name = "HAT_WITHSAVE")
	private String is_withsave;

	public HistoAutoGate() {
		super();
		// TODO Auto-generated constructor stub
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getHatNumdem() {
		return hatNumdem;
	}

	public void setHatNumdem(Long hatNumdem) {
		this.hatNumdem = hatNumdem;
	}

	public String getHatNumcmr() {
		return hatNumcmr;
	}

	public void setHatNumcmr(String hatNumcmr) {
		this.hatNumcmr = hatNumcmr;
	}

	public Date getHatDatdem() {
		return hatDatdem;
	}

	public void setHatDatdem(Date hatDatdem) {
		this.hatDatdem = hatDatdem;
	}

	public String getHatHerdem() {
		return hatHerdem;
	}

	public void setHatHerdem(String hatHerdem) {
		this.hatHerdem = hatHerdem;
	}

	public String getHatCodtpe() {
		return hatCodtpe;
	}

	public void setHatCodtpe(String hatCodtpe) {
		this.hatCodtpe = hatCodtpe;
	}

	public String getHatPorteur() {
		return hatPorteur;
	}

	public void setHatPorteur(String hatPorteur) {
		this.hatPorteur = hatPorteur;
	}

	public Double getHatMontant() {
		return hatMontant;
	}

	public void setHatMontant(Double hatMontant) {
		this.hatMontant = hatMontant;
	}

	public String getHatDevise() {
		return hatDevise;
	}

	public void setHatDevise(String hatDevise) {
		this.hatDevise = hatDevise;
	}

	public String getHatCoderep() {
		return hatCoderep;
	}

	public void setHatCoderep(String hatCoderep) {
		this.hatCoderep = hatCoderep;
	}

	public String getHatNautsrv() {
		return hatNautsrv;
	}

	public void setHatNautsrv(String hatNautsrv) {
		this.hatNautsrv = hatNautsrv;
	}

	public String getHatNcompte() {
		return hatNcompte;
	}

	public void setHatNcompte(String hatNcompte) {
		this.hatNcompte = hatNcompte;
	}

	public String getHatNautemt() {
		return hatNautemt;
	}

	public void setHatNautemt(String hatNautemt) {
		this.hatNautemt = hatNautemt;
	}

	public Character getHatCodtrns() {
		return hatCodtrns;
	}

	public void setHatCodtrns(Character hatCodtrns) {
		this.hatCodtrns = hatCodtrns;
	}

	public String getHatNrefce() {
		return hatNrefce;
	}

	public void setHatNrefce(String hatNrefce) {
		this.hatNrefce = hatNrefce;
	}

	public String getHatMtfref1() {
		return hatMtfref1;
	}

	public void setHatMtfref1(String hatMtfref1) {
		this.hatMtfref1 = hatMtfref1;
	}

	public String getHatMtfref2() {
		return hatMtfref2;
	}

	public void setHatMtfref2(String hatMtfref2) {
		this.hatMtfref2 = hatMtfref2;
	}

	public Character getHatProcode() {
		return hatProcode;
	}

	public void setHatProcode(Character hatProcode) {
		this.hatProcode = hatProcode;
	}

	public Character getHatReasreq() {
		return hatReasreq;
	}

	public void setHatReasreq(Character hatReasreq) {
		this.hatReasreq = hatReasreq;
	}

	public Character getHatAnomalie() {
		return hatAnomalie;
	}

	public void setHatAnomalie(Character hatAnomalie) {
		this.hatAnomalie = hatAnomalie;
	}

	public Character getHatInstance() {
		return hatInstance;
	}

	public void setHatInstance(Character hatInstance) {
		this.hatInstance = hatInstance;
	}

	public Character getHatAppelbqe() {
		return hatAppelbqe;
	}

	public void setHatAppelbqe(Character hatAppelbqe) {
		this.hatAppelbqe = hatAppelbqe;
	}

	public String getHatCodpay() {
		return hatCodpay;
	}

	public void setHatCodpay(String hatCodpay) {
		this.hatCodpay = hatCodpay;
	}

	public String getHatDemandeur() {
		return hatDemandeur;
	}

	public void setHatDemandeur(String hatDemandeur) {
		this.hatDemandeur = hatDemandeur;
	}

	public String getHatRepondeur() {
		return hatRepondeur;
	}

	public void setHatRepondeur(String hatRepondeur) {
		this.hatRepondeur = hatRepondeur;
	}

	public String getHatNomdeandeur() {
		return hatNomdeandeur;
	}

	public void setHatNomdeandeur(String hatNomdeandeur) {
		this.hatNomdeandeur = hatNomdeandeur;
	}

	public Character getHatTypproto() {
		return hatTypproto;
	}

	public void setHatTypproto(Character hatTypproto) {
		this.hatTypproto = hatTypproto;
	}

	public Character getHatEtat() {
		return hatEtat;
	}

	public void setHatEtat(Character hatEtat) {
		this.hatEtat = hatEtat;
	}

	public Character getHatGestis() {
		return hatGestis;
	}

	public void setHatGestis(Character hatGestis) {
		this.hatGestis = hatGestis;
	}

	public Character getHatGestac() {
		return hatGestac;
	}

	public void setHatGestac(Character hatGestac) {
		this.hatGestac = hatGestac;
	}

	public String getHatArn() {
		return hatArn;
	}

	public void setHatArn(String hatArn) {
		this.hatArn = hatArn;
	}

	public String getHatBqcmr() {
		return hatBqcmr;
	}

	public void setHatBqcmr(String hatBqcmr) {
		this.hatBqcmr = hatBqcmr;
	}

	public String getHatBqprp() {
		return hatBqprp;
	}

	public void setHatBqprp(String hatBqprp) {
		this.hatBqprp = hatBqprp;
	}

	public String getHatRrn() {
		return hatRrn;
	}

	public void setHatRrn(String hatRrn) {
		this.hatRrn = hatRrn;
	}

	public String getHatExpdate() {
		return hatExpdate;
	}

	public void setHatExpdate(String hatExpdate) {
		this.hatExpdate = hatExpdate;
	}

	public String getHatTypmsg() {
		return hatTypmsg;
	}

	public void setHatTypmsg(String hatTypmsg) {
		this.hatTypmsg = hatTypmsg;
	}

	public String getHatMcc() {
		return hatMcc;
	}

	public void setHatMcc(String hatMcc) {
		this.hatMcc = hatMcc;
	}

	public String getHatNumCommande() {
		return hatNumCommande;
	}

	public void setHatNumCommande(String hatNumCommande) {
		this.hatNumCommande = hatNumCommande;
	}

	public String getOperateurAnnultlc() {
		return operateurAnnultlc;
	}

	public void setOperateurAnnultlc(String operateurAnnultlc) {
		this.operateurAnnultlc = operateurAnnultlc;
	}

	public String getOperateurtlc() {
		return operateurtlc;
	}

	public void setOperateurtlc(String operateurtlc) {
		this.operateurtlc = operateurtlc;
	}

	public Date getHatdatetlc() {
		return hatdatetlc;
	}

	public void setHatdatetlc(Date hatdatetlc) {
		this.hatdatetlc = hatdatetlc;
	}

	public Date getDateAnnulationTelecollecte() {
		return dateAnnulationTelecollecte;
	}

	public void setDateAnnulationTelecollecte(Date dateAnnulationTelecollecte) {
		this.dateAnnulationTelecollecte = dateAnnulationTelecollecte;
	}

	public String getIs_cof() {
		return is_cof;
	}

	public void setIs_cof(String is_cof) {
		this.is_cof = is_cof;
	}

	public String getIs_tokenized() {
		return is_tokenized;
	}

	public void setIs_tokenized(String is_tokenized) {
		this.is_tokenized = is_tokenized;
	}

	public String getIs_cvv_verified() {
		return is_cvv_verified;
	}

	public void setIs_cvv_verified(String is_cvv_verified) {
		this.is_cvv_verified = is_cvv_verified;
	}

	public String getIs_whitelist() {
		return is_whitelist;
	}

	public void setIs_whitelist(String is_whitelist) {
		this.is_whitelist = is_whitelist;
	}

	public String getIs_3ds() {
		return is_3ds;
	}

	public void setIs_3ds(String is_3ds) {
		this.is_3ds = is_3ds;
	}

	public String getIs_national() {
		return is_national;
	}

	public void setIs_national(String is_national) {
		this.is_national = is_national;
	}

	public String getIs_addcard() {
		return is_addcard;
	}

	public void setIs_addcard(String is_addcard) {
		this.is_addcard = is_addcard;
	}

	public String getIs_withsave() {
		return is_withsave;
	}

	public void setIs_withsave(String is_withsave) {
		this.is_withsave = is_withsave;
	}

	public HistoAutoGate(long id, Long hatNumdem, String hatNumcmr, Date hatDatdem, String hatHerdem, String hatCodtpe,
			String hatPorteur, Double hatMontant, String hatDevise, String hatCoderep, String hatNautsrv,
			String hatNcompte, String hatNautemt, Character hatCodtrns, String hatNrefce, String hatMtfref1,
			String hatMtfref2, Character hatProcode, Character hatReasreq, Character hatAnomalie, Character hatInstance,
			Character hatAppelbqe, String hatCodpay, String hatDemandeur, String hatRepondeur, String hatNomdeandeur,
			Character hatTypproto, Character hatEtat, Character hatGestis, Character hatGestac, String hatArn,
			String hatBqcmr, String hatBqprp, String hatRrn, String hatExpdate, String hatTypmsg, String hatMcc,
			String hatNumCommande, String operateurAnnultlc, String operateurtlc, Date hatdatetlc,
			Date dateAnnulationTelecollecte, String is_cof, String is_tokenized, String is_cvv_verified,
			String is_whitelist, String is_3ds, String is_national, String is_addcard, String is_withsave) {
		super();
		this.id = id;
		this.hatNumdem = hatNumdem;
		this.hatNumcmr = hatNumcmr;
		this.hatDatdem = hatDatdem;
		this.hatHerdem = hatHerdem;
		this.hatCodtpe = hatCodtpe;
		this.hatPorteur = hatPorteur;
		this.hatMontant = hatMontant;
		this.hatDevise = hatDevise;
		this.hatCoderep = hatCoderep;
		this.hatNautsrv = hatNautsrv;
		this.hatNcompte = hatNcompte;
		this.hatNautemt = hatNautemt;
		this.hatCodtrns = hatCodtrns;
		this.hatNrefce = hatNrefce;
		this.hatMtfref1 = hatMtfref1;
		this.hatMtfref2 = hatMtfref2;
		this.hatProcode = hatProcode;
		this.hatReasreq = hatReasreq;
		this.hatAnomalie = hatAnomalie;
		this.hatInstance = hatInstance;
		this.hatAppelbqe = hatAppelbqe;
		this.hatCodpay = hatCodpay;
		this.hatDemandeur = hatDemandeur;
		this.hatRepondeur = hatRepondeur;
		this.hatNomdeandeur = hatNomdeandeur;
		this.hatTypproto = hatTypproto;
		this.hatEtat = hatEtat;
		this.hatGestis = hatGestis;
		this.hatGestac = hatGestac;
		this.hatArn = hatArn;
		this.hatBqcmr = hatBqcmr;
		this.hatBqprp = hatBqprp;
		this.hatRrn = hatRrn;
		this.hatExpdate = hatExpdate;
		this.hatTypmsg = hatTypmsg;
		this.hatMcc = hatMcc;
		this.hatNumCommande = hatNumCommande;
		this.operateurAnnultlc = operateurAnnultlc;
		this.operateurtlc = operateurtlc;
		this.hatdatetlc = hatdatetlc;
		this.dateAnnulationTelecollecte = dateAnnulationTelecollecte;
		this.is_cof = is_cof;
		this.is_tokenized = is_tokenized;
		this.is_cvv_verified = is_cvv_verified;
		this.is_whitelist = is_whitelist;
		this.is_3ds = is_3ds;
		this.is_national = is_national;
		this.is_addcard = is_addcard;
		this.is_withsave = is_withsave;
	}

	@Override
	public String toString() {
		return "HistoAutoGate [id=" + id + ", hatNumdem=" + hatNumdem + ", hatNumcmr=" + hatNumcmr + ", hatDatdem="
				+ hatDatdem + ", hatHerdem=" + hatHerdem + ", hatCodtpe=" + hatCodtpe + ", hatPorteur=" + hatPorteur
				+ ", hatMontant=" + hatMontant + ", hatDevise=" + hatDevise + ", hatCoderep=" + hatCoderep
				+ ", hatNautsrv=" + hatNautsrv + ", hatNcompte=" + hatNcompte + ", hatNautemt=" + hatNautemt
				+ ", hatCodtrns=" + hatCodtrns + ", hatNrefce=" + hatNrefce + ", hatMtfref1=" + hatMtfref1
				+ ", hatMtfref2=" + hatMtfref2 + ", hatProcode=" + hatProcode + ", hatReasreq=" + hatReasreq
				+ ", hatAnomalie=" + hatAnomalie + ", hatInstance=" + hatInstance + ", hatAppelbqe=" + hatAppelbqe
				+ ", hatCodpay=" + hatCodpay + ", hatDemandeur=" + hatDemandeur + ", hatRepondeur=" + hatRepondeur
				+ ", hatNomdeandeur=" + hatNomdeandeur + ", hatTypproto=" + hatTypproto + ", hatEtat=" + hatEtat
				+ ", hatGestis=" + hatGestis + ", hatGestac=" + hatGestac + ", hatArn=" + hatArn + ", hatBqcmr="
				+ hatBqcmr + ", hatBqprp=" + hatBqprp + ", hatRrn=" + hatRrn + ", hatExpdate=" + hatExpdate
				+ ", hatTypmsg=" + hatTypmsg + ", hatMcc=" + hatMcc + ", hatNumCommande=" + hatNumCommande
				+ ", operateurAnnultlc=" + operateurAnnultlc + ", operateurtlc=" + operateurtlc + ", hatdatetlc="
				+ hatdatetlc + ", dateAnnulationTelecollecte=" + dateAnnulationTelecollecte + ", is_cof=" + is_cof
				+ ", is_tokenized=" + is_tokenized + ", is_cvv_verified=" + is_cvv_verified + ", is_whitelist="
				+ is_whitelist + ", is_3ds=" + is_3ds + ", is_national=" + is_national + ", is_addcard=" + is_addcard
				+ ", is_withsave=" + is_withsave + "]";
	}

	
	

	
	
	
	
	
	

}
