package ma.m2m.gateway.mappers;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<HistoAutoGateDto> modelList2VOList(List<HistoAutoGate> vos) {
		HistoAutoGateDto model = null;
		List<HistoAutoGateDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (HistoAutoGate vo : vos) {
		
			model = new HistoAutoGateDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}
}
