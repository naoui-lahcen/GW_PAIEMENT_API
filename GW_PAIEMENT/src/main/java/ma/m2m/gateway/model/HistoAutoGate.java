package ma.m2m.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name = "HISTOAUTO_GATE")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HistoAutoGate implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Id
    @Column(name="HAT_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	@Column(name = "HAT_NUMDEM")
	private Long hatNumdem;
	@Column(name = "HAT_NUMCMR")
	private String hatNumcmr;
	@Column(name = "HAT_DATDEM")
	private Date hatDatdem;
	@Column(name = "HAT_HERDEM")
	private String hatHerdem;
	@Column(name = "HAT_CODTPE")
	private String hatCodtpe;
	@Column(name = "HAT_PORTEUR")
	private String hatPorteur;
	@Column(name = "HAT_MONTANT")
	private Double hatMontant;
	@Column(name = "HAT_MT_ANNUL")
	private Double hatMontantAnnul;
	@Column(name = "HAT_MT_REFUND")
	private Double hatMontantRefund;
	@Column(name = "HAT_MT_CAPTURE")
	private Double hatMontantCapture;
	@Column(name = "HAT_DEVISE")
	private String hatDevise;
	@Column(name = "HAT_CODEREP")
	private String hatCoderep;
	@Column(name = "HAT_NAUTSRV")
	private String hatNautsrv;
	@Column(name = "HAT_NCOMPTE")
	private String hatNcompte;
	@Column(name = "HAT_NAUTEMT")
	private String hatNautemt;
	@Column(name = "HAT_CODTRNS")
	private Character hatCodtrns;
	@Column(name = "HAT_NREFCE")
	private String hatNrefce;
	@Column(name = "HAT_MTFREF1")
	private String hatMtfref1;
	@Column(name = "HAT_MTFREF2")
	private String hatMtfref2;
	@Column(name = "HAT_PROCODE")
	private Character hatProcode;
	@Column(name = "HAT_REASREQ")
	private Character hatReasreq;
	@Column(name = "HAT_ANOMALIE")
	private Character hatAnomalie;
	@Column(name = "HAT_INSTANCE")
	private Character hatInstance;
	@Column(name = "HAT_APPELBQE")
	private Character hatAppelbqe;
	@Column(name = "HAT_CODPAY")
	private String hatCodpay;
	@Column(name = "HAT_DEMANDEUR")
	private String hatDemandeur;
	@Column(name = "HAT_REPONDEUR")
	private String hatRepondeur;
	@Column(name = "HAT_NOMDEANDEUR")
	private String hatNomdeandeur;
	@Column(name = "HAT_TYPPROTO")
	private Character hatTypproto;
	@Column(name = "HAT_ETAT")
	private Character hatEtat;
	@Column(name = "HAT_GESTIS")
	private Character hatGestis;
	@Column(name = "HAT_GESTAC")
	private Character hatGestac;
	@Column(name = "HAT_ARN")
	private String hatArn;
	@Column(name = "HAT_BQCMR")
	private String hatBqcmr;
	@Column(name = "HAT_BQPRP")
	private String hatBqprp;
	@Column(name = "HAT_RRN")
	private String hatRrn;
	@Column(name = "HAT_EXPDATE")
	private String hatExpdate;
	@Column(name = "HAT_TYPMSG")
	private String hatTypmsg;
	@Column(name = "HAT_MCC")
	private String hatMcc;
	@Column(name = "HAT_COMMANDE")
	private String hatNumCommande;
	@Column(name = "HAT_OPERATEUR_ANNULTLC")
	private String operateurAnnultlc;
	@Column(name = "HAT_OPERATEUR_TLC")
	private String operateurtlc;
	@Column(name = "HAT_DATE_TELEC")
	private Date hatdatetlc;
	@Column(name = "HAT_DATE_ANNULTELEC")
	private Date dateAnnulationTelecollecte;
	@Column(name = "HAT_COF")
	private String isCof;
	@Column(name = "HAT_TOKENIZED")
	private String isTokenized;
	@Column(name = "HAT_CVV_VERIFIED")
	private String isCvvVerified;
	@Column(name = "HAT_WHITELIST")
	private String isWhitelist;
	@Column(name = "HAT_3DS")
	private String is3ds;
	@Column(name = "HAT_NATIONAL")
	private String isNational;
	@Column(name = "HAT_ADDCARD")
	private String isAddcard;
	@Column(name = "HAT_WITHSAVE")
	private String isWithsave;

}
