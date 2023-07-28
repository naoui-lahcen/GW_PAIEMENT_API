package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.InfoAcquirerDto;
import ma.m2m.gateway.model.InfoAcquirer;

public class InfoAcquirerMapper {
	
	

	public InfoAcquirerDto model2VO(InfoAcquirer model) {
		InfoAcquirerDto vo = null;
		if (model != null) {
			vo = new InfoAcquirerDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public InfoAcquirer vo2Model(InfoAcquirerDto vo) {
		InfoAcquirer model = null;
		if (vo != null) {
			model = new InfoAcquirer();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
