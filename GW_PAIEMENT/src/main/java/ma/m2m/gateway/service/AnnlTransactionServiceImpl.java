package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.AnnlTransactionDto;
import ma.m2m.gateway.mappers.AnnlTransactionMapper;
import ma.m2m.gateway.model.AnnlTransaction;
import ma.m2m.gateway.repository.AnnlTransactionDao;
import org.springframework.stereotype.Service;
/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-09-22
 */

@Service
public class AnnlTransactionServiceImpl implements AnnlTransactionService {

    private final AnnlTransactionDao annlTransactionDao;

    public AnnlTransactionServiceImpl(AnnlTransactionDao annlTransactionDao) {
        this.annlTransactionDao = annlTransactionDao;
    }

    private AnnlTransactionMapper annlTransactionMapper = new AnnlTransactionMapper();
    @Override
    public AnnlTransactionDto findByNumRrn(String rrn) {
        return annlTransactionMapper.model2VO(annlTransactionDao.findByNumRrn(rrn));
    }

    @Override
    public AnnlTransactionDto save(AnnlTransactionDto dto) {
        AnnlTransaction annlTransaction = annlTransactionMapper.vo2Model(dto);
        return annlTransactionMapper.model2VO(annlTransactionDao.save(annlTransaction));
    }
}
