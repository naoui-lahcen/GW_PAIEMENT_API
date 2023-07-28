package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.mappers.GalerieMapper;
import ma.m2m.gateway.repository.GalerieDao;

@Service
public class GalerieServiceImpl implements GalerieService {
	
	private GalerieMapper galerieMapper = new GalerieMapper();
	
	@Autowired
	GalerieDao galerieDao;

	@Override
	public GalerieDto findByCodeGalAndCodeCmr(String codeGal, String codeCmr) {
		
		return galerieMapper.model2VO(galerieDao.findByCodeGalAndCodeCmr(codeGal, codeCmr));
	}

	@Override
	public GalerieDto findByCodeCmr(String codeCmr) {
		return galerieMapper.model2VO(galerieDao.findByCodeCmr(codeCmr));
	}

}
