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
@Table(name="INFO_ACQ")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

}
