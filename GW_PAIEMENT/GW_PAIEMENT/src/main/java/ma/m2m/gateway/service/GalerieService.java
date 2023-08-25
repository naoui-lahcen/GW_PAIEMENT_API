package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.GalerieDto;

public interface GalerieService {
	
	GalerieDto findByCodeGalAndCodeCmr(String codeGal, String codeCmr);
	
	GalerieDto findByCodeCmr(String codeCmr);

}
