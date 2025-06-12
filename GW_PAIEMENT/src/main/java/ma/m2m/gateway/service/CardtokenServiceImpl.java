package ma.m2m.gateway.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.mappers.CardtokenMapper;
import ma.m2m.gateway.model.Cardtoken;
import ma.m2m.gateway.repository.CardtokenDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class CardtokenServiceImpl implements CardtokenService {
	
	CardtokenMapper cardtokenMapper = new CardtokenMapper();
	
	//@Autowired
	private final CardtokenDao cardtokenDao;

	public CardtokenServiceImpl(CardtokenDao cardtokenDao) {
		this.cardtokenDao = cardtokenDao;
	}

	@Override
	public CardtokenDto save(CardtokenDto cardtokenDto) {
		
		Cardtoken cardtoken = cardtokenMapper.vo2Model(cardtokenDto);
		
		return cardtokenMapper.model2VO(cardtokenDao.save(cardtoken));
	}

	@Override
	public CardtokenDto findByIdMerchantAndToken(String merchantid, String token) {
		return cardtokenMapper.model2VO(cardtokenDao.findByIdMerchantAndToken(merchantid, token));
	}
	
	@Override
	public List<CardtokenDto> findByIdMerchantClientAndCardNumber(String merchantid, String cardNumber) {
		return cardtokenMapper.modelList2VOList(cardtokenDao.findByIdMerchantClientAndCardNumber(merchantid, cardNumber));
	}
	
	@Override
	public List<CardtokenDto> findByIdMerchantAndIdMerchantClient(String merchantid, String idclient) {
		return cardtokenMapper.modelList2VOList(cardtokenDao.findByIdMerchantAndIdMerchantClient(merchantid, idclient));
	}
	
	@Override
	public CardtokenDto findByIdMerchantAndTokenAndExprDate(String merchantid, String token, Date dateExp) {
		return cardtokenMapper.model2VO(cardtokenDao.findByIdMerchantAndTokenAndExprDate(merchantid, token, dateExp));
	}

	@Override
	public Integer getMAX_ID() {
		return cardtokenDao.getMAX_ID();
	}

	@Override
	public void delete(CardtokenDto cardtokenDto) {
		Cardtoken cardtoken = cardtokenMapper.vo2Model(cardtokenDto);
		cardtokenDao.delete(cardtoken);
	}
	

}
