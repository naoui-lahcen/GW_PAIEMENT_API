package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.TelecollecteDto;
import ma.m2m.gateway.model.Telecollecte;

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
