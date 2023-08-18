package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.SWHistoAutoDto;
import ma.m2m.gateway.mappers.SWHistoAutoMapper;
import ma.m2m.gateway.repository.SWHistoAutoDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class SWHistoAutoServiceImpl implements SWHistoAutoService {
	
	SWHistoAutoMapper swHistoAutoMapper = new SWHistoAutoMapper();
	
	@Autowired
	SWHistoAutoDao swHistoAutoDao;

	@Override
	public SWHistoAutoDto getSWHistoAuto(String cardnumber, String rrn, String amount, String date_auto,
			String merchantid) {
		return swHistoAutoMapper.model2VO(swHistoAutoDao.getSWHistoAuto(cardnumber, rrn, amount, date_auto, merchantid));
	}

	@Override
	public SWHistoAutoDto getNumCMR(String merchantid) {
		return swHistoAutoMapper.model2VO(swHistoAutoDao.getNumCMR(merchantid));
	}

}
