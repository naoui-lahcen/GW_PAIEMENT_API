package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.InfoCommercantDto;

public interface InfoCommercantService {
	
	InfoCommercantDto findByCmrCode(String numCMR);

}
