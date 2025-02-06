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
public class TelecollecteDto implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	private String tlcNumcmr;
	private String tlcNumtpe;
	
	private Long tlcNumtlcolcte;
	private Double tlcNumremise;
	private Double tlcNumfich;
	private String tlcAkwnbr;
	private String tlcMesimp;
	private Date tlcDatremise;
	private String tlcHeuremise;
	private Date tlcDatcrtfich;
	private Double tlcNbrtrans;
	private String tlcGest;
	private String tlcTypentre;
	private String tlcEscompte;
	private String tlcCodbq;
	private String tlcFileid;

}
