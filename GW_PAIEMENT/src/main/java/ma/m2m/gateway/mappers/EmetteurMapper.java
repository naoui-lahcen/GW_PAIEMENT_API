package ma.m2m.gateway.mappers;

import java.util.ArrayList;
import java.util.List;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.model.Emetteur;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */

public class EmetteurMapper {
	

	public EmetteurDto model2VO(Emetteur model) {
		EmetteurDto vo = null;
		if (model != null) {
			vo = new EmetteurDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public Emetteur vo2Model(EmetteurDto vo) {
		Emetteur model = null;
		if (vo != null) {
			model = new Emetteur();
			Objects.copyProperties(model, vo);
		}
		return model;
	}
	
	public List<EmetteurDto> modelList2VOList(List<Emetteur> vos) {
		EmetteurDto model = null;
		List<EmetteurDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (Emetteur vo : vos) {
		
			model = new EmetteurDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}

}
