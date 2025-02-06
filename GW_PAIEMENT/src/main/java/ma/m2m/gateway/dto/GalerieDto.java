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
public class GalerieDto implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long idGal;
	
	private String codeGal;
	
	private String nomGal;
	
	private String urlGal;
	
	private String codeCmr;
	
	private Date dateActivation;
	
	private String categorieGal;
	
	private String description;
	
	private String etat;

}
