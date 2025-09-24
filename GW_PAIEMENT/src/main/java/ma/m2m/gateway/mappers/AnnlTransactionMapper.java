package ma.m2m.gateway.mappers;

import ma.m2m.gateway.dto.AnnlTransactionDto;
import ma.m2m.gateway.model.AnnlTransaction;
import ma.m2m.gateway.utils.Objects;
/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-09-22
 */
public class AnnlTransactionMapper {

    public AnnlTransactionDto model2VO(AnnlTransaction model) {
        AnnlTransactionDto vo = null;
        if (model != null) {
            vo = new AnnlTransactionDto();
            Objects.copyProperties(vo, model);
        }
        return vo;
    }

    public AnnlTransaction vo2Model(AnnlTransactionDto vo) {
        AnnlTransaction model = null;
        if (vo != null) {
            model = new AnnlTransaction();
            Objects.copyProperties(model, vo);
        }
        return model;
    }
}
