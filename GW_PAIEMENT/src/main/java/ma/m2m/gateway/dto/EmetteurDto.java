package ma.m2m.gateway.dto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */

public class EmetteurDto {

	public EmetteurDto() {
		super();
	}


	private String emt_code;
	
	private String emt_libelle;
	
	private String emt_syspay;
	
	private String emtbindebut;
	
	private String emtbinfin;
	
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

	public String getEmtbinfin() {
		return emtbinfin;
	}

	public void setEmtbinfin(String emtbinfin) {
		this.emtbinfin = emtbinfin;
	}

	public String getEmt_codeserv() {
		return emt_codeserv;
	}

	public void setEmt_codeserv(String emt_codeserv) {
		this.emt_codeserv = emt_codeserv;
	}

	public EmetteurDto(String emt_code, String emt_libelle, String emt_syspay, String emtbindebut, String emtbinfin,
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
