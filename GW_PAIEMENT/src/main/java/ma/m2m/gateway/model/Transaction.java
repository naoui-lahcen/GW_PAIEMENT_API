package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Transaction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5885509636447853241L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TRS_ID")	
	private long trsId;
	
	@Column(name = "TRS_NUMTRAIT")
	private Double trsNumtrait;
	@Column(name = "TRS_NUMCMR")
	private String trsNumcmr;
	@Column(name = "TRS_NUMTLCOLCTE")
	private Double trsNumtlcolcte;
	@Column(name = "TRS_CODTPE")
	private Double  trsCodtpe;
	@Column(name = "TRS_NUMFACT")
	private Double  trsNumfact;
	@Column(name = "TRS_NUMBLOC")
	private Double  trsNumbloc;
	@Column(name = "TRS_NUMSEQ")
	private Double  trsNumseq;
	@Column(name = "TRS_CODPORTEUR")
    private String 	trsCodporteur;
	@Column(name = "TRS_NOCOMPTE")
	private String trsNocompte;
	@Column(name = "TRS_TYPTOP")
	private String  trsTyptop;
	@Column(name = "TRS_PROCOD")
	private String trsProcod;
	@Column(name = "TRS_MONTANT")
	private Double  trsMontant;
	@Column(name = "TRS_ECART")	
	private Double  trsEcart;
	@Column(name = "TRS_TYPCARTE")	
	private String  trsTypcarte;
	@Column(name = "TRS_SYSPAY")	
	private String  trsSyspay;
	@Column(name = "TRS_MODVAL")	
	private String   trsModval;
	@Column(name = "TRS_MAPRELEAS")	
	private String trsMapreleas;
	@Column(name = "TRS_ORIGINPOR")	
	private String  trsOriginpor;
	@Column(name = "TRS_CODBQE")	
	private String  trsCodbqe;
	@Column(name = "TRS_GROUPE")	
	private String  trsGroupe;
	@Column(name = "TRS_DATTRANS")	
	private Date   trsDattrans;
	@Column(name = "TRS_NUMAUT")	
	private String  trsNumaut;
	@Column(name = "TRS_NUMRELEVEPORT")	
	private Double   trsNumreleveport;
	@Column(name = "TRS_NUMRELEVECMR")	
	private Double trsNumrelevecmr;
	@Column(name = "TRS_NUMEDITION")	
	private Double   trsNumedition;
	@Column(name = "TRS_ANNULE")	
	private String  trsAnnule;
	@Column(name = "TRS_ETAT")	
	private String  trsEtat;
	@Column(name = "TRS_ARN")	
	private String  trsArn;
	@Column(name = "TRS_DEVISE")	
	private String  trsDevise;
	@Column(name = "TRS_ICA")	
	private String  trsIca;
	@Column(name = "TRS_BQCMR")	
	private String  trsBqcmr;
	@Column(name = "TRS_ORIGINE")	
	private String  trsOrigine;
	@Column(name = "TRS_CHGREAS")	
	private String  trsChgreas;
	@Column(name = "TRS_POSCAPBI")	
	private String  trsPoscapbi;
	@Column(name = "TRS_CDHLIDMT")	
	private String  trsCdhlidmt;
	@Column(name = "TRS_POSENTRY")	
	private String  trsPosentry;
	@Column(name = "TRS_MAILIN")	
	private String  trsMailin;
	@Column(name = "TRS_HEURETRS")	
	private String  trsHeuretrs;
	@Column(name = "TRS_MNTSRC")	
	private Double   trsMntsrc;
	@Column(name = "TRS_DEVSRC")	
	private String  trsDevsrc;
	@Column(name = "TRS_CPTCMR")	
	private String  trsCptcmr;
	@Column(name = "TRS_IMPDATE")	
	private Date    trsImpdate;
	@Column(name = "TRS_TRTDATE")	
	private Date     trsTrtdate;
	@Column(name = "TRS_EXPDATE")	
	private Date    trsExpdate;
	@Column(name = "TRS_TRSTYPE")	
	private String  trsTrstype;
	@Column(name = "TRS_CODOPR")	
	private String  trsCodopr;
	@Column(name = "TRS_CERTIF")	
	private String  trsCertif;
	@Column(name = "TRS_PURSEFILE")	
	private String  trsPursefile;
	@Column(name = "TRS_DEBTRANSC")	
	private String  trsDebtransc;
	@Column(name = "TRS_CREDTRANSC")	
	private String  trsCredtransc;
	@Column(name = "TRS_PURSECOD")	
	private String  trsPursecod;
	@Column(name = "TRS_NUMREMISE")	
	private String  trsNumremise;
	@Column(name = "TRS_DATFIN")	
	private Date    trsDatfin;
	@Column(name = "TRS_ORDRECARTE")	
	private String  trsOrdrecarte;

	@Column(name = "TRS_COMMANDE")	
	private String   trsCommande;

}
