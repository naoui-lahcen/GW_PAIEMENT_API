package ma.m2m.gateway.dto;

import java.io.Serializable;
import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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

	public long getIdGal() {
		return idGal;
	}

	public void setIdGal(long idGal) {
		this.idGal = idGal;
	}

	public String getCodeGal() {
		return codeGal;
	}

	public void setCodeGal(String codeGal) {
		this.codeGal = codeGal;
	}

	public String getNomGal() {
		return nomGal;
	}

	public void setNomGal(String nomGal) {
		this.nomGal = nomGal;
	}

	public String getUrlGal() {
		return urlGal;
	}

	public void setUrlGal(String urlGal) {
		this.urlGal = urlGal;
	}

	public String getCodeCmr() {
		return codeCmr;
	}

	public void setCodeCmr(String codeCmr) {
		this.codeCmr = codeCmr;
	}

	public Date getDateActivation() {
		return dateActivation;
	}

	public void setDateActivation(Date dateActivation) {
		this.dateActivation = dateActivation;
	}

	public String getCategorieGal() {
		return categorieGal;
	}

	public void setCategorieGal(String categorieGal) {
		this.categorieGal = categorieGal;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEtat() {
		return etat;
	}

	public void setEtat(String etat) {
		this.etat = etat;
	}

	public GalerieDto() {
		super();
	}

	public GalerieDto(long idGal, String codeGal, String nomGal, String urlGal, String codeCmr, Date dateActivation,
			String categorieGal, String description, String etat) {
		super();
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

	@Override
	public String toString() {
		return "GalerieDto [idGal=" + idGal + ", codeGal=" + codeGal + ", nomGal=" + nomGal + ", urlGal=" + urlGal
				+ ", codeCmr=" + codeCmr + ", dateActivation=" + dateActivation + ", categorieGal=" + categorieGal
				+ ", description=" + description + ", etat=" + etat + "]";
	}
	
	
	
	

}
