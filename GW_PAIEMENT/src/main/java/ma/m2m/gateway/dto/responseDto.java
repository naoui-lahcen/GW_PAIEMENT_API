package ma.m2m.gateway.dto;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class responseDto implements Serializable {
	
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



	public String getErrorNb() {
		return errorNb;
	}

	public void setErrorNb(String errorNb) {
		this.errorNb = errorNb;
	}

	public String getMsgRetour() {
		return msgRetour;
	}

	public void setMsgRetour(String msgRetour) {
		this.msgRetour = msgRetour;
	}

	public String getUrlRetour() {
		return urlRetour;
	}

	public void setUrlRetour(String urlRetour) {
		this.urlRetour = urlRetour;
	}

	public String getStatuscode() {
		return statuscode;
	}

	public void setStatuscode(String statuscode) {
		this.statuscode = statuscode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getTransactiondate() {
		return transactiondate;
	}

	public void setTransactiondate(String transactiondate) {
		this.transactiondate = transactiondate;
	}

	public String getTransactiontime() {
		return transactiontime;
	}

	public void setTransactiontime(String transactiontime) {
		this.transactiontime = transactiontime;
	}

	public String getTransactionid() {
		return transactionid;
	}

	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthnumber() {
		return authnumber;
	}

	public void setAuthnumber(String authnumber) {
		this.authnumber = authnumber;
	}

	public String getPaymentid() {
		return paymentid;
	}

	public void setPaymentid(String paymentid) {
		this.paymentid = paymentid;
	}

	public String getEtataut() {
		return etataut;
	}

	public void setEtataut(String etataut) {
		this.etataut = etataut;
	}

	public String getTreedsid() {
		return treedsid;
	}

	public void setTreedsid(String treedsid) {
		this.treedsid = treedsid;
	}

	public String getLinkacs() {
		return linkacs;
	}

	public void setLinkacs(String linkacs) {
		this.linkacs = linkacs;
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

	public String getCardnumber() {
		return cardnumber;
	}

	public void setCardnumber(String cardnumber) {
		this.cardnumber = cardnumber;
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

	public String getNumTransLydec() {
		return numTransLydec;
	}

	public void setNumTransLydec(String numTransLydec) {
		this.numTransLydec = numTransLydec;
	}

	public String getMontantTTC() {
		return montantTTC;
	}

	public void setMontantTTC(String montantTTC) {
		this.montantTTC = montantTTC;
	}

	public responseDto() {
		super();
	}

	public responseDto(String errorNb, String msgRetour, String urlRetour, String statuscode, String status,
			String orderid, Double amount, String transactiondate, String transactiontime, String transactionid,
			String url) {
		super();
		this.errorNb = errorNb;
		this.msgRetour = msgRetour;
		this.urlRetour = urlRetour;
		this.statuscode = statuscode;
		this.status = status;
		this.orderid = orderid;
		this.amount = amount;
		this.transactiondate = transactiondate;
		this.transactiontime = transactiontime;
		this.transactionid = transactionid;
		this.url = url;
	}

	public responseDto(String errorNb, String msgRetour, String urlRetour, String statuscode, String status,
			String orderid, Double amount, String transactiondate, String transactiontime, String transactionid,
			String url, String authnumber, String paymentid, String etataut, String treedsid, String linkacs,
			String merchantid, String merchantname, String websitename, String websiteid, String callbackurl,
			String cardnumber, String fname, String lname, String email) {
		super();
		this.errorNb = errorNb;
		this.msgRetour = msgRetour;
		this.urlRetour = urlRetour;
		this.statuscode = statuscode;
		this.status = status;
		this.orderid = orderid;
		this.amount = amount;
		this.transactiondate = transactiondate;
		this.transactiontime = transactiontime;
		this.transactionid = transactionid;
		this.url = url;
		this.authnumber = authnumber;
		this.paymentid = paymentid;
		this.etataut = etataut;
		this.treedsid = treedsid;
		this.linkacs = linkacs;
		this.merchantid = merchantid;
		this.merchantname = merchantname;
		this.websitename = websitename;
		this.websiteid = websiteid;
		this.callbackurl = callbackurl;
		this.cardnumber = cardnumber;
		this.fname = fname;
		this.lname = lname;
		this.email = email;
	}

	public responseDto(String errorNb, String msgRetour, String urlRetour, String statuscode, String status,
			String orderid, Double amount, String transactiondate, String transactiontime, String transactionid,
			String url, String authnumber, String paymentid, String etataut, String treedsid, String linkacs,
			String merchantid, String merchantname, String websitename, String websiteid, String callbackurl,
			String cardnumber, String fname, String lname, String email, String numTransLydec, String montantTTC) {
		super();
		this.errorNb = errorNb;
		this.msgRetour = msgRetour;
		this.urlRetour = urlRetour;
		this.statuscode = statuscode;
		this.status = status;
		this.orderid = orderid;
		this.amount = amount;
		this.transactiondate = transactiondate;
		this.transactiontime = transactiontime;
		this.transactionid = transactionid;
		this.url = url;
		this.authnumber = authnumber;
		this.paymentid = paymentid;
		this.etataut = etataut;
		this.treedsid = treedsid;
		this.linkacs = linkacs;
		this.merchantid = merchantid;
		this.merchantname = merchantname;
		this.websitename = websitename;
		this.websiteid = websiteid;
		this.callbackurl = callbackurl;
		this.cardnumber = cardnumber;
		this.fname = fname;
		this.lname = lname;
		this.email = email;
		this.numTransLydec = numTransLydec;
		this.montantTTC = montantTTC;
	}

	@Override
	public String toString() {
		return "responseDto [errorNb=" + errorNb + ", msgRetour=" + msgRetour + ", urlRetour=" + urlRetour
				+ ", statuscode=" + statuscode + ", status=" + status + ", orderid=" + orderid + ", amount=" + amount
				+ ", transactiondate=" + transactiondate + ", transactiontime=" + transactiontime + ", transactionid="
				+ transactionid + ", url=" + url + ", authnumber=" + authnumber + ", paymentid=" + paymentid
				+ ", etataut=" + etataut + ", treedsid=" + treedsid + ", linkacs=" + linkacs + ", merchantid="
				+ merchantid + ", merchantname=" + merchantname + ", websitename=" + websitename + ", websiteid="
				+ websiteid + ", callbackurl=" + callbackurl + ", cardnumber=" + cardnumber + ", fname=" + fname
				+ ", lname=" + lname + ", email=" + email + ", numTransLydec=" + numTransLydec + ", montantTTC="
				+ montantTTC + "]";
	}

}
