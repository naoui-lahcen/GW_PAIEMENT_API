package ma.m2m.gateway.risk;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21
 */

public enum GWRiskAnalysisMsgs {
	
//	INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT("Service non autorisé : contrôle risque"),
//	DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT("Opération rejetée: contrôle risque"),
//	DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE("Opération rejetée: contrôle risque"),
//	DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE("Opération rejetée: contrôle risque"),
//	TRANSACTION_MAX_AMOUNT("Opération rejetée: contrôle risque");	
	
	INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT("Carte internationale non autorisée chez le commerçant. Veuillez utiliser une autre carte."),
	DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT("La transaction en cours n’a pas abouti. Veuillez contacter votre commerçant"),
	DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE("La transaction en cours n’a pas abouti. Le plafond jounalier de votre carte est atteint, veuillez contacter votre banque."),
	DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE("La transaction en cours n’a pas abouti. Veuillez contacter votre commerçant"),
	TRANSACTION_MAX_AMOUNT("La transaction en cours n’a pas abouti. Le plafond par transaction est dépassé, veuillez contacter votre banque");	
	
	private String valueFR;

	public String getValueFR() {
		return valueFR;
	}

	public void setValueFR(String valueFR) {
		this.valueFR = valueFR;
	}

	private GWRiskAnalysisMsgs(String valueFR) {
		this.valueFR = valueFR;
	}

	private GWRiskAnalysisMsgs() {
	}
	 
	 

}
