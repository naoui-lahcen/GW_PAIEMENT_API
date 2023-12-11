package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.m2m.gateway.model.CommandeFac;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
@Repository
public interface CommandeFacDao extends JpaRepository<CommandeFac, Long> {

}
