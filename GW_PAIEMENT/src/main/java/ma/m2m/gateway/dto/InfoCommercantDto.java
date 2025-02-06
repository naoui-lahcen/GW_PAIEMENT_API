package ma.m2m.gateway.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoCommercantDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long idInfo;
	
	private String cmrCode;
	
	private String cmrEmail;
	
	private String password;
	
	private String passwordMpi;
	
	private String clePub;
	
	private String clePriv;
	
	private String cmrNom;
	
	private String cmrPwd;
	
	private String cmrBin;
	
	private String cmrUrl;
	
	private String cmrVille;
	
	private String cmrCurrency;
	
	private String cmrExponent;
	
	private String cmrPurchamont;
	
	private String cleApi;
	
    private String apiUrl;

}
