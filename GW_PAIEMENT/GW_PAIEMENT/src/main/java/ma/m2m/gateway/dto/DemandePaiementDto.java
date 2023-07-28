package ma.m2m.gateway.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.m2m.gateway.Utils.Util;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemandePaiementDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int iddemande;

	private String nom;
	private String prenom;
	private String commande;
	private String email;
	private Double montant;
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
	
	public static String htmlCreq;
	
	public int getIddemande() {
		return iddemande;
	}
	public void setIddemande(int iddemande) {
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
	
	public static String getHtmlCreq() {
		return htmlCreq;
	}
	public static void setHtmlCreq(String htmlCreq) {
		DemandePaiementDto.htmlCreq = htmlCreq;
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
	// Util.displayCard(dem_pan) display carte pcidss
	@Override
	public String toString() {
		return "DemandePaiementDto [iddemande=" + iddemande + ", nom=" + nom + ", prenom=" + prenom + ", commande="
				+ commande + ", email=" + email + ", montant=" + montant + ", tel=" + tel + ", comid=" + comid
				+ ", galid=" + galid + ", etat_demande=" + etat_demande + ", dem_cvv=" + dem_cvv + ", dem_xid="
				+ demxid + ", dem_pan=" + Util.displayCard(dem_pan) + ", refdemande=" + refdemande + ", dem_date_time="
				+ dem_date_time + ", type_carte=" + type_carte + ", callbackURL=" + callbackURL + ", estimation="
				+ estimation + ", recallRep=" + recallRep + ", type_annulation=" + type_annulation
				+ ", etat_chargement=" + etat_chargement + ", etat_timeout=" + etat_timeout + ", id_client=" + id_client
				+ ", token=" + token + ", is_cof=" + is_cof + ", is_tokenized=" + is_tokenized + ", is_cvv_verified="
				+ is_cvv_verified + ", is_whitelist=" + is_whitelist + ", is_3ds=" + is_3ds + ", is_national="
				+ is_national + ", is_addcard=" + is_addcard + ", is_withsave=" + is_withsave + ", is_bpay=" + is_bpay
				+ ", is_bpaytoken=" + is_bpaytoken + ", is_bpaysave=" + is_bpaysave + ", dateSendMPI=" + dateSendMPI
				+ ", dateSendSWT=" + dateSendSWT + ", dateRetourSWT=" + dateRetourSWT + ", dateSendSWTAN="
				+ dateSendSWTAN + ", dateRetourSWTAN=" + dateRetourSWTAN + ", dateSendRecall=" + dateSendRecall
				+ ", dateRetourRecall=" + dateRetourRecall + ", nbreTenta=" + nbreTenta + ", tokencommande="
				+ tokencommande + ", etat_annulation=" + etat_annulation + "]";
	}
	
}
