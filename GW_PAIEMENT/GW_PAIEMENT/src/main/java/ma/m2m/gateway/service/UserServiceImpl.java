package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.UserDto;
import ma.m2m.gateway.mappers.UserMapper;
import ma.m2m.gateway.model.User;

@Service
public class UserServiceImpl implements UserService{

	private UserMapper userMapper = new UserMapper();


	@Override
	public User testMapper(UserDto userDto) {
		
	      User user = userMapper.vo2Model(userDto);

		return user;
	}
	

}
