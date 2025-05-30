package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name = "TELECOLLECTE")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Telecollecte implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 248680037522737855L;


	@EmbeddedId
	private TelecollecteId id;

	@Column(name = "TLC_NUMTPE")
	private String tlcNumtpe;
	
	@Column(name = "TLC_NUMREMISE")
	private Double tlcNumremise;
	@Column(name = "TLC_NUMFICH")
	private Double tlcNumfich;
	@Column(name = "TLC_AKWNBR")
	private String tlcAkwnbr;
	@Column(name = "TLC_MESIMP")
	private String tlcMesimp;
	@Column(name = "TLC_DATREMISE")
	private Date tlcDatremise;
	@Column(name = "TLC_HEUREMISE")
	private String tlcHeuremise;
	@Column(name = "TLC_DATCRTFICH")
	private Date tlcDatcrtfich;
	@Column(name = "TLC_NBRTRANS")
	private Double tlcNbrtrans;
	@Column(name = "TLC_GEST")
	private String tlcGest;
	@Column(name = "TLC_TYPENTRE")
	private String tlcTypentre;
	@Column(name = "TLC_ESCOMPTE")
	private String tlcEscompte;
	@Column(name = "TLC_CODBQ")
	private String tlcCodbq;
	@Column(name = "TLC_FILEID")
	private String tlcFileid;
	
}
