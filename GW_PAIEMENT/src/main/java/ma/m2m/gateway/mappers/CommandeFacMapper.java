package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.CommandeFacDto;
import ma.m2m.gateway.model.CommandeFac;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-27
 */
public class CommandeFacMapper {
	
	public CommandeFacDto model2VO(CommandeFac model) {
		CommandeFacDto vo = null;
		if (model != null) {
			vo = new CommandeFacDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public CommandeFac vo2Model(CommandeFacDto vo) {
		CommandeFac model = null;
		if (vo != null) {
			model = new CommandeFac();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
