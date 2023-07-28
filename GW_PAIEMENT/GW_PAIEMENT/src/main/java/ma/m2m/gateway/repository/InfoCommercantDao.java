package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.InfoCommercant;

@Repository
public interface InfoCommercantDao extends JpaRepository<InfoCommercant,Long> {
	
	InfoCommercant findByCmrCode(String numCMR);

}
