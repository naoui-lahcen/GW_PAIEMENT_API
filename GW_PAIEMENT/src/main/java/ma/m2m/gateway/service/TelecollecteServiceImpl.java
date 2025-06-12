package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.mappers.TelecollecteMapper;
import ma.m2m.gateway.model.Telecollecte;
import ma.m2m.gateway.repository.TelecollecteDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class TelecollecteServiceImpl implements TelecollecteService {
	
	private TelecollecteMapper telecollecteMapper = new TelecollecteMapper();
	
	//@Autowired
	private final TelecollecteDao telecollecteDao;
	
	public TelecollecteServiceImpl(TelecollecteDao telecollecteDao) {
		this.telecollecteDao = telecollecteDao;
	}

	@Override
	public TelecollecteDto getMAXTLC_N(String merchantid) {
		return telecollecteMapper.model2VO(telecollecteDao.getMAXTLC_N(merchantid));
	}

	@Override
	public TelecollecteDto save(TelecollecteDto tlcDto) {
		Telecollecte tlc= telecollecteMapper.vo2Model(tlcDto);
		
		return telecollecteMapper.model2VO(telecollecteDao.save(tlc));
	}

	@Override
	public Integer getMAX_ID() {
		return telecollecteDao.getMAX_ID();
	}
	
	@Override
	public Integer getMAX_ID(String merchantid) {
		return telecollecteDao.getMAX_ID(merchantid);
	}

}
