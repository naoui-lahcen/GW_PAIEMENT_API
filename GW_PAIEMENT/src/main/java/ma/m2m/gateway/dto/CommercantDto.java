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
public class CommercantDto implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String cmrCode;
	
	private Date cmrDatcrt;
	
	private String cmrNom;
	
	private String cmrAbrvnom;
	
	private String cmrAdrs1;
	
	private String cmrAbrvadrs;
	
	private String cmrAdrs2;
	
	private String cmrCodpostal;
	
	private String cmrTel;
	
	private String cmrFax;
	
	private String cmrCodactivite;
	
	private String cmrCodpays;
	
	private String cmrCodzone;
	
	private String cmrCodbqe;
	
	private String cmrCodagence;
	
	private String cmrNumbadge;
	
	private String cmrNumcmr;
	
	private String cmrTypcrt;
	
	private Date cmrDatdpr;
	
	private Date cmrDatdebut;
	
	private Date cmrDatfin;
	
	private Character cmrTest;
	
	private String cmrEtat;
	
	private Character cmrCmrin;
	
	private Character cmrUpstat;
	
	private String cmrPatent;
	
	private String cmrCodvil;
	
	private Character cmrSurvel;
	
	private String cmrNomcmr;
	
	private String cmrLibcheq;
	
	private Character cmrType;
	
	private Integer cmrCptreleve;
	
	private Character cmrGnrfrais;
	
	private String cmrEtatprd;
	
	private Character cmrActif;
	
	private String cmrAdrsp1;
	
	private String cmrAdrsp2;
	
	private Character cmrAdrsrlv;

	private String cmrCodpostp;
	
	private String cmrLocalite;
	
	private String cmrPays;

}
