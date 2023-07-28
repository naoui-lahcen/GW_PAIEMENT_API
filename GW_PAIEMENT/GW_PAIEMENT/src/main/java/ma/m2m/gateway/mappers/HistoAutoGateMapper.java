package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.model.HistoAutoGate;

public class HistoAutoGateMapper {

	public HistoAutoGateDto model2VO(HistoAutoGate model) {
		HistoAutoGateDto vo = null;
		if (model != null) {
			vo = new HistoAutoGateDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public HistoAutoGate vo2Model(HistoAutoGateDto vo) {
		HistoAutoGate model = null;
		if (vo != null) {
			model = new HistoAutoGate();
			Objects.copyProperties(model, vo);
		}
		return model;
	}
}
