package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.InfoAcquirer;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Repository
public interface InfoAcquirerDao extends JpaRepository<InfoAcquirer,Long> {
	
	InfoAcquirer findByAcqCom(String acqCom);

}
