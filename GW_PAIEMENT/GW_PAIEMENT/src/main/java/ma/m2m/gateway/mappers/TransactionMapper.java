package ma.m2m.gateway.mappers;

import ma.m2m.gateway.Utils.Objects;
import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.model.Transaction;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class TransactionMapper {
	
	public TransactionDto model2VO(Transaction model) {
		TransactionDto vo = null;
		if (model != null) {
			vo = new TransactionDto();
			Objects.copyProperties(vo, model);
		}
		return vo;
	}

	public Transaction vo2Model(TransactionDto vo) {
		Transaction model = null;
		if (vo != null) {
			model = new Transaction();
			Objects.copyProperties(model, vo);
		}
		return model;
	}

}
