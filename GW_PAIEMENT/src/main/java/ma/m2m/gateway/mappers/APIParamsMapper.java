package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.APIParamsDto;
import ma.m2m.gateway.model.APIParams;
import ma.m2m.gateway.utils.Objects;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-06-13
 */
public class APIParamsMapper {

    public APIParamsDto model2VO(APIParams model) {
        APIParamsDto vo = null;
        if (model != null) {
            vo = new APIParamsDto();
            Objects.copyProperties(vo, model);
        }
        return vo;
    }

    public APIParams vo2Model(APIParamsDto vo) {
        APIParams model = null;
        if (vo != null) {
            model = new APIParams();
            Objects.copyProperties(model, vo);
        }
        return model;
    }
}
