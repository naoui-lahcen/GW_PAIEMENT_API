package ma.m2m.gateway.threedsecure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ma.m2m.gateway.utils.Util;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthInitRequest {
	
	private String pan;
	
	private Double amount;
	
	private String currency;
	
	private String idCommercant;
	
	private Integer idDemande;
	
	/*
	 * Format: YYMM
	 */
	private String expiry; 
	
	private String acquirerBIN;
	
	private String browserAcceptHeader;
	
	private String browserUserAgent;
	
	private String email;
	
	private String mcc;
	
	private String merchantCountryCode;
	
	private String nomCommercant;
	
	private String notificationURL;
	
	private String threeDSRequestorAuthenticationInd = "01";
	
	private String messageCategory = "01";
	
	private String urlThreeDSS;

	// synchronisation du gw avec mpi
	private String acquirerMerchantID;
	private String acctNumber;
	private Double purchaseAmount;
	private String cardExpiryDate;
	private String purchaseCurrency;
	private String merchantName;
	private String purchaseDate;
	private String cardholderName;
	
}
