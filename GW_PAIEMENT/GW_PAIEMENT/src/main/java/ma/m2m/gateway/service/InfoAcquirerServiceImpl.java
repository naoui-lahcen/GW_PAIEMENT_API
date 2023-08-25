package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.InfoAcquirerDto;
import ma.m2m.gateway.mappers.InfoAcquirerMapper;
import ma.m2m.gateway.repository.InfoAcquirerDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class InfoAcquirerServiceImpl implements InfoAcquirerService {
	
	InfoAcquirerMapper acquirerMapper = new InfoAcquirerMapper();
	
	@Autowired
	InfoAcquirerDao acquirerDao;

	@Override
	public InfoAcquirerDto findByAcqCom(String acqCom) {
		return acquirerMapper.model2VO(acquirerDao.findByAcqCom(acqCom));
	}

}
