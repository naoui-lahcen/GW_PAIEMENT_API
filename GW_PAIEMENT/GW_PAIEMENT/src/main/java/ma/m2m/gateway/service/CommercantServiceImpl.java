package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.mappers.CommercantMapper;
import ma.m2m.gateway.repository.CommercantDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class CommercantServiceImpl implements CommercantService {
	
	private CommercantMapper commercantMapper = new CommercantMapper();
	
	@Autowired
	CommercantDao commercantDao;

	@Override
	public CommercantDto findByCmrCode(String numCMR) {
		return commercantMapper.model2VO(commercantDao.findByCmrCode(numCMR));
	}

}
