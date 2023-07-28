package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.model.Cardtoken;

public class CardtokenMapper {
	
	public CardtokenDto model2VO(Cardtoken model) {
		CardtokenDto vo = null;
		if (model != null) {
			vo = new CardtokenDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public Cardtoken vo2Model(CardtokenDto vo) {
		Cardtoken model = null;
		if (vo != null) {
			model = new Cardtoken();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
