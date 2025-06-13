package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.APIParamsDto;
import ma.m2m.gateway.mappers.APIParamsMapper;
import ma.m2m.gateway.repository.APIParamsDao;
import org.springframework.stereotype.Service;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-06-13
 */

@Service
public class APIParamsServiceImpl implements APIParamsService {

    private final APIParamsDao apiParamsDao;

    public APIParamsServiceImpl(APIParamsDao apiParamsDao) {
        this.apiParamsDao = apiParamsDao;
    }

    private APIParamsMapper apiParamsMapper = new APIParamsMapper();
    @Override
    public APIParamsDto findByMerchantIDAndProductAndVersion(String merchantID, String product, String version) {
        return apiParamsMapper.model2VO(apiParamsDao.findByMerchantIDAndProductAndVersion(merchantID, product, version));
    }
}
