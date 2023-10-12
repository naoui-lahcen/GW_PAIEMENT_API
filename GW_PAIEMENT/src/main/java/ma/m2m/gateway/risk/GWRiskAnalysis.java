package ma.m2m.gateway.risk;

import static ma.m2m.gateway.config.FlagActivation.ACTIVE;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.service.ControlRiskCmrService;
import static ma.m2m.gateway.Utils.StringUtils.isNullOrEmpty;


/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21
 */

public class GWRiskAnalysis {

	/* ------------------------ DAO INSTANCES ------------------------- */
	@Autowired
	private ControlRiskCmrService controlRiskCmrService;

	
	/* --- LOG INSTANCES --- */
	private String logFolder;
	private String logFile;

	public GWRiskAnalysis(String logFolder, String logFile) {
		this.logFolder = logFolder;
		this.logFile = logFile;
	}

	public String executeRiskControls(String numCmr, double montant, String cardnumber,
			ControlRiskCmrDto controlRiskCmr, Double globalFlowPerDay, List<HistoAutoGateDto> porteurFlowPerDay,List<EmetteurDto> listBin) throws GWRiskAnalysisException {
		
		if (controlRiskCmr == null) {
			return "ControlRiskCmr misconfigured in DB or not existing ";
		}

		Util.writeInFileTransaction(logFolder, logFile, "COMMERCANT RISK PARAMS : " + controlRiskCmr.toString());
		
		/* ------------------------- Controle de montant max autorisé par transaction ----------------------------*/
		if(controlRiskCmr.getTransactionMaxAmount() != null && controlRiskCmr.getTransactionMaxAmount() > 0) {
			Util.writeInFileTransaction(logFolder, logFile, "Controle de montant max autorisé par transaction");
			Util.writeInFileTransaction(logFolder, logFile, "montant / TransactionMaxAmount : " + montant +"/" + controlRiskCmr.getTransactionMaxAmount());
			
			if(montant > controlRiskCmr.getTransactionMaxAmount()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.TRANSACTION_MAX_AMOUNT.toString());
				return GWRiskAnalysisMsgs.TRANSACTION_MAX_AMOUNT.getValueFR();
			}
		}	
		/* --------------------------------------------------------------------------------------------------------*/
		
		/* --------------------------------- Controle des cartes internationales -----------------------------------------*/
		if (isNullOrEmpty(controlRiskCmr.getAcceptInternational()) || (controlRiskCmr.getAcceptInternational() != null
						&& !ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getAcceptInternational().trim()))) {
			
			Util.writeInFileTransaction(logFolder, logFile, "Controle des cartes internationales");

			if(listBin.size() == 0) {
				Util.writeInFileTransaction(logFolder, logFile, "Le cmr n'accepte pas les trs I et cette carte est I car son bin n'est pas paramétré dans la table EMETTEIR");

				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT.toString());
				return GWRiskAnalysisMsgs.INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT.getValueFR();
			} else {
				Util.writeInFileTransaction(logFolder, logFile, "Carte National N ");
			}
		}
		/* ----------------------------------------------------------------------------------------------------------------*/
		
		/* --------------------------------- Controle de flux journalier autorisé par commerçant  ----------------------------------*/
		if(!isNullOrEmpty(controlRiskCmr.getIsGlobalFlowControlActive()) && ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getIsGlobalFlowControlActive())) {
			
			Util.writeInFileTransaction(logFolder, logFile, "Controle de flux journalier autorisé par commerçant");

			if(globalFlowPerDay == null) {
				globalFlowPerDay = 0.00;
			}
			Util.writeInFileTransaction(logFolder, logFile, "globalFlowPerDay / controlRiskCmrGlobalFlowPerDay : " + globalFlowPerDay +"/" + controlRiskCmr.getGlobalFlowPerDay());
			
			Double globalFlowPerDayWithCurrentAmount=globalFlowPerDay + montant;
			
			Util.writeInFileTransaction(logFolder, logFile, "globalFlowPerDayWithCurrentAmount / controlRiskCmrGlobalFlowPerDay : " + globalFlowPerDayWithCurrentAmount +"/" + controlRiskCmr.getGlobalFlowPerDay());
			
			if(controlRiskCmr.getGlobalFlowPerDay() != null && globalFlowPerDayWithCurrentAmount > controlRiskCmr.getGlobalFlowPerDay()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT.toString());
				return GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT.getValueFR();
			}
		}
		/* -------------------------------------------------------------------------------------------------------------------------*/
		
		/* ------------------------- Controle de flux journalier autorisé par client (porteur de carte) ----------------------------*/
		if((controlRiskCmr.getFlowCardPerDay() != null && controlRiskCmr.getFlowCardPerDay() > 0) 
				|| (controlRiskCmr.getNumberOfTransactionCardPerDay() != null && controlRiskCmr.getNumberOfTransactionCardPerDay() > 0)) {
			
			Util.writeInFileTransaction(logFolder, logFile, "Controle de flux journalier autorisé par client (porteur de carte)");

			Double flowCardPerDay = 0.0;
			int nbrTrxCardPerDay = porteurFlowPerDay.size();
			Util.writeInFileTransaction(logFolder, logFile, "nbrTrxCardPerDay : " + nbrTrxCardPerDay);
			
			for(HistoAutoGateDto hist : porteurFlowPerDay) {
				flowCardPerDay += hist.getHatMontant();
			}
			
			Util.writeInFileTransaction(logFolder, logFile, "controlRiskCmrFlowCardPerDay /  flowCardPerDay: " + controlRiskCmr.getFlowCardPerDay() +"/" + flowCardPerDay);
			
			Double flowCardPerDayWithCurrentAmount=flowCardPerDay + montant;
			
			Util.writeInFileTransaction(logFolder, logFile, "controlRiskCmrFlowCardPerDay / flowCardPerDayWithCurrentAmount : " + controlRiskCmr.getFlowCardPerDay() +"/" + flowCardPerDayWithCurrentAmount);
			
			if(flowCardPerDayWithCurrentAmount >= controlRiskCmr.getFlowCardPerDay()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE.toString());
				return GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE.getValueFR();
			}
			
			Util.writeInFileTransaction(logFolder, logFile, "nbrTrxCardPerDay / NumberOfTransactionCardPerDay : " + nbrTrxCardPerDay +"/" + controlRiskCmr.getNumberOfTransactionCardPerDay());
			
			if(nbrTrxCardPerDay >= controlRiskCmr.getNumberOfTransactionCardPerDay()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE.toString());
				return GWRiskAnalysisMsgs.DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE.getValueFR();
			}
		}
		/* -------------------------------------------------------------------------------------------------------------------------*/

		return "OK";
	}
	
	public String executeControlInternationalCarte(String numCmr) throws GWRiskAnalysisException {
		
		ControlRiskCmrDto controlRiskCmr = controlRiskCmrService.findByNumCommercant(numCmr);
		if (controlRiskCmr == null) 
			return "KO";
		Util.writeInFileTransaction(logFolder, logFile, "COMMERCANT RISK PARAMS : " + controlRiskCmr.toString());

		/* --------------------------------- Controle des cartes internationales -----------------------------------------*/
		if (!controlRiskCmr.getAcceptInternational().equals("") && (ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getAcceptInternational().trim()) ) ) {
			
			Util.writeInFileTransaction(logFolder, logFile, "[CMR_ACCEPT_CARTE_INTERNATIONAL_GW_CONTROLS] : " + numCmr);
			return "OK";
		} else {
			return "KO";
		}
		/* ----------------------------------------------------------------------------------------------------------------*/
	}
}
