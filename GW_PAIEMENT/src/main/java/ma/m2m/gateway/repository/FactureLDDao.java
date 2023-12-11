package ma.m2m.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.FactureLD;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
@Repository
public interface FactureLDDao extends JpaRepository<FactureLD, Long> {
	
	List<FactureLD> findFactureByIddemande(Integer iddemande);

}
