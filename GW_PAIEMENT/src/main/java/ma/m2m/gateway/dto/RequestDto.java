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
public class RequestDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String capture;
	private String transactiontype;
	private String currency;
	private String orderid;
	private String recurring;
	private String amount;
	private String successURL;
	private String failURL;
	private String promocode;
	private String transactionid;
	private String securtoken24;
	private String macValue;
	private String merchantid;
	private String merchantname;
	private String websitename;
	private String websiteid;
	private String callbackurl;
	private String fname;
	private String lname;
	private String email;
	private String country;
	private String phone;
	private String city;
	private String state;
	private String zipcode;
	private String address;

}
