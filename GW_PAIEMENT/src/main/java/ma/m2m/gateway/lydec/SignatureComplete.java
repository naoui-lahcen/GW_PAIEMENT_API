
package ma.m2m.gateway.lydec;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour SignatureComplete complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SignatureComplete"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="typeSignature" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateSignature" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="dateDebut" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="dateFin" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="numeroTransactionDebut" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numeroTransactionFin" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numeroTransactionIfxDebut" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numeroTransactionIfxFin" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="espaceServiceDebut" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="espaceServiceFin" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="montantDebiteDebut" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="montantDebiteFin" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="nombreAnnulations" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="nombreRejets" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="nombreTransactionsDebitees" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="nombreTransactions" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="montantTotalDebite" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="montantTotalEspecesTr" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="montantTotalAutresMPTr" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="montantTotalTimbresAnnules" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="nombreFacturesRegles" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="montantTotalTTC" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="nombreMPs" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="montantTotalEspecesMP" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="montantTotalAutresMPMP" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="agcCod" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignatureComplete", propOrder = {
    "typeSignature",
    "dateSignature",
    "dateDebut",
    "dateFin",
    "numeroTransactionDebut",
    "numeroTransactionFin",
    "numeroTransactionIfxDebut",
    "numeroTransactionIfxFin",
    "espaceServiceDebut",
    "espaceServiceFin",
    "montantDebiteDebut",
    "montantDebiteFin",
    "nombreAnnulations",
    "nombreRejets",
    "nombreTransactionsDebitees",
    "nombreTransactions",
    "montantTotalDebite",
    "montantTotalEspecesTr",
    "montantTotalAutresMPTr",
    "montantTotalTimbresAnnules",
    "nombreFacturesRegles",
    "montantTotalTTC",
    "nombreMPs",
    "montantTotalEspecesMP",
    "montantTotalAutresMPMP",
    "agcCod"
})
public class SignatureComplete {

    @XmlElement(required = true, nillable = true)
    protected String typeSignature;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateSignature;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateDebut;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateFin;
    protected int numeroTransactionDebut;
    protected int numeroTransactionFin;
    protected int numeroTransactionIfxDebut;
    protected int numeroTransactionIfxFin;
    protected int espaceServiceDebut;
    protected int espaceServiceFin;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantDebiteDebut;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantDebiteFin;
    protected int nombreAnnulations;
    protected int nombreRejets;
    protected int nombreTransactionsDebitees;
    protected int nombreTransactions;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalDebite;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalEspecesTr;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalAutresMPTr;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalTimbresAnnules;
    protected int nombreFacturesRegles;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalTTC;
    protected int nombreMPs;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalEspecesMP;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal montantTotalAutresMPMP;
    protected int agcCod;

    /**
     * Obtient la valeur de la propriété typeSignature.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeSignature() {
        return typeSignature;
    }

    /**
     * Définit la valeur de la propriété typeSignature.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeSignature(String value) {
        this.typeSignature = value;
    }

    /**
     * Obtient la valeur de la propriété dateSignature.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateSignature() {
        return dateSignature;
    }

    /**
     * Définit la valeur de la propriété dateSignature.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateSignature(XMLGregorianCalendar value) {
        this.dateSignature = value;
    }

    /**
     * Obtient la valeur de la propriété dateDebut.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateDebut() {
        return dateDebut;
    }

    /**
     * Définit la valeur de la propriété dateDebut.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateDebut(XMLGregorianCalendar value) {
        this.dateDebut = value;
    }

    /**
     * Obtient la valeur de la propriété dateFin.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateFin() {
        return dateFin;
    }

    /**
     * Définit la valeur de la propriété dateFin.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateFin(XMLGregorianCalendar value) {
        this.dateFin = value;
    }

    /**
     * Obtient la valeur de la propriété numeroTransactionDebut.
     * 
     */
    public int getNumeroTransactionDebut() {
        return numeroTransactionDebut;
    }

    /**
     * Définit la valeur de la propriété numeroTransactionDebut.
     * 
     */
    public void setNumeroTransactionDebut(int value) {
        this.numeroTransactionDebut = value;
    }

    /**
     * Obtient la valeur de la propriété numeroTransactionFin.
     * 
     */
    public int getNumeroTransactionFin() {
        return numeroTransactionFin;
    }

    /**
     * Définit la valeur de la propriété numeroTransactionFin.
     * 
     */
    public void setNumeroTransactionFin(int value) {
        this.numeroTransactionFin = value;
    }

    /**
     * Obtient la valeur de la propriété numeroTransactionIfxDebut.
     * 
     */
    public int getNumeroTransactionIfxDebut() {
        return numeroTransactionIfxDebut;
    }

    /**
     * Définit la valeur de la propriété numeroTransactionIfxDebut.
     * 
     */
    public void setNumeroTransactionIfxDebut(int value) {
        this.numeroTransactionIfxDebut = value;
    }

    /**
     * Obtient la valeur de la propriété numeroTransactionIfxFin.
     * 
     */
    public int getNumeroTransactionIfxFin() {
        return numeroTransactionIfxFin;
    }

    /**
     * Définit la valeur de la propriété numeroTransactionIfxFin.
     * 
     */
    public void setNumeroTransactionIfxFin(int value) {
        this.numeroTransactionIfxFin = value;
    }

    /**
     * Obtient la valeur de la propriété espaceServiceDebut.
     * 
     */
    public int getEspaceServiceDebut() {
        return espaceServiceDebut;
    }

    /**
     * Définit la valeur de la propriété espaceServiceDebut.
     * 
     */
    public void setEspaceServiceDebut(int value) {
        this.espaceServiceDebut = value;
    }

    /**
     * Obtient la valeur de la propriété espaceServiceFin.
     * 
     */
    public int getEspaceServiceFin() {
        return espaceServiceFin;
    }

    /**
     * Définit la valeur de la propriété espaceServiceFin.
     * 
     */
    public void setEspaceServiceFin(int value) {
        this.espaceServiceFin = value;
    }

    /**
     * Obtient la valeur de la propriété montantDebiteDebut.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantDebiteDebut() {
        return montantDebiteDebut;
    }

    /**
     * Définit la valeur de la propriété montantDebiteDebut.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantDebiteDebut(BigDecimal value) {
        this.montantDebiteDebut = value;
    }

    /**
     * Obtient la valeur de la propriété montantDebiteFin.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantDebiteFin() {
        return montantDebiteFin;
    }

    /**
     * Définit la valeur de la propriété montantDebiteFin.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantDebiteFin(BigDecimal value) {
        this.montantDebiteFin = value;
    }

    /**
     * Obtient la valeur de la propriété nombreAnnulations.
     * 
     */
    public int getNombreAnnulations() {
        return nombreAnnulations;
    }

    /**
     * Définit la valeur de la propriété nombreAnnulations.
     * 
     */
    public void setNombreAnnulations(int value) {
        this.nombreAnnulations = value;
    }

    /**
     * Obtient la valeur de la propriété nombreRejets.
     * 
     */
    public int getNombreRejets() {
        return nombreRejets;
    }

    /**
     * Définit la valeur de la propriété nombreRejets.
     * 
     */
    public void setNombreRejets(int value) {
        this.nombreRejets = value;
    }

    /**
     * Obtient la valeur de la propriété nombreTransactionsDebitees.
     * 
     */
    public int getNombreTransactionsDebitees() {
        return nombreTransactionsDebitees;
    }

    /**
     * Définit la valeur de la propriété nombreTransactionsDebitees.
     * 
     */
    public void setNombreTransactionsDebitees(int value) {
        this.nombreTransactionsDebitees = value;
    }

    /**
     * Obtient la valeur de la propriété nombreTransactions.
     * 
     */
    public int getNombreTransactions() {
        return nombreTransactions;
    }

    /**
     * Définit la valeur de la propriété nombreTransactions.
     * 
     */
    public void setNombreTransactions(int value) {
        this.nombreTransactions = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalDebite.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalDebite() {
        return montantTotalDebite;
    }

    /**
     * Définit la valeur de la propriété montantTotalDebite.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalDebite(BigDecimal value) {
        this.montantTotalDebite = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalEspecesTr.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalEspecesTr() {
        return montantTotalEspecesTr;
    }

    /**
     * Définit la valeur de la propriété montantTotalEspecesTr.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalEspecesTr(BigDecimal value) {
        this.montantTotalEspecesTr = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalAutresMPTr.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalAutresMPTr() {
        return montantTotalAutresMPTr;
    }

    /**
     * Définit la valeur de la propriété montantTotalAutresMPTr.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalAutresMPTr(BigDecimal value) {
        this.montantTotalAutresMPTr = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalTimbresAnnules.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalTimbresAnnules() {
        return montantTotalTimbresAnnules;
    }

    /**
     * Définit la valeur de la propriété montantTotalTimbresAnnules.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalTimbresAnnules(BigDecimal value) {
        this.montantTotalTimbresAnnules = value;
    }

    /**
     * Obtient la valeur de la propriété nombreFacturesRegles.
     * 
     */
    public int getNombreFacturesRegles() {
        return nombreFacturesRegles;
    }

    /**
     * Définit la valeur de la propriété nombreFacturesRegles.
     * 
     */
    public void setNombreFacturesRegles(int value) {
        this.nombreFacturesRegles = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalTTC.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalTTC() {
        return montantTotalTTC;
    }

    /**
     * Définit la valeur de la propriété montantTotalTTC.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalTTC(BigDecimal value) {
        this.montantTotalTTC = value;
    }

    /**
     * Obtient la valeur de la propriété nombreMPs.
     * 
     */
    public int getNombreMPs() {
        return nombreMPs;
    }

    /**
     * Définit la valeur de la propriété nombreMPs.
     * 
     */
    public void setNombreMPs(int value) {
        this.nombreMPs = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalEspecesMP.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalEspecesMP() {
        return montantTotalEspecesMP;
    }

    /**
     * Définit la valeur de la propriété montantTotalEspecesMP.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalEspecesMP(BigDecimal value) {
        this.montantTotalEspecesMP = value;
    }

    /**
     * Obtient la valeur de la propriété montantTotalAutresMPMP.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMontantTotalAutresMPMP() {
        return montantTotalAutresMPMP;
    }

    /**
     * Définit la valeur de la propriété montantTotalAutresMPMP.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMontantTotalAutresMPMP(BigDecimal value) {
        this.montantTotalAutresMPMP = value;
    }

    /**
     * Obtient la valeur de la propriété agcCod.
     * 
     */
    public int getAgcCod() {
        return agcCod;
    }

    /**
     * Définit la valeur de la propriété agcCod.
     * 
     */
    public void setAgcCod(int value) {
        this.agcCod = value;
    }

}
