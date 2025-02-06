package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.model.CodeReponse;
import ma.m2m.gateway.utils.Objects;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30 
 */

public class CodeReponseMapper {
	
	public CodeReponseDto model2VO(CodeReponse model) {
		CodeReponseDto vo = null;
		if (model != null) {
			vo = new CodeReponseDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public CodeReponse vo2Model(CodeReponseDto vo) {
		CodeReponse model = null;
		if (vo != null) {
			model = new CodeReponse();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
