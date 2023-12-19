package ma.m2m.gateway.model;

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
public class Emetteur implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4899691466839327712L;

	public Emetteur() {
		super();
	}

	/**
	 * 
	 */
	@Id
	@Column(name="EMT_CODE")
	private String emt_code;
	@Column(name="EMT_LIBELLE")
	private String emt_libelle;
	@Column(name="EMT_SYSPAY")
	private String emt_syspay;
	@Column(name="EMT_BINDEBUT")
	private String emtbindebut;
	@Column(name="EMT_BINFIN")
	private String emtbinfin;
	@Column(name="EMT_CODESERV")
	private String emt_codeserv;

	public String getEmt_code() {
		return emt_code;
	}

	public void setEmt_code(String emt_code) {
		this.emt_code = emt_code;
	}

	public String getEmt_libelle() {
		return emt_libelle;
	}

	public void setEmt_libelle(String emt_libelle) {
		this.emt_libelle = emt_libelle;
	}

	public String getEmt_syspay() {
		return emt_syspay;
	}

	public void setEmt_syspay(String emt_syspay) {
		this.emt_syspay = emt_syspay;
	}

	public String getEmtbindebut() {
		return emtbindebut;
	}

	public void setEmtbindebut(String emtbindebut) {
		this.emtbindebut = emtbindebut;
	}

	public String getEmt_binfin() {
		return emtbinfin;
	}

	public void setEmt_binfin(String emtbinfin) {
		this.emtbinfin = emtbinfin;
	}

	public String getEmt_codeserv() {
		return emt_codeserv;
	}

	public void setEmt_codeserv(String emt_codeserv) {
		this.emt_codeserv = emt_codeserv;
	}

	public Emetteur(String emt_code, String emt_libelle, String emt_syspay, String emtbindebut, String emtbinfin,
			String emt_codeserv) {
		super();
		this.emt_code = emt_code;
		this.emt_libelle = emt_libelle;
		this.emt_syspay = emt_syspay;
		this.emtbindebut = emtbindebut;
		this.emtbinfin = emtbinfin;
		this.emt_codeserv = emt_codeserv;
	}

	@Override
	public String toString() {
		return "Emetteur [emt_code=" + emt_code + ", emt_libelle=" + emt_libelle + ", emt_syspay=" + emt_syspay
				+ ", emtbindebut=" + emtbindebut + ", emtbinfin=" + emtbinfin + ", emt_codeserv=" + emt_codeserv
				+ "]";
	}
}