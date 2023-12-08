package ma.m2m.gateway.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.m2m.gateway.Utils.Util;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class DemandePaiementDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer iddemande;

	private String nom;
	private String prenom;
	private String commande;
	private String email;
	private Double montant;
	private String montantStr;
	private String langue;
	private String successURL;
	private String failURL;
	private String timeoutURL;
	private String tel;
	private String address;
	private String city;
	private String state;
	private String country;
	private String postcode;
	private String comid;
	private String galid;
	private String etat_demande;
	private String dem_cvv;
	private String demxid;
	private String dem_pan;
	private String refdemande;
	private Double frais;
	private String dem_date_time;
	private String cartenaps;
	private String dateexpnaps;
	private String type_carte;
	private String callbackURL;
	private String estimation;
	private String recallRep;
	private String type_annulation;
	private String etat_chargement;
	private String etat_timeout;
	private String id_client;
	private String token;
	private String is_cof;
	private String is_tokenized;
	private String is_cvv_verified;
	private String is_whitelist;
	private String is_3ds;
	private String is_national;
	private String is_addcard;
	private String is_withsave;
	private String is_bpay;
	private String is_bpaytoken;
	private String is_bpaysave;
	private String dateSendMPI;
	private String dateSendSWT;
	private String dateRetourSWT;
	private String dateSendSWTAN;
	private String dateRetourSWTAN;
	private String dateSendRecall;
	private String dateRetourRecall;
	private int nbreTenta;
	private String tokencommande;
	private boolean etat_annulation;
	private String expery;
	private String annee;
	private String mois;
	private CommercantDto commercantDto;
	private GalerieDto galerieDto;
	private String msgRefus;
	private String transactiontype;
	private boolean condition;
	
	private String creq;
	private List<Integer> years;
	private List<MonthDto> months;
	private List<Cartes> cartes;
	private Cartes carte;
	private String infoCarte;
	private List<FactureLDDto> factures;
	
	public Cartes getCarte() {
		return carte;
	}
	public void setCarte(Cartes carte) {
		this.carte = carte;
	}
	
	public String getInfoCarte() {
		return infoCarte;
	}
	public void setInfoCarte(String infoCarte) {
		this.infoCarte = infoCarte;
	}
	public Integer getIddemande() {
		return iddemande;
	}
	public void setIddemande(Integer iddemande) {
		this.iddemande = iddemande;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getCommande() {
		return commande;
	}
	public void setCommande(String commande) {
		this.commande = commande;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Double getMontant() {
		return montant;
	}
	public void setMontant(Double montant) {
		this.montant = montant;
	}
	public String getMontantStr() {
		return montantStr;
	}
	public void setMontantStr(String montantStr) {
		this.montantStr = montantStr;
	}
	public String getLangue() {
		return langue;
	}
	public void setLangue(String langue) {
		this.langue = langue;
	}
	public String getSuccessURL() {
		return successURL;
	}
	public void setSuccessURL(String successURL) {
		this.successURL = successURL;
	}
	public String getFailURL() {
		return failURL;
	}
	public void setFailURL(String failURL) {
		this.failURL = failURL;
	}
	public String getTimeoutURL() {
		return timeoutURL;
	}
	public void setTimeoutURL(String timeoutURL) {
		this.timeoutURL = timeoutURL;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getComid() {
		return comid;
	}
	public void setComid(String comid) {
		this.comid = comid;
	}
	public String getGalid() {
		return galid;
	}
	public void setGalid(String galid) {
		this.galid = galid;
	}
	public String getEtat_demande() {
		return etat_demande;
	}
	public void setEtat_demande(String etat_demande) {
		this.etat_demande = etat_demande;
	}
	public String getDem_cvv() {
		return dem_cvv;
	}
	public void setDem_cvv(String dem_cvv) {
		this.dem_cvv = dem_cvv;
	}
	public String getDem_xid() {
		return demxid;
	}
	public void setDem_xid(String dem_xid) {
		this.demxid = dem_xid;
	}
	public String getDem_pan() {
		return dem_pan;
	}
	public void setDem_pan(String dem_pan) {
		this.dem_pan = dem_pan;
	}
	public String getRefdemande() {
		return refdemande;
	}
	public void setRefdemande(String refdemande) {
		this.refdemande = refdemande;
	}
	public Double getFrais() {
		return frais;
	}
	public void setFrais(Double frais) {
		this.frais = frais;
	}
	public String getDem_date_time() {
		return dem_date_time;
	}
	public void setDem_date_time(String dem_date_time) {
		this.dem_date_time = dem_date_time;
	}
	public String getCartenaps() {
		return cartenaps;
	}
	public void setCartenaps(String cartenaps) {
		this.cartenaps = cartenaps;
	}
	public String getDateexpnaps() {
		return dateexpnaps;
	}
	public void setDateexpnaps(String dateexpnaps) {
		this.dateexpnaps = dateexpnaps;
	}
	public String getType_carte() {
		return type_carte;
	}
	public void setType_carte(String type_carte) {
		this.type_carte = type_carte;
	}
	public String getCallbackURL() {
		return callbackURL;
	}
	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}
	public String getEstimation() {
		return estimation;
	}
	public void setEstimation(String estimation) {
		this.estimation = estimation;
	}
	public String getRecallRep() {
		return recallRep;
	}
	public void setRecallRep(String recallRep) {
		this.recallRep = recallRep;
	}
	public String getType_annulation() {
		return type_annulation;
	}
	public void setType_annulation(String type_annulation) {
		this.type_annulation = type_annulation;
	}
	public String getEtat_chargement() {
		return etat_chargement;
	}
	public void setEtat_chargement(String etat_chargement) {
		this.etat_chargement = etat_chargement;
	}
	public String getEtat_timeout() {
		return etat_timeout;
	}
	public void setEtat_timeout(String etat_timeout) {
		this.etat_timeout = etat_timeout;
	}
	public String getId_client() {
		return id_client;
	}
	public void setId_client(String id_client) {
		this.id_client = id_client;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
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
	public String getIs_bpay() {
		return is_bpay;
	}
	public void setIs_bpay(String is_bpay) {
		this.is_bpay = is_bpay;
	}
	public String getIs_bpaytoken() {
		return is_bpaytoken;
	}
	public void setIs_bpaytoken(String is_bpaytoken) {
		this.is_bpaytoken = is_bpaytoken;
	}
	public String getIs_bpaysave() {
		return is_bpaysave;
	}
	public void setIs_bpaysave(String is_bpaysave) {
		this.is_bpaysave = is_bpaysave;
	}
	public String getDateSendMPI() {
		return dateSendMPI;
	}
	public void setDateSendMPI(String dateSendMPI) {
		this.dateSendMPI = dateSendMPI;
	}
	public String getDateSendSWT() {
		return dateSendSWT;
	}
	public void setDateSendSWT(String dateSendSWT) {
		this.dateSendSWT = dateSendSWT;
	}
	public String getDateRetourSWT() {
		return dateRetourSWT;
	}
	public void setDateRetourSWT(String dateRetourSWT) {
		this.dateRetourSWT = dateRetourSWT;
	}
	public String getDateSendSWTAN() {
		return dateSendSWTAN;
	}
	public void setDateSendSWTAN(String dateSendSWTAN) {
		this.dateSendSWTAN = dateSendSWTAN;
	}
	public String getDateRetourSWTAN() {
		return dateRetourSWTAN;
	}
	public void setDateRetourSWTAN(String dateRetourSWTAN) {
		this.dateRetourSWTAN = dateRetourSWTAN;
	}
	public String getDateSendRecall() {
		return dateSendRecall;
	}
	public void setDateSendRecall(String dateSendRecall) {
		this.dateSendRecall = dateSendRecall;
	}
	public String getDateRetourRecall() {
		return dateRetourRecall;
	}
	public void setDateRetourRecall(String dateRetourRecall) {
		this.dateRetourRecall = dateRetourRecall;
	}
	public int getNbreTenta() {
		return nbreTenta;
	}
	public void setNbreTenta(int nbreTenta) {
		this.nbreTenta = nbreTenta;
	}
	public String getTokencommande() {
		return tokencommande;
	}
	public void setTokencommande(String tokencommande) {
		this.tokencommande = tokencommande;
	}
	public boolean isEtat_annulation() {
		return etat_annulation;
	}
	public void setEtat_annulation(boolean etat_annulation) {
		this.etat_annulation = etat_annulation;
	}
	public String getExpery() {
		return expery;
	}
	public void setExpery(String expery) {
		this.expery = expery;
	}
	public String getAnnee() {
		return annee;
	}
	public void setAnnee(String annee) {
		this.annee = annee;
	}
	public String getMois() {
		return mois;
	}
	public void setMois(String mois) {
		this.mois = mois;
	}
	
	public String getCreq() {
		return creq;
	}
	public void setCreq(String creq) {
		this.creq = creq;
	}
	public String getDemxid() {
		return demxid;
	}
	public void setDemxid(String demxid) {
		this.demxid = demxid;
	}
	public CommercantDto getCommercantDto() {
		return commercantDto;
	}
	public void setCommercantDto(CommercantDto commercantDto) {
		this.commercantDto = commercantDto;
	}
	
	public GalerieDto getGalerieDto() {
		return galerieDto;
	}
	public void setGalerieDto(GalerieDto galerieDto) {
		this.galerieDto = galerieDto;
	}
	
	public String getMsgRefus() {
		return msgRefus;
	}
	public void setMsgRefus(String msgRefus) {
		this.msgRefus = msgRefus;
	}
	
	public String getTransactiontype() {
		return transactiontype;
	}
	public void setTransactiontype(String transactiontype) {
		this.transactiontype = transactiontype;
	}
	
	public List<Integer> getYears() {
		return years;
	}
	public void setYears(List<Integer> years) {
		this.years = years;
	}
	
	public List<MonthDto> getMonths() {
		return months;
	}
	public void setMonths(List<MonthDto> months) {
		this.months = months;
	}
	
	public boolean isCondition() {
		return condition;
	}
	public void setCondition(boolean condition) {
		this.condition = condition;
	}
	
	public List<Cartes> getCartes() {
		return cartes;
	}
	public void setCartes(List<Cartes> cartes) {
		this.cartes = cartes;
	}
	
	public List<FactureLDDto> getFactures() {
		return factures;
	}
	public void setFactures(List<FactureLDDto> factures) {
		this.factures = factures;
	}
	public DemandePaiementDto() {
		super();
	}
	
	public DemandePaiementDto(Integer iddemande, String nom, String prenom, String commande, String email,
			Double montant, String montantStr, String langue, String successURL, String failURL, String timeoutURL,
			String tel, String address, String city, String state, String country, String postcode, String comid,
			String galid, String etat_demande, String dem_cvv, String demxid, String dem_pan, String refdemande,
			Double frais, String dem_date_time, String cartenaps, String dateexpnaps, String type_carte,
			String callbackURL, String estimation, String recallRep, String type_annulation, String etat_chargement,
			String etat_timeout, String id_client, String token, String is_cof, String is_tokenized,
			String is_cvv_verified, String is_whitelist, String is_3ds, String is_national, String is_addcard,
			String is_withsave, String is_bpay, String is_bpaytoken, String is_bpaysave, String dateSendMPI,
			String dateSendSWT, String dateRetourSWT, String dateSendSWTAN, String dateRetourSWTAN,
			String dateSendRecall, String dateRetourRecall, int nbreTenta, String tokencommande,
			boolean etat_annulation, String expery, String annee, String mois, CommercantDto commercantDto,
			GalerieDto galerieDto, String msgRefus, String transactiontype, boolean condition, String creq,
			List<Integer> years, List<MonthDto> months, List<Cartes> cartes) {
		super();
		this.iddemande = iddemande;
		this.nom = nom;
		this.prenom = prenom;
		this.commande = commande;
		this.email = email;
		this.montant = montant;
		this.montantStr = montantStr;
		this.langue = langue;
		this.successURL = successURL;
		this.failURL = failURL;
		this.timeoutURL = timeoutURL;
		this.tel = tel;
		this.address = address;
		this.city = city;
		this.state = state;
		this.country = country;
		this.postcode = postcode;
		this.comid = comid;
		this.galid = galid;
		this.etat_demande = etat_demande;
		this.dem_cvv = dem_cvv;
		this.demxid = demxid;
		this.dem_pan = dem_pan;
		this.refdemande = refdemande;
		this.frais = frais;
		this.dem_date_time = dem_date_time;
		this.cartenaps = cartenaps;
		this.dateexpnaps = dateexpnaps;
		this.type_carte = type_carte;
		this.callbackURL = callbackURL;
		this.estimation = estimation;
		this.recallRep = recallRep;
		this.type_annulation = type_annulation;
		this.etat_chargement = etat_chargement;
		this.etat_timeout = etat_timeout;
		this.id_client = id_client;
		this.token = token;
		this.is_cof = is_cof;
		this.is_tokenized = is_tokenized;
		this.is_cvv_verified = is_cvv_verified;
		this.is_whitelist = is_whitelist;
		this.is_3ds = is_3ds;
		this.is_national = is_national;
		this.is_addcard = is_addcard;
		this.is_withsave = is_withsave;
		this.is_bpay = is_bpay;
		this.is_bpaytoken = is_bpaytoken;
		this.is_bpaysave = is_bpaysave;
		this.dateSendMPI = dateSendMPI;
		this.dateSendSWT = dateSendSWT;
		this.dateRetourSWT = dateRetourSWT;
		this.dateSendSWTAN = dateSendSWTAN;
		this.dateRetourSWTAN = dateRetourSWTAN;
		this.dateSendRecall = dateSendRecall;
		this.dateRetourRecall = dateRetourRecall;
		this.nbreTenta = nbreTenta;
		this.tokencommande = tokencommande;
		this.etat_annulation = etat_annulation;
		this.expery = expery;
		this.annee = annee;
		this.mois = mois;
		this.commercantDto = commercantDto;
		this.galerieDto = galerieDto;
		this.msgRefus = msgRefus;
		this.transactiontype = transactiontype;
		this.condition = condition;
		this.creq = creq;
		this.years = years;
		this.months = months;
		this.cartes = cartes;
	}
	
	public DemandePaiementDto(Integer iddemande, String nom, String prenom, String commande, String email,
			Double montant, String montantStr, String langue, String successURL, String failURL, String timeoutURL,
			String tel, String address, String city, String state, String country, String postcode, String comid,
			String galid, String etat_demande, String dem_cvv, String demxid, String dem_pan, String refdemande,
			Double frais, String dem_date_time, String cartenaps, String dateexpnaps, String type_carte,
			String callbackURL, String estimation, String recallRep, String type_annulation, String etat_chargement,
			String etat_timeout, String id_client, String token, String is_cof, String is_tokenized,
			String is_cvv_verified, String is_whitelist, String is_3ds, String is_national, String is_addcard,
			String is_withsave, String is_bpay, String is_bpaytoken, String is_bpaysave, String dateSendMPI,
			String dateSendSWT, String dateRetourSWT, String dateSendSWTAN, String dateRetourSWTAN,
			String dateSendRecall, String dateRetourRecall, int nbreTenta, String tokencommande,
			boolean etat_annulation, String expery, String annee, String mois, CommercantDto commercantDto,
			GalerieDto galerieDto, String msgRefus, String transactiontype, boolean condition, String creq,
			List<Integer> years, List<MonthDto> months, List<Cartes> cartes, Cartes carte) {
		super();
		this.iddemande = iddemande;
		this.nom = nom;
		this.prenom = prenom;
		this.commande = commande;
		this.email = email;
		this.montant = montant;
		this.montantStr = montantStr;
		this.langue = langue;
		this.successURL = successURL;
		this.failURL = failURL;
		this.timeoutURL = timeoutURL;
		this.tel = tel;
		this.address = address;
		this.city = city;
		this.state = state;
		this.country = country;
		this.postcode = postcode;
		this.comid = comid;
		this.galid = galid;
		this.etat_demande = etat_demande;
		this.dem_cvv = dem_cvv;
		this.demxid = demxid;
		this.dem_pan = dem_pan;
		this.refdemande = refdemande;
		this.frais = frais;
		this.dem_date_time = dem_date_time;
		this.cartenaps = cartenaps;
		this.dateexpnaps = dateexpnaps;
		this.type_carte = type_carte;
		this.callbackURL = callbackURL;
		this.estimation = estimation;
		this.recallRep = recallRep;
		this.type_annulation = type_annulation;
		this.etat_chargement = etat_chargement;
		this.etat_timeout = etat_timeout;
		this.id_client = id_client;
		this.token = token;
		this.is_cof = is_cof;
		this.is_tokenized = is_tokenized;
		this.is_cvv_verified = is_cvv_verified;
		this.is_whitelist = is_whitelist;
		this.is_3ds = is_3ds;
		this.is_national = is_national;
		this.is_addcard = is_addcard;
		this.is_withsave = is_withsave;
		this.is_bpay = is_bpay;
		this.is_bpaytoken = is_bpaytoken;
		this.is_bpaysave = is_bpaysave;
		this.dateSendMPI = dateSendMPI;
		this.dateSendSWT = dateSendSWT;
		this.dateRetourSWT = dateRetourSWT;
		this.dateSendSWTAN = dateSendSWTAN;
		this.dateRetourSWTAN = dateRetourSWTAN;
		this.dateSendRecall = dateSendRecall;
		this.dateRetourRecall = dateRetourRecall;
		this.nbreTenta = nbreTenta;
		this.tokencommande = tokencommande;
		this.etat_annulation = etat_annulation;
		this.expery = expery;
		this.annee = annee;
		this.mois = mois;
		this.commercantDto = commercantDto;
		this.galerieDto = galerieDto;
		this.msgRefus = msgRefus;
		this.transactiontype = transactiontype;
		this.condition = condition;
		this.creq = creq;
		this.years = years;
		this.months = months;
		this.cartes = cartes;
		this.carte = carte;
	}
	// Util.formatCard(dem_pan) display carte pcidss
	@Override
	public String toString() {
		return "DemandePaiementDto [iddemande=" + iddemande + ", nom=" + nom + ", prenom=" + prenom + ", commande="
				+ commande + ", email=" + email + ", montant=" + montant + ", montantStr=" + montantStr + ", langue="
				+ langue + ", successURL=" + successURL + ", failURL=" + failURL + ", timeoutURL=" + timeoutURL
				+ ", tel=" + tel + ", address=" + address + ", city=" + city + ", state=" + state + ", country="
				+ country + ", postcode=" + postcode + ", comid=" + comid + ", galid=" + galid + ", etat_demande="
				+ etat_demande + ", dem_cvv=" + dem_cvv + ", demxid=" + demxid + ", dem_pan=" + Util.formatCard(dem_pan)
				+ ", refdemande=" + refdemande + ", frais=" + frais + ", dem_date_time=" + dem_date_time
				+ ", cartenaps=" + cartenaps + ", dateexpnaps=" + dateexpnaps + ", type_carte=" + type_carte
				+ ", callbackURL=" + callbackURL + ", estimation=" + estimation + ", recallRep=" + recallRep
				+ ", type_annulation=" + type_annulation + ", etat_chargement=" + etat_chargement + ", etat_timeout="
				+ etat_timeout + ", id_client=" + id_client + ", token=" + token + ", is_cof=" + is_cof
				+ ", is_tokenized=" + is_tokenized + ", is_cvv_verified=" + is_cvv_verified + ", is_whitelist="
				+ is_whitelist + ", is_3ds=" + is_3ds + ", is_national=" + is_national + ", is_addcard=" + is_addcard
				+ ", is_withsave=" + is_withsave + ", is_bpay=" + is_bpay + ", is_bpaytoken=" + is_bpaytoken
				+ ", is_bpaysave=" + is_bpaysave + ", dateSendMPI=" + dateSendMPI + ", dateSendSWT=" + dateSendSWT
				+ ", dateRetourSWT=" + dateRetourSWT + ", dateSendSWTAN=" + dateSendSWTAN + ", dateRetourSWTAN="
				+ dateRetourSWTAN + ", dateSendRecall=" + dateSendRecall + ", dateRetourRecall=" + dateRetourRecall
				+ ", nbreTenta=" + nbreTenta + ", tokencommande=" + tokencommande + ", etat_annulation="
				+ etat_annulation + ", expery=" + expery + ", annee=" + annee + ", mois=" + mois + ", commercantDto="
				+ commercantDto + ", galerieDto=" + galerieDto + ", msgRefus=" + msgRefus + ", transactiontype="
				+ transactiontype + ", condition=" + condition + ", creq=" + creq + ", years=" + years + ", months="
				+ months + ", cartes=" + cartes + "]";
	}
	
}
