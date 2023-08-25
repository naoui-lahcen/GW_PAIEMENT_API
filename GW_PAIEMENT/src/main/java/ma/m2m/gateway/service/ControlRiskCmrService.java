package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.ControlRiskCmrDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface ControlRiskCmrService {

	ControlRiskCmrDto findByNumCommercant(String numCmr);
}
