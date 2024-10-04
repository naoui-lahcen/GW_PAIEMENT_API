package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.TransactionDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface TransactionService {
	
	TransactionDto findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr);
	
	TransactionDto findByTrsnumautAndTrsnumcmrAndTrsmontant(String numAuth, String cumCmr, Double montant);
	
	TransactionDto findByTrsnumautAndTrsnumcmrAndTrscommande(String numAuth, String cumCmr, String commande);
	
	TransactionDto findByTrsnumautAndTrsnumcmrAndDateTrs(String numAuth, String cumCmr, String dateTrs);
	
	Integer getMAX_ID();
	
	TransactionDto save(TransactionDto trs);

}
