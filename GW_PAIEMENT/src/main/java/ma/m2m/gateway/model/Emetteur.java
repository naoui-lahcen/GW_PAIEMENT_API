package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */

@Entity
@Table(name="EMETTEUR")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Emetteur implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4899691466839327712L;

	/**
	 * 
	 */
	@Id
	@Column(name="EMT_CODE")
	private String emtCode;
	@Column(name="EMT_LIBELLE")
	private String emtLibelle;
	@Column(name="EMT_SYSPAY")
	private String emtSyspay;
	@Column(name="EMT_BINDEBUT")
	private String emtbindebut;
	@Column(name="EMT_BINFIN")
	private String emtbinfin;
	@Column(name="EMT_CODESERV")
	private String emtCodeserv;

}