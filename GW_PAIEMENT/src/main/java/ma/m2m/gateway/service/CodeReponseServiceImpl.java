package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ma.m2m.gateway.dto.CodeReponseDto;
import ma.m2m.gateway.mappers.CodeReponseMapper;
import ma.m2m.gateway.repository.CodeReponseDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30 
 */

@Service
public class CodeReponseServiceImpl implements CodeReponseService {
	
	CodeReponseMapper codeReponseMapper = new CodeReponseMapper();
	
	//@Autowired
	private final CodeReponseDao codeReponseDao;

	public CodeReponseServiceImpl(CodeReponseDao codeReponseDao) {
		this.codeReponseDao = codeReponseDao;
	}

	@Override
	public CodeReponseDto findByRpcCode(String code) {
		return codeReponseMapper.model2VO(codeReponseDao.findByRpcCode(code));
	}

}
