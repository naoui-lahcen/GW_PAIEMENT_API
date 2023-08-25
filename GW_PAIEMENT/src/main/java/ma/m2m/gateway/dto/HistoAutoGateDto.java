package ma.m2m.gateway.dto;

import java.io.Serializable;
import java.util.Date;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class HistoAutoGateDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private long hatId;
	private Long hatNumdem;
	private String hatNumcmr;
	private Date hatDatdem;
	private String hatHerdem;
	private String hatCodtpe;
	private String hatPorteur;
	private Double hatMontant;
	private String hatDevise;
	private String hatCoderep;
	private String hatNautsrv;
	private String hatNcompte;
	private String hatNautemt;
	private Character hatCodtrns;
	private String hatNrefce;
	private String hatMtfref1;
	private String hatMtfref2;
	private Character hatProcode;
	private Character hatReasreq;
	private Character hatAnomalie;
	private Character hatInstance;
	private Character hatAppelbqe;
	private String hatCodpay;
	private String hatDemandeur;
	private String hatRepondeur;
	private String hatNomdeandeur;
	private Character hatTypproto;
	private Character hatEtat;
	private Character hatGestis;
	private Character hatGestac;
	private String hatArn;
	private String hatBqcmr;
	private String hatBqprp;
	private String hatRrn;
	private String hatExpdate;
	private String hatTypmsg;
	private String hatMcc;
	private String hatNumCommande;
	private String operateurtlc;
	private Date hatdatetlc;
    private String annulationTelecollecteFlag;
	private String dateAnnulationTelecollecte;
	private String is_cof;
	private String is_tokenized;
	private String is_cvv_verified;
	private String is_whitelist;
	private String is_3ds;
	private String is_national;
	private String is_addcard;
	private String is_withsave;
	
	
	
	
	public HistoAutoGateDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	public long getHatId() {
		return hatId;
	}

	public void setHatId(long hatId) {
		this.hatId = hatId;
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

	public Character getHatEtat() {
		return hatEtat;
	}

	public void setHatEtat(Character hatEtat) {
		this.hatEtat = hatEtat;
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

	public String getAnnulationTelecollecteFlag() {
		return annulationTelecollecteFlag;
	}

	public void setAnnulationTelecollecteFlag(String annulationTelecollecteFlag) {
		this.annulationTelecollecteFlag = annulationTelecollecteFlag;
	}

	public String getDateAnnulationTelecollecte() {
		return dateAnnulationTelecollecte;
	}

	public void setDateAnnulationTelecollecte(String dateAnnulationTelecollecte) {
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

	public Character getHatCodtrns() {
		return hatCodtrns;
	}

	public void setHatCodtrns(Character hatCodtrns) {
		this.hatCodtrns = hatCodtrns;
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

	public Character getHatTypproto() {
		return hatTypproto;
	}

	public void setHatTypproto(Character hatTypproto) {
		this.hatTypproto = hatTypproto;
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

	public Date getHatdatetlc() {
		return hatdatetlc;
	}

	public void setHatdatetlc(Date hatdatetlc) {
		this.hatdatetlc = hatdatetlc;
	}

	public String getOperateurtlc() {
		return operateurtlc;
	}

	public void setOperateurtlc(String operateurtlc) {
		this.operateurtlc = operateurtlc;
	}

	public HistoAutoGateDto(long hatId, Long hatNumdem, String hatNumcmr, Date hatDatdem, String hatHerdem,
			String hatCodtpe, String hatPorteur, Double hatMontant, String hatDevise, String hatCoderep,
			String hatNautsrv, String hatNcompte, String hatNautemt, String hatNrefce, String hatMtfref1,
			String hatMtfref2, Character hatProcode, String hatCodpay, String hatDemandeur, String hatRepondeur,
			String hatNomdeandeur, Character hatEtat, String hatArn, String hatBqcmr, String hatBqprp, String hatRrn,
			String hatExpdate, String hatTypmsg, String hatMcc, String hatNumCommande,
			String annulationTelecollecteFlag,Date hatdatetlc, String dateAnnulationTelecollecte, String is_cof, String is_tokenized,
			String is_cvv_verified, String is_whitelist, String is_3ds, String is_national, String is_addcard,
			String is_withsave) {
		super();
		this.hatId = hatId;
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
		this.hatNrefce = hatNrefce;
		this.hatMtfref1 = hatMtfref1;
		this.hatMtfref2 = hatMtfref2;
		this.hatProcode = hatProcode;
		this.hatCodpay = hatCodpay;
		this.hatDemandeur = hatDemandeur;
		this.hatRepondeur = hatRepondeur;
		this.hatNomdeandeur = hatNomdeandeur;
		this.hatEtat = hatEtat;
		this.hatArn = hatArn;
		this.hatBqcmr = hatBqcmr;
		this.hatBqprp = hatBqprp;
		this.hatRrn = hatRrn;
		this.hatExpdate = hatExpdate;
		this.hatTypmsg = hatTypmsg;
		this.hatMcc = hatMcc;
		this.hatNumCommande = hatNumCommande;
		this.hatdatetlc = hatdatetlc;
		this.annulationTelecollecteFlag = annulationTelecollecteFlag;
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

	public HistoAutoGateDto(long hatId, Long hatNumdem, String hatNumcmr, Date hatDatdem, String hatHerdem,
			String hatCodtpe, String hatPorteur, Double hatMontant, String hatDevise, String hatCoderep,
			String hatNautsrv, String hatNcompte, String hatNautemt, Character hatCodtrns, String hatNrefce,
			String hatMtfref1, String hatMtfref2, Character hatProcode, Character hatReasreq, Character hatAnomalie,
			Character hatInstance, Character hatAppelbqe, String hatCodpay, String hatDemandeur, String hatRepondeur,
			String hatNomdeandeur, Character hatTypproto, Character hatEtat, Character hatGestis, Character hatGestac,
			String hatArn, String hatBqcmr, String hatBqprp, String hatRrn, String hatExpdate, String hatTypmsg,
			String hatMcc, String hatNumCommande, String operateurtlc, Date hatdatetlc,
			String annulationTelecollecteFlag, String dateAnnulationTelecollecte, String is_cof, String is_tokenized,
			String is_cvv_verified, String is_whitelist, String is_3ds, String is_national, String is_addcard,
			String is_withsave) {
		super();
		this.hatId = hatId;
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
		this.operateurtlc = operateurtlc;
		this.hatdatetlc = hatdatetlc;
		this.annulationTelecollecteFlag = annulationTelecollecteFlag;
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
		return "HistoAutoGateDto [hatId=" + hatId + ", hatNumdem=" + hatNumdem + ", hatNumcmr=" + hatNumcmr
				+ ", hatDatdem=" + hatDatdem + ", hatHerdem=" + hatHerdem + ", hatCodtpe=" + hatCodtpe + ", hatPorteur="
				+ hatPorteur + ", hatMontant=" + hatMontant + ", hatDevise=" + hatDevise + ", hatCoderep=" + hatCoderep
				+ ", hatNautsrv=" + hatNautsrv + ", hatNcompte=" + hatNcompte + ", hatNautemt=" + hatNautemt
				+ ", hatCodtrns=" + hatCodtrns + ", hatNrefce=" + hatNrefce + ", hatMtfref1=" + hatMtfref1
				+ ", hatMtfref2=" + hatMtfref2 + ", hatProcode=" + hatProcode + ", hatReasreq=" + hatReasreq
				+ ", hatAnomalie=" + hatAnomalie + ", hatInstance=" + hatInstance + ", hatAppelbqe=" + hatAppelbqe
				+ ", hatCodpay=" + hatCodpay + ", hatDemandeur=" + hatDemandeur + ", hatRepondeur=" + hatRepondeur
				+ ", hatNomdeandeur=" + hatNomdeandeur + ", hatTypproto=" + hatTypproto + ", hatEtat=" + hatEtat
				+ ", hatGestis=" + hatGestis + ", hatGestac=" + hatGestac + ", hatArn=" + hatArn + ", hatBqcmr="
				+ hatBqcmr + ", hatBqprp=" + hatBqprp + ", hatRrn=" + hatRrn + ", hatExpdate=" + hatExpdate
				+ ", hatTypmsg=" + hatTypmsg + ", hatMcc=" + hatMcc + ", hatNumCommande=" + hatNumCommande
				+ ", operateurtlc=" + operateurtlc + ", hatdatetlc=" + hatdatetlc + ", annulationTelecollecteFlag="
				+ annulationTelecollecteFlag + ", dateAnnulationTelecollecte=" + dateAnnulationTelecollecte
				+ ", is_cof=" + is_cof + ", is_tokenized=" + is_tokenized + ", is_cvv_verified=" + is_cvv_verified
				+ ", is_whitelist=" + is_whitelist + ", is_3ds=" + is_3ds + ", is_national=" + is_national
				+ ", is_addcard=" + is_addcard + ", is_withsave=" + is_withsave + "]";
	}


}
