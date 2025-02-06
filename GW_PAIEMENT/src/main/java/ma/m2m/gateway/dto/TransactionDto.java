package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionDto implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private long trsId;
	private String   trsCommande;
	private Double trsNumtrait;
	private String trsNumcmr;
	private Double trsNumtlcolcte;
	private Double  trsCodtpe;
	private Double  trsNumfact;
	private Double  trsNumbloc;
	private Double  trsNumseq;
    private String 	trsCodporteur;
	private String trsNocompte;
	private String  trsTyptop;
	private String trsProcod;
	private Double  trsMontant;
	private Double  trsEcart;
	private String  trsTypcarte;
	private String  trsSyspay;
	private String   trsModval;
	private String trsMapreleas;
	private String  trsOriginpor;
	private String  trsCodbqe;
	private String  trsGroupe;
	private Date   trsDattrans;
	private String  trsNumaut;
	private Double   trsNumreleveport;
	private Double trsNumrelevecmr;
	private Double   trsNumedition;
	private String  trsAnnule;
	private String  trsEtat;
	private String  trsArn;
	private String  trsDevise;
	private String  trsIca;
	private String  trsBqcmr;
	private String  trsOrigine;
	private String  trsChgreas;
	private String  trsPoscapbi;
	private String  trsCdhlidmt;
	private String  trsPosentry;
	private String  trsMailin;
	private String  trsHeuretrs;
	private Double   trsMntsrc;
	private String  trsDevsrc;
	private String  trsCptcmr;
	private Date    trsImpdate;
	private Date     trsTrtdate;
	private Date    trsExpdate;
	private String  trsTrstype;
	private String  trsCodopr;
	private String  trsCertif;
	private String  trsPursefile;
	private String  trsDebtransc;
	private String  trsCredtransc;
	private String  trsPursecod;
	private String  trsNumremise;
	private Date    trsDatfin;
	private String  trsOrdrecarte;

}
