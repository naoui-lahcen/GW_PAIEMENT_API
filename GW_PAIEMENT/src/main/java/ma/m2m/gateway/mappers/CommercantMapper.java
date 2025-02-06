package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.CommercantDto;
import ma.m2m.gateway.model.Commercant;
import ma.m2m.gateway.utils.Objects;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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
