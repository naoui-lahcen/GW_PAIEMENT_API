package ma.m2m.gateway.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RequestDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String capture;
	private String Transactiontype;
	private String currency;
	private String orderid;
	private String recurring;
	private String amount;
	private String successURL;
	private String failURL;
	private String promocode;
	private String transactionid;
	private String Securtoken24;
	private String mac_value;
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
	public String getCapture() {
		return capture;
	}
	public void setCapture(String capture) {
		this.capture = capture;
	}
	public String getTransactiontype() {
		return Transactiontype;
	}
	public void setTransactiontype(String transactiontype) {
		Transactiontype = transactiontype;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getOrderid() {
		return orderid;
	}
	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}
	public String getRecurring() {
		return recurring;
	}
	public void setRecurring(String recurring) {
		this.recurring = recurring;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getSuccessURL() {
		return successURL;
	}
	public void setSuccessURL(String successURL) {
		this.successURL = successURL;
	}
	public String getFailURL() {
		return failURL;
	}
	public void setFailURL(String failURL) {
		this.failURL = failURL;
	}
	public String getPromocode() {
		return promocode;
	}
	public void setPromocode(String promocode) {
		this.promocode = promocode;
	}
	public String getTransactionid() {
		return transactionid;
	}
	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}
	public String getSecurtoken24() {
		return Securtoken24;
	}
	public void setSecurtoken24(String securtoken24) {
		Securtoken24 = securtoken24;
	}
	public String getMac_value() {
		return mac_value;
	}
	public void setMac_value(String mac_value) {
		this.mac_value = mac_value;
	}
	public String getMerchantid() {
		return merchantid;
	}
	public void setMerchantid(String merchantid) {
		this.merchantid = merchantid;
	}
	public String getMerchantname() {
		return merchantname;
	}
	public void setMerchantname(String merchantname) {
		this.merchantname = merchantname;
	}
	public String getWebsitename() {
		return websitename;
	}
	public void setWebsitename(String websitename) {
		this.websitename = websitename;
	}
	public String getWebsiteid() {
		return websiteid;
	}
	public void setWebsiteid(String websiteid) {
		this.websiteid = websiteid;
	}
	public String getCallbackurl() {
		return callbackurl;
	}
	public void setCallbackurl(String callbackurl) {
		this.callbackurl = callbackurl;
	}
	public String getFname() {
		return fname;
	}
	public void setFname(String fname) {
		this.fname = fname;
	}
	public String getLname() {
		return lname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public RequestDto() {
		super();
	}
	public RequestDto(String capture, String transactiontype, String currency, String orderid, String recurring,
			String amount, String successURL, String failURL, String promocode, String transactionid,
			String securtoken24, String mac_value, String merchantid, String merchantname, String websitename,
			String websiteid, String callbackurl, String fname, String lname, String email, String country,
			String phone, String city, String state, String zipcode, String address) {
		super();
		this.capture = capture;
		Transactiontype = transactiontype;
		this.currency = currency;
		this.orderid = orderid;
		this.recurring = recurring;
		this.amount = amount;
		this.successURL = successURL;
		this.failURL = failURL;
		this.promocode = promocode;
		this.transactionid = transactionid;
		Securtoken24 = securtoken24;
		this.mac_value = mac_value;
		this.merchantid = merchantid;
		this.merchantname = merchantname;
		this.websitename = websitename;
		this.websiteid = websiteid;
		this.callbackurl = callbackurl;
		this.fname = fname;
		this.lname = lname;
		this.email = email;
		this.country = country;
		this.phone = phone;
		this.city = city;
		this.state = state;
		this.zipcode = zipcode;
		this.address = address;
	}
	@Override
	public String toString() {
		return "RequestDto [capture=" + capture + ", Transactiontype=" + Transactiontype + ", currency=" + currency
				+ ", orderid=" + orderid + ", recurring=" + recurring + ", amount=" + amount + ", successURL="
				+ successURL + ", failURL=" + failURL + ", promocode=" + promocode + ", transactionid=" + transactionid
				+ ", Securtoken24=" + Securtoken24 + ", mac_value=" + mac_value + ", merchantid=" + merchantid
				+ ", merchantname=" + merchantname + ", websitename=" + websitename + ", websiteid=" + websiteid
				+ ", callbackurl=" + callbackurl + ", fname=" + fname + ", lname=" + lname + ", email=" + email
				+ ", country=" + country + ", phone=" + phone + ", city=" + city + ", state=" + state + ", zipcode="
				+ zipcode + ", address=" + address + "]";
	}
	

}
