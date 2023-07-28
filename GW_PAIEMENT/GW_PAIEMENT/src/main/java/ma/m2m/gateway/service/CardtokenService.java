package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.CardtokenDto;

public interface CardtokenService {
	
	CardtokenDto save(CardtokenDto cardtoken);
	
	void delete(CardtokenDto cardtoken);
	
	CardtokenDto findByIdMerchantAndToken(String merchantid, String token);
	
	Integer getMAX_ID();
}
