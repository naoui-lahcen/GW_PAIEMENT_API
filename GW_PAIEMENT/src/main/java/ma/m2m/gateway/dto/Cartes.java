package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-23 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cartes implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String carte;
	
	private String pcidsscarte;
	
	private Integer year;
	
	private String mois;
	
	private String moisValue;

	private boolean isExpired;

	private String scheme;

}
