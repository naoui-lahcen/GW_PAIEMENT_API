package ma.m2m.gateway.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.SplittableRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.mappers.DemandePaiementMapper;
import ma.m2m.gateway.model.DemandePaiement;
import ma.m2m.gateway.repository.DemandePaiementDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class DemandePaiementServiceImpl implements DemandePaiementService {

	//@Autowired(required = true)
	private final DemandePaiementDao demandePaiementDao;
	
	private DemandePaiementMapper demandePaiementMapper = new DemandePaiementMapper();
	
	public DemandePaiementServiceImpl( DemandePaiementDao demandePaiementDao) {
		this.demandePaiementDao = demandePaiementDao;
	}
	
	@Override
	public List<DemandePaiementDto> findAllDemandePaiement() {
		return demandePaiementMapper.modelList2VOList(demandePaiementDao.findAll());
	}

	@Override
	public DemandePaiementDto save(DemandePaiementDto demandePaiementDto) {
		
		DemandePaiement demandePaiement = demandePaiementMapper.vo2Model(demandePaiementDto);

		DemandePaiementDto demandePaiementSaved = demandePaiementMapper.model2VO(demandePaiementDao.save(demandePaiement));
		
		return demandePaiementSaved;
	}

	@Override
	public DemandePaiementDto findByIdDemande(Integer id) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByiddemande(id));
	}

	@Override
	public DemandePaiementDto findByCommande(String commande) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByCommande(commande));
	}
	
	@Override
	public DemandePaiementDto findByCommandeAndComid(String commande, String comid) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByCommandeAndComid(commande, comid));
	}

	@Override
	public DemandePaiementDto findSWPAYEByNumCommandeAndNumCommercant(String commande, String comid) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByCommandeAndComid(commande, comid));
	}

	@Override
	public DemandePaiementDto findByTokencommande(String tokencommande) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByTokencommande(tokencommande));
	}
	
	@Override
	public DemandePaiementDto findByCommandeAndComidAndRefdemande(String commande, String comid, String refdemande) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByCommandeAndComidAndRefdemande(commande, comid, refdemande));
	}
	
	@Override
	public DemandePaiementDto findByDem_xid(String xid) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByDemxid(xid));
	}
	
	@Override
	public void deleteViaId(long id) {
		demandePaiementDao.deleteById(id);	
	}

	@Override
	public DemandePaiementDto findByCommandeAndComidAndDate(String commande, String comid, String dateDem) {
		return demandePaiementMapper.model2VO(demandePaiementDao.findByCommandeAndComidAndDate(commande, comid, dateDem));
	}
	

	
	
}
