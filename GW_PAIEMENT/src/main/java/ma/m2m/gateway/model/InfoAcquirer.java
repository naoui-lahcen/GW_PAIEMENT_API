package ma.m2m.gateway.model;

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
@Table(name="INFO_ACQ")
public class InfoAcquirer implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ACQ_ID")
	private long acqId;
	
	@Column(name="ACQ_HOST")
	private String acqHost;
	
	@Column(name="ACQ_PORT")
	private String acqPort;
	
	@Column(name="ACQ_BANQUE")
	private String acqBanque;
	
	@Column(name="ACQ_COM")
	private String acqCom;

	public InfoAcquirer() {
	}

	public InfoAcquirer(long acqId, String acqHost, String acqPort,
			String acqBanque, String acqCom) {
		this.acqId = acqId;
		this.acqHost = acqHost;
		this.acqPort = acqPort;
		this.acqBanque = acqBanque;
		this.acqCom = acqCom;
	}

	public long getAcqId() {
		return this.acqId;
	}

	public void setAcqId(long acqId) {
		this.acqId = acqId;
	}

	public String getAcqHost() {
		return this.acqHost;
	}

	public void setAcqHost(String acqHost) {
		this.acqHost = acqHost;
	}

	public String getAcqPort() {
		return this.acqPort;
	}

	public void setAcqPort(String acqPort) {
		this.acqPort = acqPort;
	}

	public String getAcqBanque() {
		return this.acqBanque;
	}

	public void setAcqBanque(String acqBanque) {
		this.acqBanque = acqBanque;
	}

	public String getAcqCom() {
		return this.acqCom;
	}

	public void setAcqCom(String acqCom) {
		this.acqCom = acqCom;
	}

	@Override
	public String toString() {
		return "InfoAcquirer [acqId=" + acqId + ", acqHost=" + acqHost + ", acqPort=" + acqPort + ", acqBanque="
				+ acqBanque + ", acqCom=" + acqCom + "]";
	}

}
