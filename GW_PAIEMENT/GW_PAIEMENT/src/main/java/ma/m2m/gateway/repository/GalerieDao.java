package ma.m2m.gateway.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ma.m2m.gateway.model.Galerie;

@Repository
public interface GalerieDao extends JpaRepository<Galerie,Long> {
	
	Galerie findByCodeGalAndCodeCmr(String codeGal, String codeCmr);
	
	Galerie findByCodeCmr(String codeCmr);

}
