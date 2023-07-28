package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.HistoAutoGateDto;

public interface HistoAutoGateService {

	HistoAutoGateDto findByHatNumCommande(String commande);
	
	HistoAutoGateDto save(HistoAutoGateDto histoAutoGateDto);
	
	HistoAutoGateDto findByHatNumCommandeAndHatNumcmr(String commande, String numCmr);
	
	HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth, String numCmr);
	
	Integer getMAX_ID();
}
