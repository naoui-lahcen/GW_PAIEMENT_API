package ma.m2m.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="GALLERIE")
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

	

	public Galerie() {
	}

	public Galerie(long idGal) {
		this.idGal = idGal;
	}

	public Galerie(long idGal, String codeGal, String nomGal, String urlGal,
			String codeCmr, Date dateActivation, String categorieGal,
			String description, String etat) {
		this.idGal = idGal;
		this.codeGal = codeGal;
		this.nomGal = nomGal;
		this.urlGal = urlGal;
		this.codeCmr = codeCmr;
		this.dateActivation = dateActivation;
		this.categorieGal = categorieGal;
		this.description = description;
		this.etat = etat;
	}

	public long getIdGal() {
		return this.idGal;
	}

	public void setIdGal(long idGal) {
		this.idGal = idGal;
	}

	public String getCodeGal() {
		return this.codeGal;
	}

	public void setCodeGal(String codeGal) {
		this.codeGal = codeGal;
	}

	public String getNomGal() {
		return this.nomGal;
	}

	public void setNomGal(String nomGal) {
		this.nomGal = nomGal;
	}

	public String getUrlGal() {
		return this.urlGal;
	}

	public void setUrlGal(String urlGal) {
		this.urlGal = urlGal;
	}

	public String getCodeCmr() {
		return this.codeCmr;
	}

	public void setCodeCmr(String codeCmr) {
		this.codeCmr = codeCmr;
	}

	public Date getDateActivation() {
		return this.dateActivation;
	}

	public void setDateActivation(Date dateActivation) {
		this.dateActivation = dateActivation;
	}

	public String getCategorieGal() {
		return this.categorieGal;
	}

	public void setCategorieGal(String categorieGal) {
		this.categorieGal = categorieGal;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEtat() {
		return this.etat;
	}

	public void setEtat(String etat) {
		this.etat = etat;
	}


}
