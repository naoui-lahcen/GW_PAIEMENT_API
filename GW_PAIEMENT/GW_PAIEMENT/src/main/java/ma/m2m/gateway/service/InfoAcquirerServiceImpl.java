package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.InfoAcquirerDto;
import ma.m2m.gateway.mappers.InfoAcquirerMapper;
import ma.m2m.gateway.repository.InfoAcquirerDao;

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
