package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.InternationalAcquiringDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21
 */

public interface InternationalAcquiringService {
	
	InternationalAcquiringDto findByNumCommercant(String numCMR);

}
