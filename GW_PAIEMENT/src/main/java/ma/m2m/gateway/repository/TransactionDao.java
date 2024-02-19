package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Telecollecte;
import ma.m2m.gateway.model.Transaction;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface TransactionDao extends JpaRepository<Transaction, Long> {
	
	Transaction findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr);
	
	@Query(value="select max(TRS_ID)  FROM  MXGATEWAY.TRANSACTION", nativeQuery = true)
	Integer getMAX_ID();

}
