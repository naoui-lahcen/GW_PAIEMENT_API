package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.mappers.TransactionMapper;
import ma.m2m.gateway.model.Transaction;
import ma.m2m.gateway.repository.TransactionDao;

import java.util.List;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class TransactionServiceImpl implements TransactionService {
	
	private TransactionMapper transactionMapper = new TransactionMapper();
	
	//@Autowired
	private final TransactionDao transactionDao;

	public TransactionServiceImpl(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr) {
		return transactionMapper.model2VO(transactionDao.findByTrsNumautAndTrsNumcmr(numAuth, cumCmr));
	}
	
	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmrAndTrsmontant(String numAuth, String cumCmr, Double montant) {
		return transactionMapper.model2VO(transactionDao.findByTrsNumautAndTrsNumcmrAndTrsMontant(numAuth, cumCmr, montant));
	}
	
	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmrAndTrscommande(String numAuth, String cumCmr, String commande) {
		return transactionMapper.model2VO(transactionDao.findByTrsNumautAndTrsNumcmrAndTrsCommande(numAuth, cumCmr, commande));
	}

	@Override
	public List<TransactionDto> findListByTrsNumautAndTrsNumcmrAndTrsCommande(String numAuth, String cumCmr, String commande) {
		return transactionMapper.modelList2VOList(transactionDao.findListByTrsNumautAndTrsNumcmrAndTrsCommande(numAuth, cumCmr, commande));
	}
	
	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmrAndDateTrs(String numAuth, String cumCmr, String dateTrs) {
		//dateTrs = dateTrs+"%";
		return transactionMapper.model2VO(transactionDao.findByTrsNumautAndTrsNumcmrAndDateTrs(numAuth, cumCmr, dateTrs));
	}

	@Override
	public TransactionDto save(TransactionDto trs) {
		Transaction tr = transactionMapper.vo2Model(trs);

		return transactionMapper.model2VO(transactionDao.save(tr));
	}
	
	@Override
	public Integer getMAX_ID() {
		return transactionDao.getMAX_ID();
	}
}
