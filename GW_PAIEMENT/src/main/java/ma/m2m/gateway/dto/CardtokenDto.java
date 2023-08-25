package ma.m2m.gateway.dto;

import java.util.Date;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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
	private String first_name;
	private String last_name;
	private String exprDate;
	private String acqbanq;
	private long idProfiletoken;

	public CardtokenDto() {
		super();
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

	public String getExprDate() {
		return exprDate;
	}

	public void setExprDate(String exprDate) {
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

	public CardtokenDto(Integer id, String idToken, String idMerchant, String mcc, String idMerchantClient,
			String idClientuuid, String token, Date tokenDate, String typeCarte, String cardNumber, String cardMask,
			String holderName, String first_name, String last_name, String exprDate, String acqbanq,
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
