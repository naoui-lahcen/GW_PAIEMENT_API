package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.CommercantDto;

public interface CommercantService {
	
	CommercantDto findByCmrCode(String numCMR);

}
