package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.mappers.ControlRiskCmrMapper;
import ma.m2m.gateway.repository.ControlRiskCmrDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class ControlRiskCmrServiceImpl implements ControlRiskCmrService {
	
	private ControlRiskCmrMapper controlRiskCmrMapper = new ControlRiskCmrMapper();
	
	//@Autowired
	private final ControlRiskCmrDao controlRiskCmrDao;

	public ControlRiskCmrServiceImpl(ControlRiskCmrDao controlRiskCmrDao) {
		this.controlRiskCmrDao = controlRiskCmrDao;
	}

	@Override
	public ControlRiskCmrDto findByNumCommercant(String numCmr) {
		return controlRiskCmrMapper.model2VO(controlRiskCmrDao.findByNumCommercant(numCmr));
	}

}
