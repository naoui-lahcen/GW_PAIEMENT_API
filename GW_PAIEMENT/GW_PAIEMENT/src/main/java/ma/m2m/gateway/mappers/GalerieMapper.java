package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.GalerieDto;
import ma.m2m.gateway.model.Galerie;

public class GalerieMapper {
	

	public GalerieDto model2VO(Galerie model) {
		GalerieDto vo = null;
		if (model != null) {
			vo = new GalerieDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public Galerie vo2Model(GalerieDto vo) {
		Galerie model = null;
		if (vo != null) {
			model = new Galerie();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
