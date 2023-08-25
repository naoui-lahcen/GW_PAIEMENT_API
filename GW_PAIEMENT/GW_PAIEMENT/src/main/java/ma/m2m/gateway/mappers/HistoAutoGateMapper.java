package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.model.HistoAutoGate;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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
