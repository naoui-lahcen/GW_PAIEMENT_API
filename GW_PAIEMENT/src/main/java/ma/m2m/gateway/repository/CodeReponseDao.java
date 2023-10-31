package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.CodeReponse;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30 
 */

@Repository
public interface CodeReponseDao extends JpaRepository<CodeReponse, Long> {
	
	// CodeReponse findByRpcCode(String code);
	
	@Query(value = "select * from MXGATEWAY.CODEREPONSE cr where "
			+"cr.RPC_CODE  = (?1) limit 1", nativeQuery = true)
	CodeReponse findByRpcCode(String binDebut);

}
