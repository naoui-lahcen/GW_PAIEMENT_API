package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Telecollecte;
import ma.m2m.gateway.model.Transaction;

@Repository
public interface TransactionDao extends JpaRepository<Transaction, Long> {
	
	Transaction findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr);
	
	@Query(value="select max(trs_id)  FROM  Transaction", nativeQuery = true)
	Integer getMAX_ID();

}
