package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Telecollecte;
import ma.m2m.gateway.model.Transaction;

import java.util.List;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface TransactionDao extends JpaRepository<Transaction, Long> {
	
	Transaction findByTrsNumautAndTrsNumcmr(String numAuth, String cumCmr);
	
	Transaction findByTrsNumautAndTrsNumcmrAndTrsMontant(String numAuth, String cumCmr, Double montant);
	
	Transaction findByTrsNumautAndTrsNumcmrAndTrsCommande(String numAuth, String cumCmr, String commande);

	List<Transaction> findListByTrsNumautAndTrsNumcmrAndTrsCommande(String numAuth, String cumCmr, String commande);
	
	@Query(value = "SELECT * FROM MXGATEWAY.TRANSACTION WHERE "
            + "TRS_NUMCMR = :numCmr "
            + "AND TRS_NUMAUT = :numAuth "
            + "AND DATE(TRS_DATTRANS) = :dateTrs", nativeQuery = true)
    Transaction findByTrsNumautAndTrsNumcmrAndDateTrs(@Param("numAuth") String numAuth,
                                                      @Param("numCmr") String cumCmr, 
                                                      @Param("dateTrs") String dateTrs);
	
	@Query(value="select max(TRS_ID)  FROM  MXGATEWAY.TRANSACTION", nativeQuery = true)
	Integer getMAX_ID();

}
