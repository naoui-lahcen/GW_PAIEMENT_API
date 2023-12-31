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
* @since   2023-12-11
 */

@Entity
@Table(name="ArticleDGI")
public class ArticleDGI implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idArticleDGI")
	private Integer idArticleDGI;
	
	@Column(name="uniqueID")
	private String uniqueID;
	
	@Column(name="name")
	private String name;
	
	@Column(name="price")
	private String price ;
	
	@Column(name="type")
	private String type;
	
	@Column(name="cF_R_COMMONE")
	private String cF_R_COMMONE;
	
	@Column(name="commande")
	private String commande;
	
	@Column(name="id_demande")
	private int iddemande;
	
	public ArticleDGI() {
		super();
	}

	public ArticleDGI(Integer idArticleDGI, String uniqueID, String name, String price, String type, String cF_R_COMMONE,
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

	public Integer getIdArticleDGI() {
		return idArticleDGI;
	}

	public void setIdArticleDGI(Integer idArticleDGI) {
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

	@Override
	public String toString() {
		return "ArticleDGI [idArticleDGI=" + idArticleDGI + ", uniqueID=" + uniqueID + ", name=" + name + ", price="
				+ price + ", type=" + type + ", cF_R_COMMONE=" + cF_R_COMMONE + ", commande=" + commande
				+ ", iddemande=" + iddemande + "]";
	}
	
}
