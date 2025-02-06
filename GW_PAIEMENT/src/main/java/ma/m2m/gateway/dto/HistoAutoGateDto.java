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
public class HistoAutoGateDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private Integer id;
	private Long hatNumdem;
	private String hatNumcmr;
	private Date hatDatdem;
	private String hatHerdem;
	private String hatCodtpe;
	private String hatPorteur;
	private Double hatMontant;
	private Double hatMontantAnnul = 0.00;
	private Double hatMontantRefund = 0.00;
	private Double hatMontantCapture = 0.00;
	private String hatDevise;
	private String hatCoderep;
	private String hatNautsrv;
	private String hatNcompte;
	private String hatNautemt;
	private Character hatCodtrns;
	private String hatNrefce;
	private String hatMtfref1;
	private String hatMtfref2;
	private Character hatProcode;
	private Character hatReasreq;
	private Character hatAnomalie;
	private Character hatInstance;
	private Character hatAppelbqe;
	private String hatCodpay;
	private String hatDemandeur;
	private String hatRepondeur;
	private String hatNomdeandeur;
	private Character hatTypproto;
	private Character hatEtat;
	private Character hatGestis;
	private Character hatGestac;
	private String hatArn;
	private String hatBqcmr;
	private String hatBqprp;
	private String hatRrn;
	private String hatExpdate;
	private String hatTypmsg;
	private String hatMcc;
	private String hatNumCommande;
	private String operateurtlc;
	private Date hatdatetlc;
    private String annulationTelecollecteFlag;
	private String dateAnnulationTelecollecte;
	private String isCof;
	private String isTokenized;
	private String isCvvVerified;
	private String isWhitelist;
	private String is3ds;
	private String isNational;
	private String isAddcard;
	private String isWithsave;

}
