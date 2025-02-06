package ma.m2m.gateway.threedsecure;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestEnvoieEmail implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int idDemande ;
	
	private String idCommande ;
	
	private String numCmr;

}
