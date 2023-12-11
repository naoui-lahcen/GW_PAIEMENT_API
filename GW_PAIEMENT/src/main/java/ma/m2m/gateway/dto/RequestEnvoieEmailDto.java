package ma.m2m.gateway.dto;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27 
 */

public class RequestEnvoieEmailDto implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int idDemande ;
	
	private String idCommande ;
	
	private String numCmr;
	
	public int getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(int idDemande) {
		this.idDemande = idDemande;
	}

	public String getIdCommande() {
		return idCommande;
	}

	public void setIdCommande(String idCommande) {
		this.idCommande = idCommande;
	}

	public String getNumCmr() {
		return numCmr;
	}

	public void setNumCmr(String numCmr) {
		this.numCmr = numCmr;
	}

	public RequestEnvoieEmailDto(int idDemande, String idCommande, String numCmr) {
		super();
		this.idDemande = idDemande;
		this.idCommande = idCommande;
		this.numCmr = numCmr;
	}

	public RequestEnvoieEmailDto() {
		super();
	}

	@Override
	public String toString() {
		return "RequestEnvoieEmail [idDemande=" + idDemande + ", idCommande=" + idCommande + ", numCmr=" + numCmr
				+ "]";
	}

}
