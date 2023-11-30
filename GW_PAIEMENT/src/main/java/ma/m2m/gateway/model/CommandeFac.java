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
@Table(name="CommandeFac")
public class CommandeFac implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer  id ;
	
	@Column(name="codeClient")
	private String codeClient;
	
	@Column(name="nomprenom")
	private String nomprenom;
	
	@Column(name="numCommande")
	private String numCommande;
	
	@Column(name="email")
	private String email;
	
	@Column(name="montantTotal")
	private Double montantTotal;
	
	@Column(name="MontantTotalTva")
	private Double MontantTotalTva;
	
	@Column(name="MontantTotalTtc")
	private Double MontantTotalTtc;
	
	@Column(name="MontantTotalTbr")
	private Double MontantTotalTbr;
	
	@Column(name="successUrl")
	private String successUrl;
	
	@Column(name="recallUrl")
	private String recallUrl;
	
	@Column(name="failurl")
	private String failurl;
	
	@Column(name="cmr")
	private String cmr;
	
	@Column(name="gal")
	private String gal;
	
	@Column(name="date")
	private String date;
	
	@Column(name="checksum")
	private String checksum;
	
	@Column(name="xml")
	private String xml;
	
	@Column(name="etat")
	private String etat;
	
	public CommandeFac() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CommandeFac(String codeClient, String nomprenom, String numCommande, String email, Double montantTotal,
			Double montantTotalTva, Double montantTotalTtc, Double montantTotalTbr, String successUrl, String recallUrl,
			String failurl, String cmr, String gal, String date, String checksum, String xml, String etat) {
		super();
		this.codeClient = codeClient;
		this.nomprenom = nomprenom;
		this.numCommande = numCommande;
		this.email = email;
		this.montantTotal = montantTotal;
		MontantTotalTva = montantTotalTva;
		MontantTotalTtc = montantTotalTtc;
		MontantTotalTbr = montantTotalTbr;
		this.successUrl = successUrl;
		this.recallUrl = recallUrl;
		this.failurl = failurl;
		this.cmr = cmr;
		this.gal = gal;
		this.date = date;
		this.checksum = checksum;
		this.xml = xml;
		this.etat = etat;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodeClient() {
		return codeClient;
	}

	public void setCodeClient(String codeClient) {
		this.codeClient = codeClient;
	}

	public String getNomprenom() {
		return nomprenom;
	}

	public void setNomprenom(String nomprenom) {
		this.nomprenom = nomprenom;
	}

	public String getNumCommande() {
		return numCommande;
	}

	public void setNumCommande(String numCommande) {
		this.numCommande = numCommande;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Double getMontantTotal() {
		return montantTotal;
	}

	public void setMontantTotal(Double montantTotal) {
		this.montantTotal = montantTotal;
	}

	public Double getMontantTotalTva() {
		return MontantTotalTva;
	}

	public void setMontantTotalTva(Double montantTotalTva) {
		MontantTotalTva = montantTotalTva;
	}

	public Double getMontantTotalTtc() {
		return MontantTotalTtc;
	}

	public void setMontantTotalTtc(Double montantTotalTtc) {
		MontantTotalTtc = montantTotalTtc;
	}

	public Double getMontantTotalTbr() {
		return MontantTotalTbr;
	}

	public void setMontantTotalTbr(Double montantTotalTbr) {
		MontantTotalTbr = montantTotalTbr;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getRecallUrl() {
		return recallUrl;
	}

	public void setRecallUrl(String recallUrl) {
		this.recallUrl = recallUrl;
	}

	public String getFailurl() {
		return failurl;
	}

	public void setFailurl(String failurl) {
		this.failurl = failurl;
	}

	public String getCmr() {
		return cmr;
	}

	public void setCmr(String cmr) {
		this.cmr = cmr;
	}

	public String getGal() {
		return gal;
	}

	public void setGal(String gal) {
		this.gal = gal;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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
}
