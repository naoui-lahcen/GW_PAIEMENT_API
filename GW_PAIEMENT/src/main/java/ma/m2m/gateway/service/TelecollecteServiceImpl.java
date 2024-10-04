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
	
	@Autowired
	TelecollecteDao telecollecteDao;
	
	@Override
	public TelecollecteDto getMAXTLC_N(String merchantid) {
		return telecollecteMapper.model2VO(telecollecteDao.getMAXTLC_N(merchantid));
	}

	@Override
	public TelecollecteDto save(TelecollecteDto tlcDto) {
		Telecollecte tlc= telecollecteMapper.vo2Model(tlcDto);
		
		TelecollecteDto tlctoSave = telecollecteMapper.model2VO(telecollecteDao.save(tlc));
		
		return tlctoSave;
	}

	@Override
	public Integer getMAX_ID() {
		Integer idTlc = telecollecteDao.getMAX_ID();
		return idTlc;
	}
	
	@Override
	public Integer getMAX_ID(String merchantid) {
		Integer idTlc = telecollecteDao.getMAX_ID(merchantid);
		return idTlc;
	}

}
