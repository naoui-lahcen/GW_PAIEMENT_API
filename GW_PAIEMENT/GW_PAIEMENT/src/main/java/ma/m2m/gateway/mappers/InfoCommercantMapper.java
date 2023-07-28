package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.InfoCommercantDto;
import ma.m2m.gateway.model.InfoCommercant;

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
