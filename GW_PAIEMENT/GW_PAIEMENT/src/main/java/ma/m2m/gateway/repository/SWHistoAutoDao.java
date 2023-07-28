package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.SWHistoAuto;

@Repository
public interface SWHistoAutoDao extends JpaRepository<SWHistoAuto, Long>{
	
	
	@Query(value="select * from MXSWITCH.SWHistoAuto:  WHERE trim(HAT_PORTEUR) = ':cardnumber:' "
			+ "AND trim(HAT_NREFCE) = ':rrn:' AND   HAT_MONTANT  = :amount: "
			+ "AND HAT_DATDEM = STR_TO_DATE(':date_auto:','%d%m%y')  "
			+ "AND trim(HAT_NUMCMR) = ':merchantid:'", nativeQuery = true)
	SWHistoAuto getSWHistoAuto(String cardnumber, String rrn, String amount, String date_auto,
			String merchantid);

}
