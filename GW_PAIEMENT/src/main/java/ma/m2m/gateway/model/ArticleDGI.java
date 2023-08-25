package ma.m2m.gateway.model;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class ArticleDGI implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int idArticleDGI;
	private String uniqueID;
	private String name;
	private String price ;
	private String type;
	private String cF_R_COMMONE;
	private String commande;
	private int iddemande;
	
	public ArticleDGI() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ArticleDGI(int idArticleDGI, String uniqueID, String name, String price, String type, String cF_R_COMMONE,
			String commande, int iddemande) {
		super();
		this.idArticleDGI = idArticleDGI;
		this.uniqueID = uniqueID;
		this.name = name;
		this.price = price;
		this.type = type;
		this.cF_R_COMMONE = cF_R_COMMONE;
		this.commande = commande;
		this.iddemande=iddemande;
	}

	public int getIdArticleDGI() {
		return idArticleDGI;
	}

	public void setIdArticleDGI(int idArticleDGI) {
		this.idArticleDGI = idArticleDGI;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getcF_R_COMMONE() {
		return cF_R_COMMONE;
	}

	public void setcF_R_COMMONE(String cF_R_COMMONE) {
		this.cF_R_COMMONE = cF_R_COMMONE;
	}

	public String getCommande() {
		return commande;
	}

	public void setCommande(String commande) {
		this.commande = commande;
	}

	public int getIddemande() {
		return iddemande;
	}

	public void setIddemande(int iddemande) {
		this.iddemande = iddemande;
	}
	
	
	
}
