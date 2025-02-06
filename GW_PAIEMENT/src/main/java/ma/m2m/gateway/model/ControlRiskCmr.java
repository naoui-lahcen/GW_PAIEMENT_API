package ma.m2m.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="CONTROL_RISK_CMR")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ControlRiskCmr implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ctrl_numcmr")
	private String numCommercant;
	
	@Column(name="ctrl_accept_inter")
	private String acceptInternational;
	
	@Column(name="ctrl_is_global_flow_active")
	private String isGlobalFlowControlActive;

	@Column(name="ctrl_global_flow_per_day")
	private Double globalFlowPerDay;
	
	@Column(name="ctrl_flow_card_per_day")
	private Double flowCardPerDay;
	
	@Column(name="ctrl_nbr_trx_card_per_day")
	private Integer numberOfTransactionCardPerDay;

	@Column(name="ctrl_trx_max_amount")
	private Double transactionMaxAmount;

}
