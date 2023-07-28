package ma.m2m.gateway.model;

public class CommandeFac {

	private int  id ;
	private String codeClient;
	private String nomprenom;
	private String numCommande;
	private String email;
	private Double montantTotal;
	private Double MontantTotalTva;
	private Double MontantTotalTtc;
	private Double MontantTotalTbr;
	private String successUrl;
	private String recallUrl;
	private String failurl;
	private String cmr;
	private String gal;
	private String date;
	private String checksum;
	private String xml;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
