package ma.m2m.gateway.repository;

import java.util.List;

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
	
	List<HistoAutoGate> findByHatNumcmr(String numCmr);
	
	@Query(value="select max(HAT_ID)  FROM  HistoAutoGate", nativeQuery = true)
	Integer getMAX_ID();
	
	@Query(value = "SELECT IFNULL(sum(HAT_MONTANT), 0) as globalFlowPerDay"
			+ "FROM MXGATEWAY.HISTOAUTO_GATE "
			+ "WHERE HAT_NUMCMR = (?1) "
			+ "AND HAT_CODEREP = '00' "
			+ "AND HAT_DATDEM LIKE (?2)", nativeQuery = true)
	Double getCommercantGlobalFlowPerDay(String numCmr, String dateDem);
	
	@Query(value = "select * from MXGATEWAY.HISTOAUTO_GATE where "
			+ "HAT_NUMCMR = (?1) "
			+ "AND HAT_PORTEUR = (?2) "
			+ "AND HAT_CODEREP = '00' "
			+ "AND HAT_DATDEM LIKE (?3)", nativeQuery = true)
	List<HistoAutoGate> getPorteurMerchantFlowPerDay(String numCmr, String cardnumber, String dateDem);

}
