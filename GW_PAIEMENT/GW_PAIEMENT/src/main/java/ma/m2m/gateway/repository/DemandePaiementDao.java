package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.DemandePaiement;

@Repository
public interface DemandePaiementDao extends JpaRepository<DemandePaiement,Long> {
	
	DemandePaiement findByiddemande(Integer xid);
	
	DemandePaiement findByCommande(String commande);
	
	DemandePaiement findByCommandeAndComid(String commande, String comid);
	
	@Query(value = "select * from DemandePaiement dp where "
			+ "dp.commande=?1 "
			+ "AND dp.comid=?2 "
			+ "AND dp.etat_demande LIKE 'SW_PA%' ", nativeQuery = true)
	DemandePaiement findSWPAYEByNumCommandeAndNumCommercant(String numCommande, String numCommercant);
	
	DemandePaiement findByTokencommande(String tokencommande);
	
	DemandePaiement findByCommandeAndComidAndRefdemande(String commande, String comid, String refdemande);
	
	DemandePaiement findByDemxid(String xid);

}
