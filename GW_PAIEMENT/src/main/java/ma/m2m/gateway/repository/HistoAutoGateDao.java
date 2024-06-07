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
	
	@Query(value="select * FROM  MXGATEWAY.HISTOAUTO_GATE where "
			+ "HAT_COMMANDE = (?1) "
			+ "AND HAT_NUMCMR = (?2) "
			+ "order by HAT_ID desc limit 1", nativeQuery = true)
	HistoAutoGate findByHatNumCommandeAndHatNumcmrV1(String commande, String numCmr);
	
	@Query(value="select * FROM  MXGATEWAY.HISTOAUTO_GATE where "
			+ "HAT_COMMANDE = (?1) "
			+ "AND HAT_NUMCMR = (?2) "
			+ "AND HAT_CODEREP = '00' "
			+ "order by HAT_ID desc limit 1", nativeQuery = true)
	HistoAutoGate findLastByHatNumCommandeAndHatNumcmr(String commande, String numCmr);
	
	HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth, String numCmr);
	
	HistoAutoGate findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(String commande, String numAuth, String numCmr, String codeRep);
	
	List<HistoAutoGate> findByHatNumcmr(String numCmr);
	
	@Query(value="select max(HAT_ID)  FROM  MXGATEWAY.HISTOAUTO_GATE", nativeQuery = true)
	Integer getMAX_ID();
	
	
	@Query(value = "select SUM(HAT_MONTANT) as hatMontant from MXGATEWAY.HISTOAUTO_GATE where "
			+ "HAT_NUMCMR = (?1) "
			+ "AND HAT_CODEREP = '00' "
			+ "AND HAT_DATDEM LIKE (?2)", nativeQuery = true)
	Double getCommercantGlobalFlowPerDay(String numCmr, String dateDem);
	
	@Query(value = "select * from MXGATEWAY.HISTOAUTO_GATE where "
			+ "HAT_NUMCMR = (?1) "
			+ "AND (HAT_PORTEUR = (?2) or HAT_PORTEUR like (?3) )"
			+ "AND HAT_CODEREP = '00' "
			+ "AND HAT_DATDEM LIKE (?4)", nativeQuery = true)
	List<HistoAutoGate> getPorteurMerchantFlowPerDay(String numCmr, String cardnumber, String cardnumber1, String dateDem);

}
