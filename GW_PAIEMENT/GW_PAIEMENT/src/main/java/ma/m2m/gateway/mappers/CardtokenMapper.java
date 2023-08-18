package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.CardtokenDto;
import ma.m2m.gateway.model.Cardtoken;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

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
