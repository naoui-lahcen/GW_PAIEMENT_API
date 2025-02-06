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
* @since   2023-12-11
 */

@Entity
@Table(name="DataDGI")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataDGI implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idDataDGI")
	private Integer idDataDGI;
	
	@Column(name="storeId")
	private String storeId; //code commerçant
	
	@Column(name="dateTrx")
	private String dateTrx;
	
	@Column(name="commande")
	private String commande;
	
	@Column(name="MONTANT")
	private Double montant;
	
	@Column(name="checksum")
	private String checksum;
	
	@Column(name="xml")
	private String xml;
	
	@Column(name="etat")
	private String etat;
	
	@Column(name="id_demande")
	private int iddemande;

}
