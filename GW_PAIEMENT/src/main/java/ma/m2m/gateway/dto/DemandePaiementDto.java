package ma.m2m.gateway.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
	private String etatDemande;
	private String demCvv;
	private String demxid;
	private String demPan;
	private String refdemande;
	private Double frais;
	private String demDateTime;
	private String cartenaps;
	private String dateexpnaps;
	private String typeCarte;
	private String callbackURL;
	private String estimation;
	private String recallRep;
	private String typeAnnulation;
	private String etatAhargement;
	private String etatTimeout;
	private String idClient;
	private String token;
	private String isCof;
	private String isTokenized;
	private String isCvvVerified;
	private String isWhitelist;
	private String is3ds;
	private String isNational;
	private String isAddcard;
	private String isWithsave;
	private String isBpay;
	private String isBpaytoken;
	private String isBpaysave;
	private String dateSendMPI;
	private String dateSendSWT;
	private String dateRetourSWT;
	private String dateSendSWTAN;
	private String dateRetourSWTAN;
	private String dateSendRecall;
	private String dateRetourRecall;
	private int nbreTenta;
	private String tokencommande;
	private boolean etatAnnulation;
	private String expery;
	private String annee;
	private String mois;
	private CommercantDto commercantDto;
	private GalerieDto galerieDto;
	private String msgRefus;
	private String transactiontype;
	private boolean condition;
	private String nameCmr;
	private String siteWeb;
	private String formattedDate;
	
	private String creq;
	private List<Integer> years;
	private List<MonthDto> months;
	private List<Cartes> cartes;
	private Cartes carte;
	private String infoCarte;
	private List<FactureLDDto> factures;
	private List<ArticleDGIDto> articles;
	private boolean flagNvCarte;
	private boolean flagSaveCarte;

}
