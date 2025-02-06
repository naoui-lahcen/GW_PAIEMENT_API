package ma.m2m.gateway.service;

import java.util.List;

import ma.m2m.gateway.dto.HistoAutoGateDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface HistoAutoGateService {

	HistoAutoGateDto findByHatNumCommande(String commande);
	
	HistoAutoGateDto save(HistoAutoGateDto histoAutoGateDto);
		
	HistoAutoGateDto findByHatNumCommandeAndHatNumcmr(String commande, String numCmr);
	
	HistoAutoGateDto findByHatNumCommandeAndHatNumcmrV1(String commande, String numCmr);
	
	HistoAutoGateDto findLastByHatNumCommandeAndHatNumcmr(String commande, String numCmr);
	
	HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth, String numCmr);
	
	HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(String commande, String numAuth, String numCmr, String codeRep);
	
	List<HistoAutoGateDto> findByHatNumcmr(String numCmr);
	
	HistoAutoGateDto findByHatNumCommandeAndHatNumcmrAndHatPorteur(String commande, String numCmr, String cardnumber);
	
	HistoAutoGateDto findById(Integer id);
	
	List<HistoAutoGateDto> findAll();
	
	Integer getMAX_ID();
	
	Double getCommercantGlobalFlowPerDay(String numCmr);
	
	List<HistoAutoGateDto> getPorteurMerchantFlowPerDay(String numCmr, String cardnumber);
}
