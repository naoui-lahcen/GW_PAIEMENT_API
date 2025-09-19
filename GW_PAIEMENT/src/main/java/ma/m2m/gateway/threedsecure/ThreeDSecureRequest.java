package ma.m2m.gateway.threedsecure;

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
public class ThreeDSecureRequest {

	private String threeDSSURL;
	private String notificationURL;
	private String threeDSRequestorAuthenticationInd;
	private String messageCategory;

	private String pan;
	private Double amount;
	private String currency;
	private String idComercant;
	private Integer idDemande;
	private String expiry; // YYMM
	private String acquirerBIN;
	private String browserAcceptHeader;
	private String browserUserAgent;
	private String email;
	private String mcc;
	private String merchantCountryCode;
	private String nomCommercant;
	private String cardholderName;

}
