package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.CommercantDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface CommercantService {
	
	CommercantDto findByCmrCode(String numCMR);

}
