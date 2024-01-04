package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.CFDGIDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

public interface CFDGIService {

	CFDGIDto findCFDGIByIddemande(int iddemande);
	
	CFDGIDto save(CFDGIDto cFDGIDto);
}
