package ma.m2m.gateway.dto;
/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-21 
 */

public class InternationalAcquiringDto {
	
	
	private String numCommercant;
	
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

	public InternationalAcquiringDto() {
		super();
	}

	public InternationalAcquiringDto(String numCommercant, String isIntAcquiringActive) {
		super();
		this.numCommercant = numCommercant;
		this.isIntAcquiringActive = isIntAcquiringActive;
	}

	@Override
	public String toString() {
		return "InternationalAcquiringDto [numCommercant=" + numCommercant + ", isIntAcquiringActive="
				+ isIntAcquiringActive + "]";
	}
	
	

}
