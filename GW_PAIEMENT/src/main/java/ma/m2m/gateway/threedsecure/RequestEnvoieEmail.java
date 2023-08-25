package ma.m2m.gateway.threedsecure;

import java.io.Serializable;

import lombok.Data;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Data
public class RequestEnvoieEmail implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int idDemande ;
	
	private String idCommande ;
	
	private String numCmr;

	
	public RequestEnvoieEmail(int idDemande, String idCommande, String numCmr) {
		super();
		this.idDemande = idDemande;
		this.idCommande = idCommande;
		this.numCmr = numCmr;
	}

	public RequestEnvoieEmail() {
		super();
	}

	@Override
	public String toString() {
		return "RequestEnvoieEmail [idDemande=" + idDemande + ", idCommande=" + idCommande + ", numCmr=" + numCmr
				+ "]";
	}

}
