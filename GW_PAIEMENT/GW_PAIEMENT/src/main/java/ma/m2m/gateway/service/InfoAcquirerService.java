package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.InfoAcquirerDto;

public interface InfoAcquirerService {
	
	InfoAcquirerDto findByAcqCom(String acqCom);

}
