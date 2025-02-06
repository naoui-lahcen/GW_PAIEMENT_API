package ma.m2m.gateway.model;

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
public class Param_COF  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	
	private Integer id;
	private String idMerchant;
	private String isCof;
	private String is3dsNormal;
	private String is3dsCof;
	private String isCvvNormal;
	private String isCvvCof;
	private String multiSelect;
	private int prfToken;

}
