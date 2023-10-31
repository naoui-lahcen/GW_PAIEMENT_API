package ma.m2m.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Emetteur;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */

@Repository
public interface EmetteurDao extends JpaRepository<Emetteur, String>{

	@Query(value = "select * from MXGATEWAY.EMETTEUR em where "
			+"(?1) between em.EMT_BINDEBUT and em.EMT_BINFIN ", nativeQuery = true)
	List<Emetteur> findByEmtbindebut(String binDebut);

}
