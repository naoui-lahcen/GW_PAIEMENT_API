package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.model.InfoCommercant;
import ma.m2m.gateway.utils.Objects;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class InfoCommercantMapper {
	
	public InfoCommercantDto model2VO(InfoCommercant model) {
		InfoCommercantDto vo = null;
		if (model != null) {
			vo = new InfoCommercantDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public InfoCommercant vo2Model(InfoCommercantDto vo) {
		InfoCommercant model = null;
		if (vo != null) {
			model = new InfoCommercant();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
