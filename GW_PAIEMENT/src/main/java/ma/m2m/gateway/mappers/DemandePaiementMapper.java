package ma.m2m.gateway.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.DemandePaiementDto;
import ma.m2m.gateway.model.DemandePaiement;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class DemandePaiementMapper {

	public DemandePaiementDto model2VO(DemandePaiement model) {
		DemandePaiementDto vo = null;
		if (model != null) {
			vo = new DemandePaiementDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public DemandePaiement vo2Model(DemandePaiementDto vo) {
		DemandePaiement model = null;
		if (vo != null) {
			model = new DemandePaiement();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

	
	public List<DemandePaiementDto> modelList2VOList(List<DemandePaiement> vos) {
		DemandePaiementDto model = null;
		List<DemandePaiementDto> dtos = null;
		if (vos != null) {
			for (DemandePaiement vo : vos) {
		
			model = new DemandePaiementDto();
			Objects.copyProperties(model, vo);
			dtos.add(model);
			}

		}
		return dtos;
	}

}
