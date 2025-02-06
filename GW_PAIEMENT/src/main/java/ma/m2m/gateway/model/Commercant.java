package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="COMMERCANT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Commercant  implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="CMR_CODE")
	private String cmrCode;
	
	@Column(name="CMR_DATCRT")
	private Date cmrDatcrt;
	
	@Column(name="CMR_NOM")
	private String cmrNom;
	
	@Column(name="CMR_ABRVNOM")
	private String cmrAbrvnom;
	
	@Column(name="CMR_ADRS1")
	private String cmrAdrs1;
	
	@Column(name="CMR_ABRVADRS")
	private String cmrAbrvadrs;
	
	@Column(name="CMR_ADRS2")
	private String cmrAdrs2;
	
	@Column(name="CMR_CODPOSTAL")
	private String cmrCodpostal;
	
	@Column(name="CMR_TEL")
	private String cmrTel;
	
	@Column(name="CMR_FAX")
	private String cmrFax;
	
	@Column(name="CMR_CODACTIVITE")
	private String cmrCodactivite;
	
	@Column(name="CMR_CODPAYS")
	private String cmrCodpays;
	
	@Column(name="CMR_CODZONE")
	private String cmrCodzone;
	
	@Column(name="CMR_CODBQE")
	private String cmrCodbqe;
	
	@Column(name="CMR_CODAGENCE")
	private String cmrCodagence;
	
	@Column(name="CMR_NUMBADGE")
	private String cmrNumbadge;
	
	@Column(name="CMR_NUMCMR")
	private String cmrNumcmr;
	
	@Column(name="CMR_TYPCRT")
	private String cmrTypcrt;
	
	@Column(name="CMR_DATDPR")
	private Date cmrDatdpr;
	
	@Column(name="CMR_DATDEBUT")
	private Date cmrDatdebut;
	
	@Column(name="CMR_DATFIN")
	private Date cmrDatfin;
	
	@Column(name="CMR_TEST")
	private Character cmrTest;
	
	@Column(name="CMR_ETAT")
	private String cmrEtat;
	
	@Column(name="CMR_CMRIN")
	private Character cmrCmrin;
	
	@Column(name="CMR_UPSTAT")
	private Character cmrUpstat;
	
	@Column(name="CMR_PATENT")
	private String cmrPatent;
	
	@Column(name="CMR_CODVIL")
	private String cmrCodvil;
	
	@Column(name="CMR_SURVEL")
	private Character cmrSurvel;
	
	@Column(name="CMR_NOMCMR")
	private String cmrNomcmr;
	
	@Column(name="CMR_LIBCHEQ")
	private String cmrLibcheq;
	
	@Column(name="CMR_TYPE")
	private Character cmrType;
	
	@Column(name="CMR_CPTRELEVE")
	private Integer cmrCptreleve;
	
	@Column(name="CMR_GNRFRAIS")
	private Character cmrGnrfrais;
	
	@Column(name="CMR_ETATPRD")
	private String cmrEtatprd;
	
	@Column(name="CMR_ACTIF")
	private Character cmrActif;
	
	@Column(name="CMR_ADRSP1")
	private String cmrAdrsp1;
	
	@Column(name="CMR_ADRSP2")
	private String cmrAdrsp2;
	
	@Column(name="CMR_ADRSRLV")
	private Character cmrAdrsrlv;
	
	@Column(name="CMR_CODPOSTP")
	private String cmrCodpostp;
	
	@Column(name="CMR_LOCALITE")
	private String cmrLocalite;
	
	@Column(name="CMR_PAYS")
	private String cmrPays;
	
	private transient String email;

}
