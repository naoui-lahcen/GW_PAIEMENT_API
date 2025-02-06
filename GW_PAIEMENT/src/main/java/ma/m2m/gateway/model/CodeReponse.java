package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

}
