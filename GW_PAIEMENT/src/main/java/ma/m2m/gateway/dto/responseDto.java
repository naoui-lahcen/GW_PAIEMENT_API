package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseDto implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String errorNb;
	
	private String msgRetour;
	
	private String urlRetour;
	
	private String statuscode;
	
	private String status;
	
	private String orderid;
	
	private Double amount;
	
	private String transactiondate;
	
	private String transactiontime;
	
	private String transactionid;
	
	private String url;
	
	private String authnumber;
	
	private String paymentid;
	
	private String etataut;
	
	private String treedsid;
	
	private String linkacs;
	
	private String merchantid;
	
	private String merchantname;
	
	private String websitename;
	
	private String websiteid;
	
	private String callbackurl;
	
	private String cardnumber;
	
	private String fname;
	
	private String lname;
	
	private String email;
	
	private String numTransLydec;
	
	private String montantTTC;

}
