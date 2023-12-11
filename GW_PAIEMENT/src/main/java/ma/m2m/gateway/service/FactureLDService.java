package ma.m2m.gateway.service;

import java.util.List;

import ma.m2m.gateway.dto.FactureLDDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
public interface FactureLDService {
	
	List<FactureLDDto> findFactureByIddemande(Integer iddemande);
	
	FactureLDDto save(FactureLDDto factureLD);

}
