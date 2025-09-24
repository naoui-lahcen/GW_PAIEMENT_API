package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ma.m2m.gateway.utils.Util;

import java.util.Date;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-09-22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
//@ToString
public class AnnlTransactionDto implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String numCarte;

    private String numRrn;

    private String montant;

    private String numTrs;

    private String etatAnnl;

    private String numAuto;

    private Date dateTrs;

    private Date dateTrtm;

    private String idTerm;

    private String idCMR;

    @Override
    public String toString() {
        return "AnnlTransactionDto{" +
                "dateTrs=" + dateTrs +
                ", numCarte='" + Util.formatCard(numCarte) + '\'' +
                ", numRrn='" + numRrn + '\'' +
                ", montant='" + montant + '\'' +
                ", numTrs='" + numTrs + '\'' +
                ", etatAnnl='" + etatAnnl + '\'' +
                ", numAuto='" + numAuto + '\'' +
                ", dateTrtm=" + dateTrtm +
                ", idTerm='" + idTerm + '\'' +
                ", idCMR='" + idCMR + '\'' +
                '}';
    }
}
