package ma.m2m.gateway.model;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class DataDGI {
	private int idDataDGI;
	private String storeId; //code commerçant
	private String dateTrx;
	private String commande;
	private Double montant;
	private String checksum;
	private String xml;
	private String etat;
	private int iddemande;
	
	public DataDGI() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public int getIddemande() {
		return iddemande;
	}


	public void setIddemande(int iddemande) {
		this.iddemande = iddemande;
	}


	public int getIdDataDGI() {
		return idDataDGI;
	}


	public void setIdDataDGI(int idDataDGI) {
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
	
	
}
