package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.CodeReponseDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30 
 */

public interface CodeReponseService {
	
	CodeReponseDto findByRpcCode(String code);

}
