package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2023-09-22
 */

@Entity
@Table(name = "MXPOS30.ANNLTRANSACTION")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class AnnlTransaction implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="NUM_CARTE")
    private String numCarte;

    @Column(name="NUM_RRNPOS")
    private String numRrn;

    @Column(name="MONTANT")
    private String montant;

    @Column(name="NUM_TRNS")
    private String numTrs;

    @Column(name="ETAT_ANNL")
    private String etatAnnl;

    @Column(name="NUM_AUTO")
    private String numAuto;

    @Column(name="DAT_TRANS")
    private Date dateTrs;

    @Column(name="DAT_TRAIT")
    private Date dateTrtm;

    @Column(name="NUM_IDTERM")
    private String idTerm;

    @Column(name="NUM_IDCOMM")
    private String idCMR;

}