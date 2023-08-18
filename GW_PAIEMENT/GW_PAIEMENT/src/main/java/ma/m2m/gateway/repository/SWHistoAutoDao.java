package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.SWHistoAuto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface SWHistoAutoDao extends JpaRepository<SWHistoAuto, Long>{
	
	
	@Query(value="select * FROM MXSWITCH.HISTOAUTO hist WHERE "
			+ "trim(hist.hat_porteur) = :cardnumber "
			+ "AND trim(hist.HAT_NREFCE) = :rrn "
			+ "AND   hist.HAT_MONTANT  = :amount "
			+ "AND hist.HAT_DATDEM = STR_TO_DATE(':date_auto:','%d%m%y')  "
			+ "AND trim(hist.HAT_NUMCMR) = :merchantid", nativeQuery = true)
	SWHistoAuto getSWHistoAuto(String cardnumber, String rrn, String amount, String date_auto,
			String merchantid);
	

	@Query(value="select * FROM MXSWITCH.HISTOAUTO hist WHERE "
			+ "hist.HAT_NUMCMR= :merchantid", nativeQuery = true)
	SWHistoAuto getNumCMR(String merchantid);

}
