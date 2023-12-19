package ma.m2m.gateway.dto;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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

	public ControlRiskCmrDto(String numCommercant, String acceptInternational, String isGlobalFlowControlActive,
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

	public ControlRiskCmrDto() {
		super();
	}

	@Override
	public String toString() {
		return "ControlRiskCmrDto [numCommercant=" + numCommercant + ", acceptInternational=" + acceptInternational
				+ ", isGlobalFlowControlActive=" + isGlobalFlowControlActive + ", globalFlowPerDay=" + globalFlowPerDay
				+ ", flowCardPerDay=" + flowCardPerDay + ", numberOfTransactionCardPerDay="
				+ numberOfTransactionCardPerDay + ", transactionMaxAmount=" + transactionMaxAmount + "]";
	}

	
}
