package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CardtokenDto {
	
	private Integer id;
	private String idToken;
	private String idMerchant;
	private String mcc;
	private String idMerchantClient;
	private String idClientuuid;
	private String token;
	private Date tokenDate;
	private String typeCarte;
	private String cardNumber;
	private String cardMask;
	private String holderName;
	private String firstName;
	private String lastName;
	private Date exprDate;
	private String acqbanq;
	private long idProfiletoken;

}
