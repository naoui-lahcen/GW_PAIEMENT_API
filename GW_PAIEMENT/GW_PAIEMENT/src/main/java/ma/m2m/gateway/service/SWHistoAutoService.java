package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.SWHistoAutoDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public interface SWHistoAutoService {
	
	SWHistoAutoDto getSWHistoAuto(String cardnumber, String rrn, String amount, String date_auto,
			String merchantid);
	
	
	SWHistoAutoDto getNumCMR(String merchantid);

}
