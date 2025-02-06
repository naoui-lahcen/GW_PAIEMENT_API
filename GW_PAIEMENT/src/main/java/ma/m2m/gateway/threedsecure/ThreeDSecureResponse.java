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
public class ThreeDSecureResponse implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String reponseMPI;
	private String eci;
	private String cavv;
	private String threeDSServerTransID; /* xid*/
	private String idDemande;
	private String errmpi;
	private String htmlCreq;
	private String expiry; // YYMM

}
