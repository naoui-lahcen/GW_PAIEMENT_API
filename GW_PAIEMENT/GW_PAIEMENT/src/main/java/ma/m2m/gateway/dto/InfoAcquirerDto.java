package ma.m2m.gateway.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
