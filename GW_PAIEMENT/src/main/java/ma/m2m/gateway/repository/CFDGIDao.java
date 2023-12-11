package ma.m2m.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.CFDGI;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

@Repository
public interface CFDGIDao extends JpaRepository<CFDGI, Long> {

}
