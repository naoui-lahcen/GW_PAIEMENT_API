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
@Table(name="DataDGI")
public class DataDGI implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idDataDGI")
	private Integer idDataDGI;
	
	@Column(name="storeId")
	private String storeId; //code commerçant
	
	@Column(name="dateTrx")
	private String dateTrx;
	
	@Column(name="commande")
	private String commande;
	
	@Column(name="MONTANT")
	private Double montant;
	
	@Column(name="checksum")
	private String checksum;
	
	@Column(name="xml")
	private String xml;
	
	@Column(name="etat")
	private String etat;
	
	@Column(name="id_demande")
	private int iddemande;
	
	public DataDGI() {
		super();
	}
	

	public int getIddemande() {
		return iddemande;
	}


	public void setIddemande(int iddemande) {
		this.iddemande = iddemande;
	}


	public Integer getIdDataDGI() {
		return idDataDGI;
	}


	public void setIdDataDGI(Integer idDataDGI) {
		this.idDataDGI = idDataDGI;
	}


	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getDateTrx() {
		return dateTrx;
	}

	public void setDateTrx(String dateTrx) {
		this.dateTrx = dateTrx;
	}

	public String getCommande() {
		return commande;
	}

	public void setCommande(String commande) {
		this.commande = commande;
	}

	public Double getMontant() {
		return montant;
	}

	public void setMontant(Double montant) {
		this.montant = montant;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getEtat() {
		return etat;
	}

	public void setEtat(String etat) {
		this.etat = etat;
	}


	public DataDGI(String storeId, String dateTrx, String commande, Double montant, String checksum, String xml,
			String etat, int iddemande) {
		super();
		this.storeId = storeId;
		this.dateTrx = dateTrx;
		this.commande = commande;
		this.montant = montant;
		this.checksum = checksum;
		this.xml = xml;
		this.etat = etat;
		this.iddemande = iddemande;
	}


	@Override
	public String toString() {
		return "DataDGI [idDataDGI=" + idDataDGI + ", storeId=" + storeId + ", dateTrx=" + dateTrx + ", commande="
				+ commande + ", montant=" + montant + ", checksum=" + checksum + ", xml=" + xml + ", etat=" + etat
				+ ", iddemande=" + iddemande + "]";
	}
	
}
