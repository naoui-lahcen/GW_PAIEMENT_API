package ma.m2m.gateway.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.EmetteurDto;
import ma.m2m.gateway.mappers.EmetteurMapper;
import ma.m2m.gateway.repository.EmetteurDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */

@Service
public class EmetteurServiceImpl implements EmetteurService {
	
	EmetteurMapper emetteurMapper = new EmetteurMapper();
	
	//@Autowired
	private final EmetteurDao emetteurDao;
	
	public EmetteurServiceImpl(EmetteurDao emetteurDao) {
		this.emetteurDao = emetteurDao;
	}

	@Override
	public List<EmetteurDto>  findByBindebut(String binDebut) {
		return emetteurMapper.modelList2VOList(emetteurDao.findByEmtbindebut(binDebut));
	}

}
