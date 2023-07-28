package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.ControlRiskCmrDto;
import ma.m2m.gateway.model.ControlRiskCmr;

public class ControlRiskCmrMapper {
	
	
	public ControlRiskCmrDto model2VO(ControlRiskCmr model) {
		ControlRiskCmrDto vo = null;
		if (model != null) {
			vo = new ControlRiskCmrDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public ControlRiskCmr vo2Model(ControlRiskCmrDto vo) {
		ControlRiskCmr model = null;
		if (vo != null) {
			model = new ControlRiskCmr();
			Objects.copyProperties(model, vo);
		}
		return model;
	}


}
