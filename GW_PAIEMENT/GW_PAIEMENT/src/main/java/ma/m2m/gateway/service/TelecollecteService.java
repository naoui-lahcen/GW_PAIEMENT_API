package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.TelecollecteDto;

public interface TelecollecteService {
	
	TelecollecteDto getMAXTLC_N(String merchantid);
	
	TelecollecteDto save(TelecollecteDto tlcDto);
	
	Integer getMAX_ID();

}
