package ma.m2m.gateway.mappers;

import java.util.ArrayList;
import java.util.List;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.CFDGIDto;
import ma.m2m.gateway.model.CFDGI;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

public class CFDGIMapper {
	
	public CFDGIDto model2VO(CFDGI model) {
		CFDGIDto vo = null;
		if (model != null) {
			vo = new CFDGIDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public CFDGI vo2Model(CFDGIDto vo) {
		CFDGI model = null;
		if (vo != null) {
			model = new CFDGI();
			Objects.copyProperties(model, vo);
		}
		return model;
	}
	
	public List<CFDGIDto> modelList2VOList(List<CFDGI> vos) {
		CFDGIDto model = null;
		List<CFDGIDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (CFDGI vo : vos) {
		
			model = new CFDGIDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}

}
