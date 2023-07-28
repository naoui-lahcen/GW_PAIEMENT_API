package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.ControlRiskCmr;

@Repository
public interface ControlRiskCmrDao  extends JpaRepository<ControlRiskCmr, String> {
	
	ControlRiskCmr findByNumCommercant(String numCmr);

}
