package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CodeReponseDto implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer rpcId;
	private String rpcCode;
	private String rpcLibelle;
	private String rpcSyspay;
	private String rpcLangue;

}
