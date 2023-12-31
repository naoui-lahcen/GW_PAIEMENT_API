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
* @since   2023-07-01 / 2023-09-01 
 */

@Entity
@Table(name="FactureLD")
public class FactureLD implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;
	
	@Column(name="numCommande")
	private String numCommande;
	
	@Column(name="numfacture")
	private String  numfacture;
	
	@Column(name="numcontrat")
	private String numcontrat;
	
	@Column(name="numligne")
	private String numligne;
	
	@Column(name="name")
	private String name;
	
	@Column(name="classF")
	private String classF;
	
	@Column(name="montantTbr")
	private Double montantTbr;
	
	@Column(name="montantTtc")
	private Double montantTtc;
	
	@Column(name="montantTotal")
	private Double montantTotal;
	
	@Column(name="numPolice")
	private String numPolice;
	
	@Column(name="montantTva")
	private Double montantTva;
	
	@Column(name="produit")
	private String produit;
	
	@Column(name="type")
	private String type;
	
	@Column(name="date")
	private String date;
	
	@Column(name="fourniture")
	private String fourniture;
	
	@Column(name="montantSTbr")
	private Double montantSTbr;
	
	@Column(name="id_demande")
	private int iddemande;
	
	/** reconciliation ecom lydec*/
	@Column(name="etat")
	private String etat;
	
	@Column(name="numrecnaps")
	private String numrecnaps;
	
	@Column(name="datepai")
	private String datepai;
	
	@Column(name="trxFactureLydec")
	private String trxFactureLydec;
	
	public FactureLD() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FactureLD(int iddemande, String numCommande, String numfacture, String numcontrat, String numligne, String name,
			String classF, Double montantTbr, Double montantTtc, Double montantTotal, String numPolice,
			Double montantTva, String produit, String type, String date, String fourniture, Double montantSTbr
			,String etat, String numrecnaps, String datepai, String trxFactureLydec) {
		super();
		this.numCommande = numCommande;
		this.numfacture = numfacture;
		this.numcontrat = numcontrat;
		this.numligne = numligne;
		this.name = name;
		this.classF = classF;
		this.montantTbr = montantTbr;
		this.montantTtc = montantTtc;
		this.montantTotal = montantTotal;
		this.numPolice = numPolice;
		this.montantTva = montantTva;
		this.produit = produit;
		this.type = type;
		this.date = date;
		this.fourniture=fourniture;
		this.montantSTbr = montantSTbr;
		this.iddemande=iddemande;
		this.etat=etat;
		this.numrecnaps=numrecnaps;
		this.datepai=datepai;
		this.trxFactureLydec=trxFactureLydec;  
	}

	
	public String getFourniture() {
		return fourniture;
	}

	public void setFourniture(String fourniture) {
		this.fourniture = fourniture;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumCommande() {
		return numCommande;
	}

	public void setNumCommande(String numCommande) {
		this.numCommande = numCommande;
	}

	public String getNumfacture() {
		return numfacture;
	}

	public void setNumfacture(String numfacture) {
		this.numfacture = numfacture;
	}

	public String getNumcontrat() {
		return numcontrat;
	}

	public void setNumcontrat(String numcontrat) {
		this.numcontrat = numcontrat;
	}

	public String getNumligne() {
		return numligne;
	}

	public void setNumligne(String numligne) {
		this.numligne = numligne;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassF() {
		return classF;
	}

	public void setClassF(String classF) {
		this.classF = classF;
	}

	public Double getMontantTbr() {
		return montantTbr;
	}

	public void setMontantTbr(Double montantTbr) {
		this.montantTbr = montantTbr;
	}

	public Double getMontantTtc() {
		return montantTtc;
	}

	public void setMontantTtc(Double montantTtc) {
		this.montantTtc = montantTtc;
	}

	public Double getMontantTotal() {
		return montantTotal;
	}

	public void setMontantTotal(Double montantTotal) {
		this.montantTotal = montantTotal;
	}

	public String getNumPolice() {
		return numPolice;
	}

	public void setNumPolice(String numPolice) {
		this.numPolice = numPolice;
	}

	public Double getMontantTva() {
		return montantTva;
	}

	public void setMontantTva(Double montantTva) {
		this.montantTva = montantTva;
	}

	public String getProduit() {
		return produit;
	}

	public void setProduit(String produit) {
		this.produit = produit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Double getMontantSTbr() {
		return montantSTbr;
	}

	public void setMontantSTbr(Double montantSTbr) {
		this.montantSTbr = montantSTbr;
	}
	
	public int getIddemande() {
		return iddemande;
	}

	public void setIddemande(int iddemande) {
		this.iddemande = iddemande;
	}

	@Override
	public String toString() {
		return "FactureLD [id=" + id + ", numCommande=" + numCommande + ", numfacture=" + numfacture + ", numcontrat="
				+ numcontrat + ", numligne=" + numligne + ", name=" + name + ", classF=" + classF + ", montantTbr="
				+ montantTbr + ", montantTtc=" + montantTtc + ", montantTotal=" + montantTotal + ", numPolice="
				+ numPolice + ", montantTva=" + montantTva + ", produit=" + produit + ", type=" + type + ", date="
				+ date + ", fourniture=" + fourniture + ", montantSTbr=" + montantSTbr + ", iddemande=" + iddemande
				+ ", etat=" + etat + ", numrecnaps=" + numrecnaps + ", datepai=" + datepai + ", trxFactureLydec="
				+ trxFactureLydec + "]";
	}

	public String getEtat() {
		return etat;
	}

	public void setEtat(String etat) {
		this.etat = etat;
	}

	public String getNumrecnaps() {
		return numrecnaps;
	}

	public void setNumrecnaps(String numrecnaps) {
		this.numrecnaps = numrecnaps;
	}
	

	public String getDatepai() {
		return datepai;
	}

	public void setDatepai(String datepai) {
		this.datepai = datepai;
	}
	
	public String getTrxFactureLydec() {
		return trxFactureLydec;
	}

	public void setTrxFactureLydec(String trxFactureLydec) {
		this.trxFactureLydec = trxFactureLydec;
	}

}
