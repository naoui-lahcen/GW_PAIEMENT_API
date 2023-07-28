package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.SWHistoAutoDto;
import ma.m2m.gateway.model.SWHistoAuto;

public class SWHistoAutoMapper {
	
	public SWHistoAutoDto model2VO(SWHistoAuto model) {
		SWHistoAutoDto vo = null;
		if (model != null) {
			vo = new SWHistoAutoDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public SWHistoAuto vo2Model(SWHistoAutoDto vo) {
		SWHistoAuto model = null;
		if (vo != null) {
			model = new SWHistoAuto();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
