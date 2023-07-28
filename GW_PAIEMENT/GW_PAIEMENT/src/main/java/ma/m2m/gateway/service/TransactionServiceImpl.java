package ma.m2m.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.mappers.TransactionMapper;
import ma.m2m.gateway.model.Transaction;
import ma.m2m.gateway.repository.TransactionDao;

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
