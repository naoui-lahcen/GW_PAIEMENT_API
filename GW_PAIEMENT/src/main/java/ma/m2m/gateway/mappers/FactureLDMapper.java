package ma.m2m.gateway.mappers;

import java.util.ArrayList;
import java.util.List;

import ma.m2m.gateway.dto.FactureLDDto;
import ma.m2m.gateway.model.FactureLD;
import ma.m2m.gateway.utils.Objects;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
public class FactureLDMapper {
	
	public FactureLDDto model2VO(FactureLD model) {
		FactureLDDto vo = null;
		if (model != null) {
			vo = new FactureLDDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public FactureLD vo2Model(FactureLDDto vo) {
		FactureLD model = null;
		if (vo != null) {
			model = new FactureLD();
			Objects.copyProperties(model, vo);
		}
		return model;
	}
	
	public List<FactureLDDto> modelList2VOList(List<FactureLD> vos) {
		FactureLDDto model = null;
		List<FactureLDDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (FactureLD vo : vos) {
		
			model = new FactureLDDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}

}
