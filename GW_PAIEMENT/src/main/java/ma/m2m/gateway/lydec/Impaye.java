
package ma.m2m.gateway.lydec;

import java.math.BigDecimal;
import java.util.Date;


//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "Impaye", propOrder = {
//    "numeroFacture",
//    "numeroLigne",
//    "codeFourniture",
//    "numeroPolice",
//    "montantTTC",
//    "adresse",
//    "delegation",
//    "secteur",
//    "tournee",
//    "ordre",
//    "produit",
//    "montantTimbre",
//    "montantTVA",
//    "dateEcheance"
//})
public class Impaye {

    private int numeroFacture;

    private int numeroLigne;

    private java.lang.String codeFourniture;

    private java.lang.String numeroPolice;

    private java.math.BigDecimal montantTTC;

    private java.lang.String adresse;

    private int delegation;

    private int secteur;

    private int tournee;

    private int ordre;

    private int produit;

    private java.math.BigDecimal montantTimbre;

    private java.math.BigDecimal montantTVA;

    private java.util.Date dateEcheance;

    /**
     * Obtient la valeur de la propriété numeroFacture.
     * 
     */
    public int getNumeroFacture() {
        return numeroFacture;
    }

    /**
     * Définit la valeur de la propriété numeroFacture.
     * 
     */
    public void setNumeroFacture(int value) {
        this.numeroFacture = value;
    }

    /**
     * Obtient la valeur de la propriété numeroLigne.
     * 
     */
    public int getNumeroLigne() {
        return numeroLigne;
    }

    /**
     * Définit la valeur de la propriété numeroLigne.
     * 
     */
    public void setNumeroLigne(int value) {
        this.numeroLigne = value;
    }

    /**
     * Obtient la valeur de la propriété codeFourniture.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeFourniture() {
        return codeFourniture;
    }

    /**
     * Définit la valeur de la propriété codeFourniture.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeFourniture(String value) {
        this.codeFourniture = value;
    }

    /**
     * Obtient la valeur de la propriété numeroPolice.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumeroPolice() {
        return numeroPolice;
    }

    /**
     * Définit la valeur de la propriété numeroPolice.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumeroPolice(String value) {
        this.numeroPolice = value;
    }

    /**
     * Obtient la valeur de la propriété montantTTC.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTTC() {
        return montantTTC;
    }

    /**
     * Définit la valeur de la propriété montantTTC.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTTC(BigDecimal value) {
        this.montantTTC = value;
    }

    /**
     * Obtient la valeur de la propriété adresse.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * Définit la valeur de la propriété adresse.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdresse(String value) {
        this.adresse = value;
    }

    /**
     * Obtient la valeur de la propriété delegation.
     * 
     */
    public int getDelegation() {
        return delegation;
    }

    /**
     * Définit la valeur de la propriété delegation.
     * 
     */
    public void setDelegation(int value) {
        this.delegation = value;
    }

    /**
     * Obtient la valeur de la propriété secteur.
     * 
     */
    public int getSecteur() {
        return secteur;
    }

    /**
     * Définit la valeur de la propriété secteur.
     * 
     */
    public void setSecteur(int value) {
        this.secteur = value;
    }

    /**
     * Obtient la valeur de la propriété tournee.
     * 
     */
    public int getTournee() {
        return tournee;
    }

    /**
     * Définit la valeur de la propriété tournee.
     * 
     */
    public void setTournee(int value) {
        this.tournee = value;
    }

    /**
     * Obtient la valeur de la propriété ordre.
     * 
     */
    public int getOrdre() {
        return ordre;
    }

    /**
     * Définit la valeur de la propriété ordre.
     * 
     */
    public void setOrdre(int value) {
        this.ordre = value;
    }

    /**
     * Obtient la valeur de la propriété produit.
     * 
     */
    public int getProduit() {
        return produit;
    }

    /**
     * Définit la valeur de la propriété produit.
     * 
     */
    public void setProduit(int value) {
        this.produit = value;
    }

    /**
     * Obtient la valeur de la propriété montantTimbre.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTimbre() {
        return montantTimbre;
    }

    /**
     * Définit la valeur de la propriété montantTimbre.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTimbre(BigDecimal value) {
        this.montantTimbre = value;
    }

    /**
     * Obtient la valeur de la propriété montantTVA.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTVA() {
        return montantTVA;
    }

    /**
     * Définit la valeur de la propriété montantTVA.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTVA(BigDecimal value) {
        this.montantTVA = value;
    }

    /**
     * Obtient la valeur de la propriété dateEcheance.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getDateEcheance() {
        return dateEcheance;
    }

    /**
     * Définit la valeur de la propriété dateEcheance.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateEcheance(Date value) {
        this.dateEcheance = value;
    }

	public Impaye(int numeroFacture, int numeroLigne, String codeFourniture, String numeroPolice, BigDecimal montantTTC,
			String adresse, int delegation, int secteur, int tournee, int ordre, int produit, BigDecimal montantTimbre,
			BigDecimal montantTVA, Date dateEcheance) {
		super();
		this.numeroFacture = numeroFacture;
		this.numeroLigne = numeroLigne;
		this.codeFourniture = codeFourniture;
		this.numeroPolice = numeroPolice;
		this.montantTTC = montantTTC;
		this.adresse = adresse;
		this.delegation = delegation;
		this.secteur = secteur;
		this.tournee = tournee;
		this.ordre = ordre;
		this.produit = produit;
		this.montantTimbre = montantTimbre;
		this.montantTVA = montantTVA;
		this.dateEcheance = dateEcheance;
	}

	public Impaye() {
		super();
	}

	@Override
	public String toString() {
		return "Impaye [numeroFacture=" + numeroFacture + ", numeroLigne=" + numeroLigne + ", codeFourniture="
				+ codeFourniture + ", numeroPolice=" + numeroPolice + ", montantTTC=" + montantTTC + ", adresse="
				+ adresse + ", delegation=" + delegation + ", secteur=" + secteur + ", tournee=" + tournee + ", ordre="
				+ ordre + ", produit=" + produit + ", montantTimbre=" + montantTimbre + ", montantTVA=" + montantTVA
				+ ", dateEcheance=" + dateEcheance + "]";
	}

}
