package ma.m2m.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */


@Entity
@Table(name="INTACQUIRING")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InternationalAcquiring implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id	
	@Column(name="iac_numcmr")
	private String numCommercant;
	
	@Column(name="iac_intacq")
	private String isIntAcquiringActive;

}
