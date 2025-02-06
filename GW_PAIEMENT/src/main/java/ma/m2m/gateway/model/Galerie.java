package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="GALLERIE")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Galerie implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ID_GAL")
	private long idGal;
	
	@Column(name="CODE_GAL")
	private String codeGal;
	
	@Column(name="NOM_GAL")
	private String nomGal;
	
	@Column(name="URL_GAL")
	private String urlGal;
	
	@Column(name="CODE_CMR")
	private String codeCmr;
	
	@Column(name="DATE_ACTIVATION")
	private Date dateActivation;
	
	@Column(name="CATEGORIE_GAL")
	private String categorieGal;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="ETAT")
	private String etat;

}
