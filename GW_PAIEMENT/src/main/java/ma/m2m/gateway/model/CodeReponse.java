package ma.m2m.gateway.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-30 
 */

@Entity
@Table(name="CODEREPONSE")
public class CodeReponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="RPC_ID")
	private Integer rpcId;
	
	@Column(name="RPC_CODE")
	private String rpcCode;
	
	@Column(name="RPC_LIBELLE")
	private String rpcLibelle;
	
	@Column(name="RPC_SYSPAY")
	private String rpcSyspay;
	
	@Column(name="RPC_LANGUE")
	private String rpcLangue;

	public Integer getRpcId() {
		return rpcId;
	}

	public void setRpcId(Integer rpcId) {
		this.rpcId = rpcId;
	}

	public String getRpcCode() {
		return rpcCode;
	}

	public void setRpcCode(String rpcCode) {
		this.rpcCode = rpcCode;
	}

	public String getRpcLibelle() {
		return rpcLibelle;
	}

	public void setRpcLibelle(String rpcLibelle) {
		this.rpcLibelle = rpcLibelle;
	}

	public String getRpcSyspay() {
		return rpcSyspay;
	}

	public void setRpcSyspay(String rpcSyspay) {
		this.rpcSyspay = rpcSyspay;
	}

	public String getRpcLangue() {
		return rpcLangue;
	}

	public void setRpcLangue(String rpcLangue) {
		this.rpcLangue = rpcLangue;
	}

	public CodeReponse(Integer rpcId, String rpcCode, String rpcLibelle, String rpcSyspay, String rpcLangue) {
		super();
		this.rpcId = rpcId;
		this.rpcCode = rpcCode;
		this.rpcLibelle = rpcLibelle;
		this.rpcSyspay = rpcSyspay;
		this.rpcLangue = rpcLangue;
	}

	public CodeReponse() {
		super();
	}

	@Override
	public String toString() {
		return "CodeReponse [rpcId=" + rpcId + ", rpcCode=" + rpcCode + ", rpcLibelle=" + rpcLibelle + ", rpcSyspay="
				+ rpcSyspay + ", rpcLangue=" + rpcLangue + "]";
	}
	

}
