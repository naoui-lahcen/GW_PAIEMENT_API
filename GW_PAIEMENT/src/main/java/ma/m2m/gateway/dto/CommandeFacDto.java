package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommandeFacDto {
	
	private Integer  id ;
	
	private String codeClient;
	
	private String nomprenom;
	
	private String numCommande;
	
	private String email;
	
	private Double montantTotal;
	
	private Double montantTotalTva;
	
	private Double montantTotalTtc;
	
	private Double montantTotalTbr;
	
	private String successUrl;
	
	private String recallUrl;
	
	private String failurl;
	
	private String cmr;
	
	private String gal;
	
	private String date;
	
	private String checksum;
	
	private String xml;
	
	private String etat;

}
