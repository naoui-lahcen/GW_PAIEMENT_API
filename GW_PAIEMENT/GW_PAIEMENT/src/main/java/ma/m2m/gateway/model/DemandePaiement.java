package ma.m2m.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

@Entity
@Table(name="DEMANDE_PAIEMENT")
//@Component
public class DemandePaiement implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 8571660241530586928L;

	@Id
	@Column(name = "id_demande")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer  iddemande;
    
	@Column(name="nom")
    private String nom;
    @Column(name="prenom")
    private String prenom;
    @Column(name="commande")
    private String commande;
    @Column(name="montant")
    private Double montant;
    @Column(name="email")
    private String email;
    @Column(name="langue")
    private String langue;
    @Column(name="successURL")
    private String successURL;
    @Column(name="failURL")
    private String failURL;
    @Column(name="timeoutURL")
    private String timeoutURL;
    @Column(name="tel")
    private String tel;
    @Column(name="address")
    private String address;
    @Column(name="city")
    private String city;
    @Column(name="state")
    private String state;
    @Column(name="country")
    private String country;
    @Column(name="postcode")
    private String postcode;
    @Column(name="comid")
    private String comid;
    @Column(name="galid")
    private String galid;
    @Column(name="etat_demande")
    private String etat_demande;
    @Column(name="dem_cvv")
    private String dem_cvv;
    @Column(name="dem_xid")
    private String demxid;
    @Column(name="dem_pan")
    private String dem_pan;
    @Column(name="refdemande")
    private String refdemande;
    @Column(name="frais")
    private Double frais;
    @Column(name="dem_date_time")
    private String dem_date_time;
    @Column(name="cartenaps")
    private String cartenaps;
    @Column(name="dateexpnaps")
    private String dateexpnaps;
    @Column(name="type_carte")
    private String type_carte;
    @Column(name="callbackURL")
    private String callbackURL;
    @Column(name="estimation")
    private String estimation;
    @Column(name="recallRep")
    private String recallRep;
    @Column(name="type_annulation")
    private String type_annulation;
    @Column(name="etat_chargement")
    private String etat_chargement;
    @Column(name="etat_timeout")
    private String etat_timeout;
    @Column(name="id_client")
    private String id_client;
    @Column(name="token")
    private String token;
    @Column(name="is_cof")
    private String is_cof;
    @Column(name="is_tokenized")
    private String is_tokenized;
    @Column(name="is_cvv_verified")
    private String is_cvv_verified;
    @Column(name="is_whitelist")
    private String is_whitelist;
    @Column(name="is_3ds")
    private String is_3ds;
    @Column(name="is_national")
    private String is_national;
    @Column(name="is_addcard")
    private String is_addcard;
    @Column(name="is_withsave")
    private String is_withsave;
    @Column(name="is_bpay")
    private String is_bpay;
    @Column(name="is_bpaytoken")
    private String is_bpaytoken;
    @Column(name="is_bpaysave")
    private String is_bpaysave;
    
    @Column(name="date_sendMPI")
    private String dateSendMPI;
    
    @Column(name="date_SendSWT")
    private String dateSendSWT ;
    
    @Column(name="date_RetourSWT")
    private String dateRetourSWT;
    
    @Column(name="date_SendSWTAN")
    private String dateSendSWTAN ;
    
    @Column(name="date_RetourSWTAN")
    private String dateRetourSWTAN; 
    
    @Column(name="date_SendRecall")
    private String dateSendRecall ;
    
    @Column(name="date_RetourRecall")
    private String dateRetourRecall;
    
    @Column(name="nbre_tenta")
    private int nbreTenta;
    
    @Column(name="tokencommande")
    private String tokencommande;
    
    @Column(name="etat_annulation")
    private boolean etat_annulation;
	
    @Column(name="creq")
    private String creq;
    
    public DemandePaiement() {
		super();
		// TODO Auto-generated constructor stub
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



	public Double getMontant() {
		return montant;
	}



	public void setMontant(Double montant) {
		this.montant = montant;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
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



	public void setDem_xid(String demxid) {
		this.demxid = demxid;
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



	public String getDemxid() {
		return demxid;
	}


	public void setDemxid(String demxid) {
		this.demxid = demxid;
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

	public String getCreq() {
		return creq;
	}


	public void setCreq(String creq) {
		this.creq = creq;
	}


	public boolean isEtat_annulation() {
		return etat_annulation;
	}


	public void setEtat_annulation(boolean etat_annulation) {
		this.etat_annulation = etat_annulation;
	}


	public DemandePaiement(Integer iddemande, String nom, String prenom, String commande, Double montant, String email,
			String langue, String successURL, String failURL, String timeoutURL, String tel, String address,
			String city, String state, String country, String postcode, String comid, String galid, String etat_demande,
			String dem_cvv, String demxid, String dem_pan, String refdemande, Double frais, String dem_date_time,
			String cartenaps, String dateexpnaps, String type_carte, String callbackURL, String estimation,
			String recallRep, String type_annulation, String etat_chargement, String etat_timeout, String id_client,
			String token, String is_cof, String is_tokenized, String is_cvv_verified, String is_whitelist,
			String is_3ds, String is_national, String is_addcard, String is_withsave, String is_bpay,
			String is_bpaytoken, String is_bpaysave, String dateSendMPI, String dateSendSWT, String dateRetourSWT,
			String dateSendSWTAN, String dateRetourSWTAN, String dateSendRecall, String dateRetourRecall, int nbreTenta,
			String tokencommande, String creq, boolean etat_annulation) {
		super();
		this.iddemande = iddemande;
		this.nom = nom;
		this.prenom = prenom;
		this.commande = commande;
		this.montant = montant;
		this.email = email;
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
		this.creq = creq;
		this.etat_annulation = etat_annulation;
	}


	@Override
	public String toString() {
		return "DemandePaiement [iddemande=" + iddemande + ", nom=" + nom + ", prenom=" + prenom + ", commande="
				+ commande + ", montant=" + montant + ", email=" + email + ", langue=" + langue + ", successURL="
				+ successURL + ", failURL=" + failURL + ", timeoutURL=" + timeoutURL + ", tel=" + tel + ", address="
				+ address + ", city=" + city + ", state=" + state + ", country=" + country + ", postcode=" + postcode
				+ ", comid=" + comid + ", galid=" + galid + ", etat_demande=" + etat_demande + ", dem_cvv=" + dem_cvv
				+ ", demxid=" + demxid + ", dem_pan=" + dem_pan + ", refdemande=" + refdemande + ", frais=" + frais
				+ ", dem_date_time=" + dem_date_time + ", cartenaps=" + cartenaps + ", dateexpnaps=" + dateexpnaps
				+ ", type_carte=" + type_carte + ", callbackURL=" + callbackURL + ", estimation=" + estimation
				+ ", recallRep=" + recallRep + ", type_annulation=" + type_annulation + ", etat_chargement="
				+ etat_chargement + ", etat_timeout=" + etat_timeout + ", id_client=" + id_client + ", token=" + token
				+ ", is_cof=" + is_cof + ", is_tokenized=" + is_tokenized + ", is_cvv_verified=" + is_cvv_verified
				+ ", is_whitelist=" + is_whitelist + ", is_3ds=" + is_3ds + ", is_national=" + is_national
				+ ", is_addcard=" + is_addcard + ", is_withsave=" + is_withsave + ", is_bpay=" + is_bpay
				+ ", is_bpaytoken=" + is_bpaytoken + ", is_bpaysave=" + is_bpaysave + ", dateSendMPI=" + dateSendMPI
				+ ", dateSendSWT=" + dateSendSWT + ", dateRetourSWT=" + dateRetourSWT + ", dateSendSWTAN="
				+ dateSendSWTAN + ", dateRetourSWTAN=" + dateRetourSWTAN + ", dateSendRecall=" + dateSendRecall
				+ ", dateRetourRecall=" + dateRetourRecall + ", nbreTenta=" + nbreTenta + ", tokencommande="
				+ tokencommande + ", creq=" + creq + ", etat_annulation=" + etat_annulation + "]";
	}


	
	
}
