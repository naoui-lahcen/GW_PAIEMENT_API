package ma.m2m.gateway.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Telecollecte;

@Repository
public interface TelecollecteDao extends JpaRepository<Telecollecte, Long>{
	
	
	
	//@Transactional
	@Query(value="select tlc  FROM  Telecollecte tlc WHERE  tlc.tlc_numcmr = :merchantid "
			+ " AND tlc.tlc_gest = 'N' "
			+ " AND tlc.tlc_numtlcolcte = (select max(tlc2.tlc_numtlcolcte) FROM  Telecollecte tlc2 WHERE"
			+ "  tlc.tlc_numcmr = :merchantid "
			+ " AND tlc2.tlc_gest = 'N' )", nativeQuery = true)
	Telecollecte getMAXTLC_N(String merchantid);
	 
	@Query(value="select max(tlc_numtlcolcte)  FROM  Telecollecte", nativeQuery = true)
	Integer getMAX_ID();

}
