package ma.m2m.gateway.dto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class CodeReponse implements java.io.Serializable {

	private long rpcId;
	private String rpcCode;
	private String rpcLibelle;
	private String rpcSyspay;
	private Character rpcLangue;

	public CodeReponse() {
	}

	public CodeReponse(long rpcId) {
		this.rpcId = rpcId;
	}

	public CodeReponse(long rpcId, String rpcCode, String rpcLibelle,
			String rpcSyspay, Character rpcLangue) {
		this.rpcId = rpcId;
		this.rpcCode = rpcCode;
		this.rpcLibelle = rpcLibelle;
		this.rpcSyspay = rpcSyspay;
		this.rpcLangue = rpcLangue;
	}

	public long getRpcId() {
		return this.rpcId;
	}

	public void setRpcId(long rpcId) {
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

	public Character getRpcLangue() {
		return this.rpcLangue;
	}

	public void setRpcLangue(Character rpcLangue) {
		this.rpcLangue = rpcLangue;
	}

}
