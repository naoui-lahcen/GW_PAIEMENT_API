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
@Table(name="CommandeFac")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommandeFac implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer  id ;
	
	@Column(name="codeClient")
	private String codeClient;
	
	@Column(name="nomprenom")
	private String nomprenom;
	
	@Column(name="numCommande")
	private String numCommande;
	
	@Column(name="email")
	private String email;
	
	@Column(name="montantTotal")
	private Double montantTotal;
	
	@Column(name="MontantTotalTva")
	private Double MontantTotalTva;
	
	@Column(name="MontantTotalTtc")
	private Double MontantTotalTtc;
	
	@Column(name="MontantTotalTbr")
	private Double MontantTotalTbr;
	
	@Column(name="successUrl")
	private String successUrl;
	
	@Column(name="recallUrl")
	private String recallUrl;
	
	@Column(name="failurl")
	private String failurl;
	
	@Column(name="cmr")
	private String cmr;
	
	@Column(name="gal")
	private String gal;
	
	@Column(name="date")
	private String date;
	
	@Column(name="checksum")
	private String checksum;
	
	@Column(name="xml")
	private String xml;
	
	@Column(name="etat")
	private String etat;

}
