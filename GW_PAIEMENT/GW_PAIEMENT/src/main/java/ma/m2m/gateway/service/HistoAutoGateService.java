package ma.m2m.gateway.service;

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
	
	HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth, String numCmr);
	
	Integer getMAX_ID();
}
