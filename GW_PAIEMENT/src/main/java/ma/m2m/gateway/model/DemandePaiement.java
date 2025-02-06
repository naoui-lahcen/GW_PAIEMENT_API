package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
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
@Table(name="DEMANDE_PAIEMENT")
//@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
    private String etatDemande;
    @Column(name="dem_cvv")
    private String demCvv;
    @Column(name="dem_xid")
    private String demxid;
    @Column(name="dem_pan")
    private String demPan;
    @Column(name="refdemande")
    private String refdemande;
    @Column(name="frais")
    private Double frais;
    @Column(name="dem_date_time")
    private String demDateTime;
    @Column(name="cartenaps")
    private String cartenaps;
    @Column(name="dateexpnaps")
    private String dateexpnaps;
    @Column(name="type_carte")
    private String typeCarte;
    @Column(name="callbackURL")
    private String callbackURL;
    @Column(name="estimation")
    private String estimation;
    @Column(name="recallRep")
    private String recallRep;
    @Column(name="type_annulation")
    private String typeAnnulation;
    @Column(name="etat_chargement")
    private String etatChargement;
    @Column(name="etat_timeout")
    private String etatTimeout;
    @Column(name="id_client")
    private String idClient;
    @Column(name="token")
    private String token;
    @Column(name="is_cof")
    private String isCof;
    @Column(name="is_tokenized")
    private String isTokenized;
    @Column(name="is_cvv_verified")
    private String isCvvVerified;
    @Column(name="is_whitelist")
    private String isWhitelist;
    @Column(name="is_3ds")
    private String is3ds;
    @Column(name="is_national")
    private String isNational;
    @Column(name="is_addcard")
    private String isAddcard;
    @Column(name="is_withsave")
    private String isWithsave;
    @Column(name="is_bpay")
    private String isBpay;
    @Column(name="is_bpaytoken")
    private String isBpaytoken;
    @Column(name="is_bpaysave")
    private String isBpaysave;
    
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
    private boolean etatAnnulation;
	
    @Column(name="creq")
    private String creq;
    
    @Column(name="transactiontype")
    private String transactiontype;

}
