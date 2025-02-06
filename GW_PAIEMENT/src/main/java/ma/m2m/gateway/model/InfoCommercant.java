package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="INFO_COMMERCANT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InfoCommercant implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ID_INFO")
	private long idInfo;
	
	@Column(name="CMR_CODE")
	private String cmrCode;
	
	@Column(name="CMR_EMAIL")
	private String cmrEmail;
	
	@Column(name="PASSWORD")
	private String password;
	
	@Column(name="PASSWORD_MPI")
	private String passwordMpi;
	
	@Column(name="CLE_PUB")
	private String clePub;
	
	@Column(name="CLE_PRIV")
	private String clePriv;
	
	@Column(name="CMR_NOM")
	private String cmrNom;
	
	@Column(name="CMR_PWD")
	private String cmrPwd;
	
	@Column(name="CMR_BIN")
	private String cmrBin;
	
	@Column(name="CMR_URL")
	private String cmrUrl;
	
	@Column(name="CMR_VILLE")
	private String cmrVille;
	
	@Column(name="CMR_CURRENCY")
	private String cmrCurrency;
	
	@Column(name="CMR_EXPONENT")
	private String cmrExponent;
	
	@Column(name="CMR_PURCHAMONT")
	private String cmrPurchamont;
	
	@Column(name="cle_api")
	private String cleApi;
	
	@Column(name="api_url")
    private String apiUrl;

}
