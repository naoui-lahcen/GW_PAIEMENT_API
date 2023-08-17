package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.SWHistoAutoDto;

public interface SWHistoAutoService {
	
	SWHistoAutoDto getSWHistoAuto(String cardnumber, String rrn, String amount, String date_auto,
			String merchantid);
	
	
	SWHistoAutoDto getNumCMR(String merchantid);

}
