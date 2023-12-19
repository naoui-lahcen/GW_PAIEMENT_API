package ma.m2m.gateway.dto;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class InfoAcquirerDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private long acqId;
	
	private String acqHost;
	
	private String acqPort;
	
	private String acqBanque;
	
	private String acqCom;

	public long getAcqId() {
		return acqId;
	}

	public void setAcqId(long acqId) {
		this.acqId = acqId;
	}

	public String getAcqHost() {
		return acqHost;
	}

	public void setAcqHost(String acqHost) {
		this.acqHost = acqHost;
	}

	public String getAcqPort() {
		return acqPort;
	}

	public void setAcqPort(String acqPort) {
		this.acqPort = acqPort;
	}

	public String getAcqBanque() {
		return acqBanque;
	}

	public void setAcqBanque(String acqBanque) {
		this.acqBanque = acqBanque;
	}

	public String getAcqCom() {
		return acqCom;
	}

	public void setAcqCom(String acqCom) {
		this.acqCom = acqCom;
	}

	public InfoAcquirerDto(long acqId, String acqHost, String acqPort, String acqBanque, String acqCom) {
		super();
		this.acqId = acqId;
		this.acqHost = acqHost;
		this.acqPort = acqPort;
		this.acqBanque = acqBanque;
		this.acqCom = acqCom;
	}

	public InfoAcquirerDto() {
		super();
	}

	@Override
	public String toString() {
		return "InfoAcquirerDto [acqId=" + acqId + ", acqHost=" + acqHost + ", acqPort=" + acqPort + ", acqBanque="
				+ acqBanque + ", acqCom=" + acqCom + "]";
	}

	
}
