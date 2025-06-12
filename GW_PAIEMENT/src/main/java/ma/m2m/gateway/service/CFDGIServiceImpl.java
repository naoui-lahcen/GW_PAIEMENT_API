package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ma.m2m.gateway.dto.CFDGIDto;
import ma.m2m.gateway.mappers.CFDGIMapper;
import ma.m2m.gateway.model.CFDGI;
import ma.m2m.gateway.repository.CFDGIDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

@Service
public class CFDGIServiceImpl implements CFDGIService {
	
	//@Autowired
	private final CFDGIDao cfdgiDao;
	
	public CFDGIServiceImpl(CFDGIDao cfdgiDao) {
		this.cfdgiDao = cfdgiDao;
	}

	private CFDGIMapper cfdgiMapper = new CFDGIMapper();

	@Override
	public CFDGIDto findCFDGIByIddemande(int iddemande) {
		return cfdgiMapper.model2VO(cfdgiDao.findCFDGIByIddemande(iddemande));
	}

	@Override
	public CFDGIDto save(CFDGIDto cFDGIDto) {
		CFDGI cFDGI = cfdgiMapper.vo2Model(cFDGIDto);
		return cfdgiMapper.model2VO(cfdgiDao.save(cFDGI));
	}

}
