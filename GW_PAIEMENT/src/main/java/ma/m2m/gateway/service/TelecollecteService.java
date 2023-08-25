package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.TelecollecteDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface TelecollecteService {
	
	TelecollecteDto getMAXTLC_N(String merchantid);
	
	TelecollecteDto save(TelecollecteDto tlcDto);
	
	Integer getMAX_ID();

}
