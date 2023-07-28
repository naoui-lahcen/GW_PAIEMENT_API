package ma.m2m.gateway.service;

import java.util.List;

import org.springframework.stereotype.Service;
import ma.m2m.gateway.dto.DemandePaiementDto;

//@Service
public interface DemandePaiementService {
	
	List<DemandePaiementDto> findAllDemandePaiement();
	
	DemandePaiementDto save(DemandePaiementDto demandePaiement);
    
    DemandePaiementDto findByIdDemande(Integer id);
        
    DemandePaiementDto findByCommande(String commande);
        
    DemandePaiementDto findByCommandeAndComid(String commande, String comid);
    
    DemandePaiementDto findSWPAYEByNumCommandeAndNumCommercant(String numCommande, String numCommercant);
    
    DemandePaiementDto findByTokencommande(String tokencommande);
    
    DemandePaiementDto findByCommandeAndComidAndRefdemande(String commande, String comid, String refdemande);
    
    DemandePaiementDto findByDem_xid(String xid);
    
    void deleteViaId(long id);
   

}
