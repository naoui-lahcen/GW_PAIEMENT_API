package ma.m2m.gateway.model;

import java.io.Serializable;

public class Anomalie implements Serializable {

	private static final long serialVersionUID = -3056738017800352036L;
	private Integer id;
	private String email;
	private String date;
	private String commande;
	private String codeCmr;
	private String idGalerie;
	private String raison;
	private String codeerr;


	public Anomalie() {
		super();
	}


	public Anomalie(String email, String date, String commande, String codeCmr, String idGalerie, String raison,
			String codeerr) {
		super();
		this.email = email;
		this.date = date;
		this.commande = commande;
		this.codeCmr = codeCmr;
		this.idGalerie = idGalerie;
		this.raison = raison;
		this.codeerr = codeerr;
	}


	public String getCodeerr() {
		return codeerr;
	}

	public void setCodeerr(String codeerr) {
		this.codeerr = codeerr;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCommande() {
		return commande;
	}

	public void setCommande(String commande) {
		this.commande = commande;
	}

	public String getCodeCmr() {
		return codeCmr;
	}

	public void setCodeCmr(String codeCmr) {
		this.codeCmr = codeCmr;
	}

	public String getIdGalerie() {
		return idGalerie;
	}

	public void setIdGalerie(String idGalerie) {
		this.idGalerie = idGalerie;
	}

	public String getRaison() {
		return raison;
	}

	public void setRaison(String raison) {
		this.raison = raison;
	}
}
