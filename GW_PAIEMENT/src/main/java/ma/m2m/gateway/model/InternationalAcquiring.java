package ma.m2m.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */


@Entity
@Table(name="INTACQUIRING")
@Data
public class InternationalAcquiring implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id	
	@Column(name="iac_numcmr")
	private String numCommercant;
	
	@Column(name="iac_intacq")
	private String isIntAcquiringActive;

	public String getNumCommercant() {
		return numCommercant;
	}

	public void setNumCommercant(String numCommercant) {
		this.numCommercant = numCommercant;
	}

	public String getIsIntAcquiringActive() {
		return isIntAcquiringActive;
	}

	public void setIsIntAcquiringActive(String isIntAcquiringActive) {
		this.isIntAcquiringActive = isIntAcquiringActive;
	}

	public InternationalAcquiring(String numCommercant, String isIntAcquiringActive) {
		super();
		this.numCommercant = numCommercant;
		this.isIntAcquiringActive = isIntAcquiringActive;
	}

	public InternationalAcquiring() {
		super();
	}

	@Override
	public String toString() {
		return "InternationalAcquiring [numCommercant=" + numCommercant + ", isIntAcquiringActive="
				+ isIntAcquiringActive + "]";
	}
	

}
