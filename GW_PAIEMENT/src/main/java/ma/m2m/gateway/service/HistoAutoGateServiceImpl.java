package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ma.m2m.gateway.repository.HistoAutoGateDao;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.mappers.HistoAutoGateMapper;
import ma.m2m.gateway.model.HistoAutoGate;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class HistoAutoGateServiceImpl implements HistoAutoGateService {
	
	@Autowired
	HistoAutoGateDao histoAutoGateDao;
	
	private HistoAutoGateMapper histoAutoGateMapper = new HistoAutoGateMapper();

	@Override
	public HistoAutoGateDto findByHatNumCommande(String commande) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommande(commande));
	}

	@Override
	public HistoAutoGateDto save(HistoAutoGateDto histoAutoGateDto) {
		HistoAutoGate histoAutoGate = histoAutoGateMapper.vo2Model(histoAutoGateDto);
		
		HistoAutoGateDto histoAutoGateSaved = histoAutoGateMapper.model2VO(histoAutoGateDao.save(histoAutoGate));
		
		return histoAutoGateSaved;
	}

	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNumcmr(String commande, String numCmr) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNumcmr(commande, numCmr));
	}

	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth,
			String numCmr) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNautemtAndHatNumcmr(commande, numAuth, numCmr));
	}

	@Override
	public Integer getMAX_ID() {
		Integer idHisG = histoAutoGateDao.getMAX_ID();
		return idHisG;
	}

}
