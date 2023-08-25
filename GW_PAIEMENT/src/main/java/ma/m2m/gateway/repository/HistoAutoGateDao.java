package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.HistoAutoGate;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface HistoAutoGateDao extends JpaRepository<HistoAutoGate,Long> {
	
	HistoAutoGate findByHatNumCommande(String commande);
	
	HistoAutoGate findByHatNumCommandeAndHatNumcmr(String commande, String numCmr);
	
	HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth, String numCmr);
	
	@Query(value="select max(HAT_ID)  FROM  HistoAutoGate", nativeQuery = true)
	Integer getMAX_ID();

}