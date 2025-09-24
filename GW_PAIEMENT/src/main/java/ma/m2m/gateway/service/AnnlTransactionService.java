package ma.m2m.gateway.service;

import ma.m2m.gateway.dto.AnnlTransactionDto;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-09-22
 */
public interface AnnlTransactionService {

    AnnlTransactionDto findByNumRrn(String rrn);

    AnnlTransactionDto save(AnnlTransactionDto dto);
}
