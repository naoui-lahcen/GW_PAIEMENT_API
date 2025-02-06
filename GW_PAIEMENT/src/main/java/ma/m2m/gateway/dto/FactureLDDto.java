package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FactureLDDto implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;
	
	private String numCommande;
	
	private String  numfacture;
	
	private String numcontrat;
	
	private String numligne;
	
	private String name;
	
	private String classF;
	
	private Double montantTbr;
	
	private Double montantTtc;
	
	private Double montantTotal;
	
	private String numPolice;
	
	private Double montantTva;
	
	private String produit;
	
	private String type;
	
	private String date;
	
	private String fourniture;
	
	private Double montantSTbr;
	
	private int iddemande;
	
	/** reconciliation ecom lydec*/
	private String etat;
	
	private String numrecnaps;
	
	private String datepai;
	
	private String trxFactureLydec;

}
