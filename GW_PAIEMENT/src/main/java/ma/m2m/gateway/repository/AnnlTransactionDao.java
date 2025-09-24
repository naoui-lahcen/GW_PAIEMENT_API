package ma.m2m.gateway.repository;

import ma.m2m.gateway.model.AnnlTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-09-22
 */
@Repository
public interface AnnlTransactionDao extends JpaRepository<AnnlTransaction, Long> {
    AnnlTransaction findByNumRrn(String rrn);
}
