package ma.m2m.gateway.dto;

import java.io.Serializable;
import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
