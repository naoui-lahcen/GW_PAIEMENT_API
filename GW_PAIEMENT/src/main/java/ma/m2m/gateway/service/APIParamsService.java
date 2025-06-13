package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.APIParamsDto;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-06-13
 */
public interface APIParamsService {

    APIParamsDto findByMerchantIDAndProductAndVersion(String merchantID, String product, String version);
}
