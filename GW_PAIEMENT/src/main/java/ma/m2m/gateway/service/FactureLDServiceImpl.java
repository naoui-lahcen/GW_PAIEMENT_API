package ma.m2m.gateway.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ma.m2m.gateway.dto.FactureLDDto;
import ma.m2m.gateway.mappers.FactureLDMapper;
import ma.m2m.gateway.model.FactureLD;
import ma.m2m.gateway.repository.FactureLDDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */

@Service
public class FactureLDServiceImpl implements FactureLDService {
	
	//@Autowired
	private final FactureLDDao factureLDDao;
	
	private FactureLDMapper factureLDMapper = new FactureLDMapper();

	public FactureLDServiceImpl(FactureLDDao factureLDDao) {
		this.factureLDDao = factureLDDao;
	}

	@Override
	public List<FactureLDDto> findFactureByIddemande(Integer iddemande) {
		
		return factureLDMapper.modelList2VOList(factureLDDao.findFactureByIddemande(iddemande));
	}

	@Override
	public FactureLDDto save(FactureLDDto factureLDDto) {
		FactureLD factureLD = factureLDMapper.vo2Model(factureLDDto);
		
		FactureLDDto FactureLDSaved = factureLDMapper.model2VO(factureLDDao.save(factureLD));
		return FactureLDSaved;
	}

}
