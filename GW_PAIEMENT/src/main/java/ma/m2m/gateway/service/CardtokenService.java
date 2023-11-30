package ma.m2m.gateway.service;

import java.util.Date;
import java.util.List;

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
	
	List<CardtokenDto> findByIdMerchantAndIdMerchantClient(String merchantid, String idclient);
	
	CardtokenDto findByIdMerchantAndTokenAndExprDate(String merchantid, String token, Date dateExp);
	
	Integer getMAX_ID();
}
