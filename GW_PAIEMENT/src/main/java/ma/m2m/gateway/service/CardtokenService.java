package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.CardtokenDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface CardtokenService {
	
	CardtokenDto save(CardtokenDto cardtoken);
	
	void delete(CardtokenDto cardtoken);
	
	CardtokenDto findByIdMerchantAndToken(String merchantid, String token);
	
	Integer getMAX_ID();
}
