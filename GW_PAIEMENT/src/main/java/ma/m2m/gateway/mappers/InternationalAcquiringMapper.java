package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.InternationalAcquiringDto;
import ma.m2m.gateway.model.InternationalAcquiring;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21 
 */

public class InternationalAcquiringMapper {
	
	public InternationalAcquiringDto model2VO(InternationalAcquiring model) {
		InternationalAcquiringDto vo = null;
		if (model != null) {
			vo = new InternationalAcquiringDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public InternationalAcquiring vo2Model(InternationalAcquiringDto vo) {
		InternationalAcquiring model = null;
		if (vo != null) {
			model = new InternationalAcquiring();
			Objects.copyProperties(model, vo);
		}
		return model;
	}


}
