package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.model.Telecollecte;
import ma.m2m.gateway.utils.Objects;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class TelecollecteMapper {
	
	public TelecollecteDto model2VO(Telecollecte model) {
		TelecollecteDto vo = null;
		if (model != null) {
			vo = new TelecollecteDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public Telecollecte vo2Model(TelecollecteDto vo) {
		Telecollecte model = null;
		if (vo != null) {
			model = new Telecollecte();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
