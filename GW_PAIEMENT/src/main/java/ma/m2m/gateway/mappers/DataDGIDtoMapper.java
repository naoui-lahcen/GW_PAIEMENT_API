package ma.m2m.gateway.mappers;

import java.util.ArrayList;
import java.util.List;

import ma.m2m.gateway.dto.DataDGIDto;
import ma.m2m.gateway.model.DataDGI;
import ma.m2m.gateway.utils.Objects;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

public class DataDGIDtoMapper {
	
	public DataDGIDto model2VO(DataDGI model) {
		DataDGIDto vo = null;
		if (model != null) {
			vo = new DataDGIDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public DataDGI vo2Model(DataDGIDto vo) {
		DataDGI model = null;
		if (vo != null) {
			model = new DataDGI();
			Objects.copyProperties(model, vo);
		}
		return model;
	}
	
	public List<DataDGIDto> modelList2VOList(List<DataDGI> vos) {
		DataDGIDto model = null;
		List<DataDGIDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (DataDGI vo : vos) {
		
			model = new DataDGIDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}

}
