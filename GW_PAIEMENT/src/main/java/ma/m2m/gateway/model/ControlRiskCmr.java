package ma.m2m.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="CONTROL_RISK_CMR")
@Data
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

	public String getNumCommercant() {
		return numCommercant;
	}

	public void setNumCommercant(String numCommercant) {
		this.numCommercant = numCommercant;
	}

	public String getAcceptInternational() {
		return acceptInternational;
	}

	public void setAcceptInternational(String acceptInternational) {
		this.acceptInternational = acceptInternational;
	}

	public String getIsGlobalFlowControlActive() {
		return isGlobalFlowControlActive;
	}

	public void setIsGlobalFlowControlActive(String isGlobalFlowControlActive) {
		this.isGlobalFlowControlActive = isGlobalFlowControlActive;
	}

	public Double getGlobalFlowPerDay() {
		return globalFlowPerDay;
	}

	public void setGlobalFlowPerDay(Double globalFlowPerDay) {
		this.globalFlowPerDay = globalFlowPerDay;
	}

	public Double getFlowCardPerDay() {
		return flowCardPerDay;
	}

	public void setFlowCardPerDay(Double flowCardPerDay) {
		this.flowCardPerDay = flowCardPerDay;
	}

	public Integer getNumberOfTransactionCardPerDay() {
		return numberOfTransactionCardPerDay;
	}

	public void setNumberOfTransactionCardPerDay(Integer numberOfTransactionCardPerDay) {
		this.numberOfTransactionCardPerDay = numberOfTransactionCardPerDay;
	}

	public Double getTransactionMaxAmount() {
		return transactionMaxAmount;
	}

	public void setTransactionMaxAmount(Double transactionMaxAmount) {
		this.transactionMaxAmount = transactionMaxAmount;
	}

	public ControlRiskCmr(String numCommercant, String acceptInternational, String isGlobalFlowControlActive,
			Double globalFlowPerDay, Double flowCardPerDay, Integer numberOfTransactionCardPerDay,
			Double transactionMaxAmount) {
		super();
		this.numCommercant = numCommercant;
		this.acceptInternational = acceptInternational;
		this.isGlobalFlowControlActive = isGlobalFlowControlActive;
		this.globalFlowPerDay = globalFlowPerDay;
		this.flowCardPerDay = flowCardPerDay;
		this.numberOfTransactionCardPerDay = numberOfTransactionCardPerDay;
		this.transactionMaxAmount = transactionMaxAmount;
	}

	public ControlRiskCmr() {
		super();
	}

	@Override
	public String toString() {
		return "ControlRiskCmr [numCommercant=" + numCommercant + ", acceptInternational=" + acceptInternational
				+ ", isGlobalFlowControlActive=" + isGlobalFlowControlActive + ", globalFlowPerDay=" + globalFlowPerDay
				+ ", flowCardPerDay=" + flowCardPerDay + ", numberOfTransactionCardPerDay="
				+ numberOfTransactionCardPerDay + ", transactionMaxAmount=" + transactionMaxAmount + "]";
	}
	
	
}
