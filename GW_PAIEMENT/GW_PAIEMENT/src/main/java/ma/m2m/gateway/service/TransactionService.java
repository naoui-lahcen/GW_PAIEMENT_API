package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.TransactionDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface TransactionService {
	
	TransactionDto findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr);
	
	Integer getMAX_ID();
	
	TransactionDto save(TransactionDto trs);

}
