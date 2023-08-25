package ma.m2m.gateway.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;

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
public class ControlRiskCmrDto implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private String numCommercant;
	
	private String acceptInternational;
	
	private String isGlobalFlowControlActive;

	private Double globalFlowPerDay;
	
	private Double flowCardPerDay;
	
	private Integer numberOfTransactionCardPerDay;

	private Double transactionMaxAmount;

}
