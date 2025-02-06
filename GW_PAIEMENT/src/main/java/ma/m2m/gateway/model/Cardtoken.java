package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="CARDTOKEN")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cardtoken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="ID")
	private Integer id;
	
	@Column(name="ID_TOKEN")
	private String idToken;
	
	@Column(name="ID_MERCHANT")
	private String idMerchant;
	
	@Column(name="MCC")
	private String mcc;
	
	@Column(name="ID_MERCHANT_CLIENT")
	private String idMerchantClient;
	
	@Column(name="ID_ClientUUID")
	private String idClientuuid;
	
	@Column(name="TOKEN")
	private String token;
	
	@Column(name="TOKEN_DATE")
	private Date tokenDate;
	
	@Column(name="TYPE_CARTE")
	private String typeCarte;
	
	@Column(name="CARD_NUMBER")
	private String cardNumber;
	
	@Column(name="CARD_MASK")
	private String cardMask;
	
	@Column(name="HOLDER_NAME")
	private String holderName;
	
	@Column(name="FIRST_NAME")
	private String firstName;
	
	@Column(name="LAST_NAME")
	private String lastName;
	
	@Column(name="EXPR_DATE")
	private Date exprDate;
	
	@Column(name="ACQBANQ")
	private String acqbanq;
	
	@Column(name="ID_PROFILETOKEN")
	private long idProfiletoken;

}