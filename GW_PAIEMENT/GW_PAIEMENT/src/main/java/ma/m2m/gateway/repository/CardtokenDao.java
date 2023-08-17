package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Cardtoken;

@Repository
public interface CardtokenDao extends JpaRepository<Cardtoken, Long> {
	
	Cardtoken findByIdMerchantAndToken(String merchantid, String token);
	
	
	@Query(value="select max(id)  FROM  Cardtoken", nativeQuery = true)
	Integer getMAX_ID();
	
}