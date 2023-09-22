package ma.m2m.gateway.risk;

import static ma.m2m.gateway.config.FlagActivation.ACTIVE;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;

import ma.m2m.gateway.Utils.Util;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.model.ControlRiskCmr;
import ma.m2m.gateway.repository.ControlRiskCmrDao;
import ma.m2m.gateway.repository.HistoAutoGateDao;
import ma.m2m.gateway.service.ControlRiskCmrService;
import ma.m2m.gateway.service.HistoAutoGateService;

import static ma.m2m.gateway.Utils.StringUtils.isNullOrEmpty;


/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21
 */

public class GWRiskAnalysis {

	/* ------------------------ DAO INSTANCES ------------------------- */
	@Autowired
	private ControlRiskCmrDao controlRiskCmrDAO;
	@Autowired
	private ControlRiskCmrService controlRiskCmrService;
	@Autowired
	private HistoAutoGateService histoAutoGateService;
	
	/* --- LOG INSTANCES --- */
	private String logFolder;
	private String logFile;

	public GWRiskAnalysis(String logFolder, String logFile) {
		this.logFolder = logFolder;
		this.logFile = logFile;
	}

	public String executeRiskControls(String numCmr, double montant, String cardnumber,
			ControlRiskCmrDto controlRiskCmr,List<HistoAutoGateDto> porteurFlowPerDay) throws GWRiskAnalysisException {
		//ControlRiskCmrDto controlRiskCmr = controlRiskCmrService.findByNumCommercant(numCmr);
		if (controlRiskCmr == null) {
			return "500 ControlRiskCmr misconfigured in DB or not existing ";
		}

		Util.writeInFileTransaction(logFolder, logFile, "COMMERCANT RISK PARAMS : " + controlRiskCmr.toString());
		
		/* ------------------------- Controle de montant max autorisé par transaction ----------------------------*/
		if(controlRiskCmr.getTransactionMaxAmount() != null && controlRiskCmr.getTransactionMaxAmount() > 0) {
			if(montant > controlRiskCmr.getTransactionMaxAmount()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.TRANSACTION_MAX_AMOUNT.toString());
				//throw new GWRiskAnalysisException(GWRiskAnalysisMsgs.TRANSACTION_MAX_AMOUNT.getValueFR());
				return GWRiskAnalysisMsgs.TRANSACTION_MAX_AMOUNT.getValueFR();
			}
		}	
		/* --------------------------------------------------------------------------------------------------------*/
		
		/* --------------------------------- Controle des cartes internationales -----------------------------------------*/
		if (isNullOrEmpty(controlRiskCmr.getAcceptInternational()) && (controlRiskCmr.getAcceptInternational() != null)
						&& !ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getAcceptInternational().trim())) {
			Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT.toString());
			//throw new GWRiskAnalysisException(GWRiskAnalysisMsgs.INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT.getValueFR());
			return GWRiskAnalysisMsgs.INTERNATIONAL_CARD_NOT_PERMITTED_FOR_MERCAHNT.getValueFR();
		}
		/* ----------------------------------------------------------------------------------------------------------------*/
		
		/* --------------------------------- Controle de flux journalier autorisé par commerçant  ----------------------------------*/
		if(!isNullOrEmpty(controlRiskCmr.getIsGlobalFlowControlActive()) && ACTIVE.getFlag().equalsIgnoreCase(controlRiskCmr.getIsGlobalFlowControlActive())) {
			double globalFlowPerDay = histoAutoGateService.getCommercantGlobalFlowPerDay(numCmr);
			
			if(controlRiskCmr.getGlobalFlowPerDay() != null && globalFlowPerDay > controlRiskCmr.getGlobalFlowPerDay()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT.toString());
				//throw new GWRiskAnalysisException(GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT.getValueFR());
				return GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_MERCAHNT.getValueFR();
			}
		}
		/* -------------------------------------------------------------------------------------------------------------------------*/
		
		/* ------------------------- Controle de flux journalier autorisé par client (porteur de carte) ----------------------------*/
		if((controlRiskCmr.getFlowCardPerDay() != null && controlRiskCmr.getFlowCardPerDay() > 0) 
				|| (controlRiskCmr.getNumberOfTransactionCardPerDay() != null && controlRiskCmr.getNumberOfTransactionCardPerDay() > 0)) {
			
			Double flowCardPerDay = 0.0;
			int nbrTrxCardPerDay = porteurFlowPerDay.size();
			for(HistoAutoGateDto hist : porteurFlowPerDay) {
				flowCardPerDay += hist.getHatMontant();
			}
			if(flowCardPerDay >= controlRiskCmr.getFlowCardPerDay()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE.toString());
				//throw new GWRiskAnalysisException(GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE.getValueFR());
				return GWRiskAnalysisMsgs.DAILY_QUOTA_AUTHORIZATIONS_EXCEEDED_FOR_CARTE.getValueFR();
			}
			if(nbrTrxCardPerDay >= controlRiskCmr.getNumberOfTransactionCardPerDay()) {
				Util.writeInFileTransaction(logFolder, logFile, "[ERROR_RISK_GW_CONTROLS] : " + GWRiskAnalysisMsgs.DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE.toString());
				//throw new GWRiskAnalysisException(GWRiskAnalysisMsgs.DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE.getValueFR());
				return GWRiskAnalysisMsgs.DAILY_NBR_TRANSACTIONS_EXCEEDED_FOR_CARTE.getValueFR();
			}
		}
		/* -------------------------------------------------------------------------------------------------------------------------*/

		return "OK";
	}
	
	public String executeControlInternationalCarte(String numCmr) throws GWRiskAnalysisException {
		
		ControlRiskCmr controlRiskCmr = controlRiskCmrDAO.findByNumCommercant(numCmr);
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
