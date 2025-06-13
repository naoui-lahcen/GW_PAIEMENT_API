package ma.m2m.gateway.repository;

import ma.m2m.gateway.model.APIParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-06-13
 */
@Repository
public interface APIParamsDao extends JpaRepository<APIParams, Long> {

    APIParams findByMerchantIDAndProductAndVersion(String merchantID, String product, String version);
}
