package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.mappers.TransactionMapper;
import ma.m2m.gateway.model.Transaction;
import ma.m2m.gateway.repository.TransactionDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Service
public class TransactionServiceImpl implements TransactionService {
	
	private TransactionMapper transactionMapper = new TransactionMapper();
	
	@Autowired
	TransactionDao transactionDao;

	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmr(String numAuth, String cumCmr) {
		return transactionMapper.model2VO(transactionDao.findByTrsnumautAndTrsnumcmr(numAuth, cumCmr));
	}
	
	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmrAndTrsmontant(String numAuth, String cumCmr, Double montant) {
		return transactionMapper.model2VO(transactionDao.findByTrsnumautAndTrsnumcmrAndTrsmontant(numAuth, cumCmr, montant));
	}
	
	@Override
	public TransactionDto findByTrsnumautAndTrsnumcmrAndDateTrs(String numAuth, String cumCmr, String dateTrs) {
		//dateTrs = dateTrs+"%";
		return transactionMapper.model2VO(transactionDao.findByTrsnumautAndTrsnumcmrAndDateTrs(numAuth, cumCmr, dateTrs));
	}
	
	@Override
	public TransactionDto findByTrsnumcmrAndTrscommandeAndTrsnumaut(String numCmr, String commande, String numAuth) {
		return transactionMapper.model2VO(transactionDao.findByTrsnumcmrAndTrscommandeAndTrsnumaut(numCmr, commande, numAuth));
	}

	@Override
	public TransactionDto save(TransactionDto trs) {
		Transaction tr = transactionMapper.vo2Model(trs);
		
		TransactionDto trstoSave = transactionMapper.model2VO(transactionDao.save(tr));
		return trstoSave;
	}
	
	@Override
	public Integer getMAX_ID() {
		Integer idTrs = transactionDao.getMAX_ID();
		return idTrs;
	}
}
