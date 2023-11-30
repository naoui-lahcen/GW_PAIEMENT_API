
package ma.m2m.gateway.lydec;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour STransaction complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="STransaction"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="guichetCod" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="mtAnnuleTimbre" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="mtEncEsp" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="mtEncMp" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="numTransLydec" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numTransPartenaire" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "STransaction", propOrder = {
    "guichetCod",
    "mtAnnuleTimbre",
    "mtEncEsp",
    "mtEncMp",
    "numTransLydec",
    "numTransPartenaire"
})
public class STransaction {

    protected int guichetCod;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtAnnuleTimbre;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtEncEsp;
    @XmlElement(required = true, nillable = true)
    protected BigDecimal mtEncMp;
    protected int numTransLydec;
    protected int numTransPartenaire;

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
     * Obtient la valeur de la propriété numTransLydec.
     * 
     */
    public int getNumTransLydec() {
        return numTransLydec;
    }

    /**
     * Définit la valeur de la propriété numTransLydec.
     * 
     */
    public void setNumTransLydec(int value) {
        this.numTransLydec = value;
    }

    /**
     * Obtient la valeur de la propriété numTransPartenaire.
     * 
     */
    public int getNumTransPartenaire() {
        return numTransPartenaire;
    }

    /**
     * Définit la valeur de la propriété numTransPartenaire.
     * 
     */
    public void setNumTransPartenaire(int value) {
        this.numTransPartenaire = value;
    }

}
