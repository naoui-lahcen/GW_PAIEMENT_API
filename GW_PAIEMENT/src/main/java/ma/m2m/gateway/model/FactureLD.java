package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="FactureLD")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FactureLD implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="numCommande")
	private String numCommande;
	
	@Column(name="numfacture")
	private String  numfacture;
	
	@Column(name="numcontrat")
	private String numcontrat;
	
	@Column(name="numligne")
	private String numligne;
	
	@Column(name="name")
	private String name;
	
	@Column(name="classF")
	private String classF;
	
	@Column(name="montantTbr")
	private Double montantTbr;
	
	@Column(name="montantTtc")
	private Double montantTtc;
	
	@Column(name="montantTotal")
	private Double montantTotal;
	
	@Column(name="numPolice")
	private String numPolice;
	
	@Column(name="montantTva")
	private Double montantTva;
	
	@Column(name="produit")
	private String produit;
	
	@Column(name="type")
	private String type;
	
	@Column(name="date")
	private String date;
	
	@Column(name="fourniture")
	private String fourniture;
	
	@Column(name="montantSTbr")
	private Double montantSTbr;
	
	@Column(name="id_demande")
	private int iddemande;
	
	/** reconciliation ecom lydec*/
	@Column(name="etat")
	private String etat;
	
	@Column(name="numrecnaps")
	private String numrecnaps;
	
	@Column(name="datepai")
	private String datepai;
	
	@Column(name="trxFactureLydec")
	private String trxFactureLydec;

}
