package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataDGIDto {

	private Integer idDataDGI;
	
	private String storeId; //code commerçant
	
	private String dateTrx;
	
	private String commande;
	
	private Double montant;
	
	private String checksum;
	
	private String xml;
	
	private String etat;
	
	private int iddemande;

}
