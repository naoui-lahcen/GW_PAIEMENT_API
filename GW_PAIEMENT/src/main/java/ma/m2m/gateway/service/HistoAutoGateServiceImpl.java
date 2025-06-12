package ma.m2m.gateway.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import ma.m2m.gateway.repository.HistoAutoGateDao;
import ma.m2m.gateway.dto.HistoAutoGateDto;
import ma.m2m.gateway.mappers.HistoAutoGateMapper;
import ma.m2m.gateway.model.HistoAutoGate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class HistoAutoGateServiceImpl implements HistoAutoGateService {
	
	//@Autowired
	private final HistoAutoGateDao histoAutoGateDao;
	
	public HistoAutoGateServiceImpl(HistoAutoGateDao histoAutoGateDao) {
		this.histoAutoGateDao = histoAutoGateDao;
	}

	private HistoAutoGateMapper histoAutoGateMapper = new HistoAutoGateMapper();

	@Override
	public HistoAutoGateDto findByHatNumCommande(String commande) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommande(commande));
	}

	@Override
	public HistoAutoGateDto save(HistoAutoGateDto histoAutoGateDto) {
		HistoAutoGate histoAutoGate = histoAutoGateMapper.vo2Model(histoAutoGateDto);
		
		return histoAutoGateMapper.model2VO(histoAutoGateDao.save(histoAutoGate));
	}

	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNumcmr(String commande, String numCmr) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNumcmr(commande, numCmr));
	}
	
	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNumcmrV1(String commande, String numCmr) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNumcmrV1(commande, numCmr));
	}
	
	@Override
	public HistoAutoGateDto findLastByHatNumCommandeAndHatNumcmr(String commande, String numCmr) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findLastByHatNumCommandeAndHatNumcmr(commande, numCmr));
	}

	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmr(String commande, String numAuth,
			String numCmr) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNautemtAndHatNumcmr(commande, numAuth, numCmr));
	}
	
	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(String commande, String numAuth,
			String numCmr, String codeRep) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNautemtAndHatNumcmrAndHatCoderep(commande, numAuth, numCmr, codeRep));
	}

	@Override
	public Integer getMAX_ID() {
		return histoAutoGateDao.getMAX_ID();
	}

	@Override
	public List<HistoAutoGateDto> findAll() {
		return histoAutoGateMapper.modelList2VOList(histoAutoGateDao.findAll());
	}

	@Override
	public List<HistoAutoGateDto> findByHatNumcmr(String numCmr) {
		return histoAutoGateMapper.modelList2VOList(histoAutoGateDao.findByHatNumcmr(numCmr));
	}
	
	@Override
	public HistoAutoGateDto findByHatNumCommandeAndHatNumcmrAndHatPorteur(String commande, String numCmr, String cardnumber) {
		String cardnumber1 = cardnumber.concat("???");
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findByHatNumCommandeAndHatNumcmrAndHatPorteur(commande, numCmr, cardnumber1));
	}
	
	@Override
	public HistoAutoGateDto findById(Integer id) {
		return histoAutoGateMapper.model2VO(histoAutoGateDao.findById(id));
	}

	@Override
	public Double getCommercantGlobalFlowPerDay(String numCmr) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateSysStr = dateFormat.format(new Date());
		dateSysStr = dateSysStr.concat("%");
		return histoAutoGateDao.getCommercantGlobalFlowPerDay(numCmr,dateSysStr);
	}

	@Override
	public List<HistoAutoGateDto> getPorteurMerchantFlowPerDay(String numCmr, String cardnumber) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateSysStr = dateFormat.format(new Date());
		dateSysStr = dateSysStr.concat("%");
		String cardnumber1 = cardnumber.concat("???");
		return histoAutoGateMapper.modelList2VOList(histoAutoGateDao.getPorteurMerchantFlowPerDay(numCmr, cardnumber, cardnumber1, dateSysStr));
	}

}
