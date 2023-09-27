package ma.m2m.gateway.model;

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
	private String first_name;
	
	@Column(name="LAST_NAME")
	private String last_name;
	
	@Column(name="EXPR_DATE")
	private Date exprDate;
	
	@Column(name="ACQBANQ")
	private String acqbanq;
	
	@Column(name="ID_PROFILETOKEN")
	private long idProfiletoken;

	public Cardtoken() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public String getIdMerchant() {
		return idMerchant;
	}

	public void setIdMerchant(String idMerchant) {
		this.idMerchant = idMerchant;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getIdMerchantClient() {
		return idMerchantClient;
	}

	public void setIdMerchantClient(String idMerchantClient) {
		this.idMerchantClient = idMerchantClient;
	}

	public String getIdClientuuid() {
		return idClientuuid;
	}

	public void setIdClientuuid(String idClientuuid) {
		this.idClientuuid = idClientuuid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getTokenDate() {
		return tokenDate;
	}

	public void setTokenDate(Date tokenDate) {
		this.tokenDate = tokenDate;
	}

	public String getTypeCarte() {
		return typeCarte;
	}

	public void setTypeCarte(String typeCarte) {
		this.typeCarte = typeCarte;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardMask() {
		return cardMask;
	}

	public void setCardMask(String cardMask) {
		this.cardMask = cardMask;
	}

	public String getHolderName() {
		return holderName;
	}

	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public Date getExprDate() {
		return exprDate;
	}

	public void setExprDate(Date exprDate) {
		this.exprDate = exprDate;
	}

	public String getAcqbanq() {
		return acqbanq;
	}

	public void setAcqbanq(String acqbanq) {
		this.acqbanq = acqbanq;
	}

	public long getIdProfiletoken() {
		return idProfiletoken;
	}

	public void setIdProfiletoken(long idProfiletoken) {
		this.idProfiletoken = idProfiletoken;
	}

	public Cardtoken(Integer id, String idToken, String idMerchant, String mcc, String idMerchantClient,
			String idClientuuid, String token, Date tokenDate, String typeCarte, String cardNumber, String cardMask,
			String holderName, String first_name, String last_name, Date exprDate, String acqbanq,
			long idProfiletoken) {
		super();
		this.id = id;
		this.idToken = idToken;
		this.idMerchant = idMerchant;
		this.mcc = mcc;
		this.idMerchantClient = idMerchantClient;
		this.idClientuuid = idClientuuid;
		this.token = token;
		this.tokenDate = tokenDate;
		this.typeCarte = typeCarte;
		this.cardNumber = cardNumber;
		this.cardMask = cardMask;
		this.holderName = holderName;
		this.first_name = first_name;
		this.last_name = last_name;
		this.exprDate = exprDate;
		this.acqbanq = acqbanq;
		this.idProfiletoken = idProfiletoken;
	}

	@Override
	public String toString() {
		return "Cardtoken [id=" + id + ", idToken=" + idToken + ", idMerchant=" + idMerchant + ", mcc=" + mcc
				+ ", idMerchantClient=" + idMerchantClient + ", idClientuuid=" + idClientuuid + ", token=" + token
				+ ", tokenDate=" + tokenDate + ", typeCarte=" + typeCarte + ", cardNumber=" + cardNumber + ", cardMask="
				+ cardMask + ", holderName=" + holderName + ", first_name=" + first_name + ", last_name=" + last_name
				+ ", exprDate=" + exprDate + ", acqbanq=" + acqbanq + ", idProfiletoken=" + idProfiletoken + "]";
	}

}