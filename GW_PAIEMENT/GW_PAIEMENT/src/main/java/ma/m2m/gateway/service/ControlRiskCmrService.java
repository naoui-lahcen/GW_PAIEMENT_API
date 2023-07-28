package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.ControlRiskCmrDto;

public interface ControlRiskCmrService {

	ControlRiskCmrDto findByNumCommercant(String numCmr);
}
