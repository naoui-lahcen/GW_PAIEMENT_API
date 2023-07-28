package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.UserDto;
import ma.m2m.gateway.model.User;

public class UserMapper {

	public UserDto model2VO(User model) {
		UserDto vo = null;
		if (model != null) {
			vo = new UserDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public User vo2Model(UserDto vo) {
		User model = null;
		if (vo != null) {
			model = new User();
			Objects.copyProperties(model, vo);
		}
		return model;
	}
}
