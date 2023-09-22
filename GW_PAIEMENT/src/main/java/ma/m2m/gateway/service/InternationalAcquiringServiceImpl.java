package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.InternationalAcquiringDto;
import ma.m2m.gateway.mappers.InternationalAcquiringMapper;
import ma.m2m.gateway.repository.InternationalAcquiringDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21 
 */

@Service
public class InternationalAcquiringServiceImpl implements InternationalAcquiringService {
	
	private InternationalAcquiringMapper internationalAcquiringMapper = new InternationalAcquiringMapper();
	
	@Autowired
	InternationalAcquiringDao internationalAcquiringDao;

	@Override
	public InternationalAcquiringDto findByNumCommercant(String numCMR) {
		
		return internationalAcquiringMapper.model2VO(internationalAcquiringDao.findByNumCommercant(numCMR));
	}

}
