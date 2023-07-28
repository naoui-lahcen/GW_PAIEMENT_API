package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.mappers.InfoCommercantMapper;
import ma.m2m.gateway.repository.InfoCommercantDao;

@Service
public class InfoCommercantServiceImpl implements InfoCommercantService {

	@Autowired(required = true)
	InfoCommercantDao infoCommercantDao;
	
	private InfoCommercantMapper infoCommercantMapper = new InfoCommercantMapper();
	
	@Override
	public InfoCommercantDto findByCmrCode(String numCMR) {
		return infoCommercantMapper.model2VO(infoCommercantDao.findByCmrCode(numCMR));
	}
	
	
}
