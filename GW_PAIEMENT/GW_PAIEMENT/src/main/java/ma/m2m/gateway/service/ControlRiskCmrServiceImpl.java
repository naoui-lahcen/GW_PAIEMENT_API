package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.mappers.ControlRiskCmrMapper;
import ma.m2m.gateway.repository.ControlRiskCmrDao;

@Service
public class ControlRiskCmrServiceImpl implements ControlRiskCmrService {
	
	private ControlRiskCmrMapper controlRiskCmrMapper = new ControlRiskCmrMapper();
	
	@Autowired
	ControlRiskCmrDao controlRiskCmrDao;

	@Override
	public ControlRiskCmrDto findByNumCommercant(String numCmr) {
		return controlRiskCmrMapper.model2VO(controlRiskCmrDao.findByNumCommercant(numCmr));
	}

}
