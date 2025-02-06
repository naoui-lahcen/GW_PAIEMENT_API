package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.TransactionDto;
import ma.m2m.gateway.model.Transaction;
import ma.m2m.gateway.utils.Objects;

import java.util.ArrayList;
import java.util.List;

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

	public List<TransactionDto> modelList2VOList(List<Transaction> vos) {
		TransactionDto model = null;
		List<TransactionDto> dtos = new ArrayList<>();
		if (vos != null) {
			for (Transaction vo : vos) {

				model = new TransactionDto();
				Objects.copyProperties(model, vo);
				dtos.add(model);
			}

		}
		return dtos;
	}

}
