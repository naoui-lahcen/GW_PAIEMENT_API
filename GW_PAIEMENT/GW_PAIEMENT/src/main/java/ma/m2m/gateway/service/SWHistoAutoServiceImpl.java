package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.SWHistoAutoDto;
import ma.m2m.gateway.mappers.SWHistoAutoMapper;
import ma.m2m.gateway.repository.SWHistoAutoDao;

@Service
public class SWHistoAutoServiceImpl implements SWHistoAutoService {
	
	SWHistoAutoMapper swHistoAutoMapper = new SWHistoAutoMapper();
	
	@Autowired
	SWHistoAutoDao swHistoAutoDao;

	@Override
	public SWHistoAutoDto getSWHistoAuto(String cardnumber, String rrn, String amount, String date_auto,
			String merchantid) {
		// TODO Auto-generated method stub
		return null;
	}

}
