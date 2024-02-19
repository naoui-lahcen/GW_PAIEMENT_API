package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.Telecollecte;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface TelecollecteDao extends JpaRepository<Telecollecte, Long>{
	
	
	
	//@Transactional
	@Query(value="select *  FROM  MXGATEWAY.TELECOLLECTE tlc WHERE  tlc.TLC_NUMCMR = :merchantid "
			+ " AND tlc.TLC_GEST = 'N' "
			+ " AND tlc.TLC_NUMTLCOLCTE = (select max(tlc2.TLC_NUMTLCOLCTE) FROM  MXGATEWAY.TELECOLLECTE tlc2 WHERE"
			+ "  tlc.TLC_NUMCMR = :merchantid "
			+ " AND tlc2.TLC_GEST = 'N' )", nativeQuery = true)
	Telecollecte getMAXTLC_N(String merchantid);
	 
	@Query(value="select max(TLC_NUMTLCOLCTE)  FROM  MXGATEWAY.TELECOLLECTE", nativeQuery = true)
	Integer getMAX_ID();

}
