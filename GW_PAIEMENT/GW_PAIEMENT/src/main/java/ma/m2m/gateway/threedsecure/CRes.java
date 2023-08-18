package ma.m2m.gateway.threedsecure;

import java.io.Serializable;

import lombok.Data;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Data
public class CRes implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String threeDSServerTransID;
	private String acsTransID;
	private String messageType;
	private String messageVersion;
	private String transStatus;
	
	
	public String getThreeDSServerTransID() {
		return threeDSServerTransID;
	}

	public void setThreeDSServerTransID(String threeDSServerTransID) {
		this.threeDSServerTransID = threeDSServerTransID;
	}

	public String getAcsTransID() {
		return acsTransID;
	}

	public void setAcsTransID(String acsTransID) {
		this.acsTransID = acsTransID;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessageVersion() {
		return messageVersion;
	}

	public void setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
	}

	public String getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(String transStatus) {
		this.transStatus = transStatus;
	}

	public CRes(String threeDSServerTransID, String acsTransID, String messageType, String messageVersion,
			String transStatus) {
		super();
		this.threeDSServerTransID = threeDSServerTransID;
		this.acsTransID = acsTransID;
		this.messageType = messageType;
		this.messageVersion = messageVersion;
		this.transStatus = transStatus;
	}

	public CRes() {
		super();
	}
	

}
