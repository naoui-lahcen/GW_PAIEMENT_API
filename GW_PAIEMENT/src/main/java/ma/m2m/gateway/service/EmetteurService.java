package ma.m2m.gateway.service;

import java.util.List;

import ma.m2m.gateway.dto.EmetteurDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */

public interface EmetteurService {
	
	List<EmetteurDto> findByBindebut(String binDebut);

}