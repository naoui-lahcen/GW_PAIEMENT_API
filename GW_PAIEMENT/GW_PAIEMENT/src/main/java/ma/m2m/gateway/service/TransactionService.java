package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.TransactionDto;

public interface TransactionService {
	
	TransactionDto findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr);
	
	Integer getMAX_ID();
	
	TransactionDto save(TransactionDto trs);

}
