package ma.m2m.gateway.mappers;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<CardtokenDto> modelList2VOList(List<Cardtoken> vos) {
		CardtokenDto model = null;
		List<CardtokenDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (Cardtoken vo : vos) {
		
			model = new CardtokenDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}

}
