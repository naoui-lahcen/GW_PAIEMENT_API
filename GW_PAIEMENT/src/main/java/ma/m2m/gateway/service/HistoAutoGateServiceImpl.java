package ma.m2m.gateway.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Override
	public List<HistoAutoGateDto> findAll() {
		return histoAutoGateMapper.modelList2VOList(histoAutoGateDao.findAll());
	}

	@Override
	public List<HistoAutoGateDto> findByHatNumcmr(String numCmr) {
		return histoAutoGateMapper.modelList2VOList(histoAutoGateDao.findByHatNumcmr(numCmr));
	}

	@Override
	public Double getCommercantGlobalFlowPerDay(String numCmr) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateSysStr = dateFormat.format(new Date());
		System.out.println("getCommercantGlobalFlowPerDay dateSysStr : " + dateSysStr);
		dateSysStr = dateSysStr.concat("%");
		Double montant = histoAutoGateDao.getCommercantGlobalFlowPerDay(numCmr,dateSysStr);
		return montant;
	}

	@Override
	public List<HistoAutoGateDto> getPorteurMerchantFlowPerDay(String numCmr, String cardnumber) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateSysStr = dateFormat.format(new Date());
		System.out.println("getPorteurMerchantFlowPerDay dateSysStr : " + dateSysStr);
		dateSysStr = dateSysStr.concat("%");
		String cardnumber1 = cardnumber.concat("???");
		return histoAutoGateMapper.modelList2VOList(histoAutoGateDao.getPorteurMerchantFlowPerDay(numCmr, cardnumber, cardnumber1, dateSysStr));
	}

}
