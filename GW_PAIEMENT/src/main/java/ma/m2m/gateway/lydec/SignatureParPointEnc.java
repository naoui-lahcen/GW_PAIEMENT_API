
package ma.m2m.gateway.lydec;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SignatureParPointEnc complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SignatureParPointEnc"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="guichetCod" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="mtAnnulations" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="mtAnnuleTimbre" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="mtEncEsp" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="mtEncMp" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="nbrFactures" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignatureParPointEnc", propOrder = {
    "guichetCod",
    "mtAnnulations",
    "mtAnnuleTimbre",
    "mtEncEsp",
    "mtEncMp",
    "nbrFactures"
})
public class SignatureParPointEnc {

    protected int guichetCod;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtAnnulations;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtAnnuleTimbre;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtEncEsp;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtEncMp;
    protected int nbrFactures;

    /**
     * Obtient la valeur de la propriété guichetCod.
     * 
     */
    public int getGuichetCod() {
        return guichetCod;
    }

    /**
     * Définit la valeur de la propriété guichetCod.
     * 
     */
    public void setGuichetCod(int value) {
        this.guichetCod = value;
    }

    /**
     * Obtient la valeur de la propriété mtAnnulations.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMtAnnulations() {
        return mtAnnulations;
    }

    /**
     * Définit la valeur de la propriété mtAnnulations.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMtAnnulations(BigDecimal value) {
        this.mtAnnulations = value;
    }

    /**
     * Obtient la valeur de la propriété mtAnnuleTimbre.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMtAnnuleTimbre() {
        return mtAnnuleTimbre;
    }

    /**
     * Définit la valeur de la propriété mtAnnuleTimbre.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMtAnnuleTimbre(BigDecimal value) {
        this.mtAnnuleTimbre = value;
    }

    /**
     * Obtient la valeur de la propriété mtEncEsp.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMtEncEsp() {
        return mtEncEsp;
    }

    /**
     * Définit la valeur de la propriété mtEncEsp.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMtEncEsp(BigDecimal value) {
        this.mtEncEsp = value;
    }

    /**
     * Obtient la valeur de la propriété mtEncMp.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMtEncMp() {
        return mtEncMp;
    }

    /**
     * Définit la valeur de la propriété mtEncMp.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMtEncMp(BigDecimal value) {
        this.mtEncMp = value;
    }

    /**
     * Obtient la valeur de la propriété nbrFactures.
     * 
     */
    public int getNbrFactures() {
        return nbrFactures;
    }

    /**
     * Définit la valeur de la propriété nbrFactures.
     * 
     */
    public void setNbrFactures(int value) {
        this.nbrFactures = value;
    }

}
