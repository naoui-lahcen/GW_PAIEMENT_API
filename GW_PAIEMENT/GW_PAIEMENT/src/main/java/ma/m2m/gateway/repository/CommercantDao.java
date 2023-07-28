package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.Commercant;

@Repository
public interface CommercantDao extends JpaRepository<Commercant, String> {
	
	Commercant findByCmrCode(String numCMR);

}
