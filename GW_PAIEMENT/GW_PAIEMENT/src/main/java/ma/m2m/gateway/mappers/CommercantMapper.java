package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.model.Commercant;

public class CommercantMapper {
	
	public CommercantDto model2VO(Commercant model) {
		CommercantDto vo = null;
		if (model != null) {
			vo = new CommercantDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public Commercant vo2Model(CommercantDto vo) {
		Commercant model = null;
		if (vo != null) {
			model = new Commercant();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
