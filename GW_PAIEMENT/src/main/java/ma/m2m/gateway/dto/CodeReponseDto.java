package ma.m2m.gateway.dto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30
 */

public class CodeReponseDto implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer rpcId;
	private String rpcCode;
	private String rpcLibelle;
	private String rpcSyspay;
	private String rpcLangue;

	public CodeReponseDto(Integer rpcId) {
		this.rpcId = rpcId;
	}


	public long getRpcId() {
		return this.rpcId;
	}

	public void setRpcId(Integer rpcId) {
		this.rpcId = rpcId;
	}

	public String getRpcCode() {
		return this.rpcCode;
	}

	public void setRpcCode(String rpcCode) {
		this.rpcCode = rpcCode;
	}

	public String getRpcLibelle() {
		return this.rpcLibelle;
	}

	public void setRpcLibelle(String rpcLibelle) {
		this.rpcLibelle = rpcLibelle;
	}

	public String getRpcSyspay() {
		return this.rpcSyspay;
	}

	public void setRpcSyspay(String rpcSyspay) {
		this.rpcSyspay = rpcSyspay;
	}

	public String getRpcLangue() {
		return this.rpcLangue;
	}

	public void setRpcLangue(String rpcLangue) {
		this.rpcLangue = rpcLangue;
	}
	
	public CodeReponseDto(Integer rpcId, String rpcCode, String rpcLibelle,
			String rpcSyspay, String rpcLangue) {
		this.rpcId = rpcId;
		this.rpcCode = rpcCode;
		this.rpcLibelle = rpcLibelle;
		this.rpcSyspay = rpcSyspay;
		this.rpcLangue = rpcLangue;
	}

	public CodeReponseDto() {
		super();
	}

	@Override
	public String toString() {
		return "CodeReponseDto [rpcId=" + rpcId + ", rpcCode=" + rpcCode + ", rpcLibelle=" + rpcLibelle + ", rpcSyspay="
				+ rpcSyspay + ", rpcLangue=" + rpcLangue + "]";
	}

}
