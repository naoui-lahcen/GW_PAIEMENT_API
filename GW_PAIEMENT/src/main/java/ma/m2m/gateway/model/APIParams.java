package ma.m2m.gateway.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
 * @author  LAHCEN NAOUI
 * @version 1.0
 * @since   2025-06-13
 */

@Entity
@Table(name="api_params")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class APIParams  implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5428881130099400247L;

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="authorization")
    private String authorization;

    @Column(name="reversal")
    private String reversal;

    @Column(name="capture")
    private String capture;

    @Column(name="status")
    private String status;

    @Column(name="refund")
    private String refund;

    @Column(name="automatic_capture")
    private String automaticCapture;

    @Column(name="reccuring")
    private String reccuring;

    @Column(name="merchantid")
    private String merchantID;

    @Column(name="version")
    private String version;

    @Column(name="product")
    private String product;

}
