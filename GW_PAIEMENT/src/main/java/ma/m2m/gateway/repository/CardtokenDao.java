package ma.m2m.gateway.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Cardtoken;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface CardtokenDao extends JpaRepository<Cardtoken, Long> {
	
	Cardtoken findByIdMerchantAndToken(String merchantid, String token);
	
	Cardtoken findByIdMerchantAndTokenAndExprDate(String merchantid, String token, Date dateExp);
	
	
	@Query(value="select max(id)  FROM  Cardtoken", nativeQuery = true)
	Integer getMAX_ID();
	
}
