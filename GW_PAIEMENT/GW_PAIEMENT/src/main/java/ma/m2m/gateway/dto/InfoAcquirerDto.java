package ma.m2m.gateway.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
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

}
