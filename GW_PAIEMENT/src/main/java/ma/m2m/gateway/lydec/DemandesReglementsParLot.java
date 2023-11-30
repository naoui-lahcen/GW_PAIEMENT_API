
package ma.m2m.gateway.lydec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour DemandesReglementsParLot complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="DemandesReglementsParLot"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="listeTransactions" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="listePortefeuilles" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="listeMoyensPayement" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="signature" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="agc_Cod" type="{http://www.w3.org/2001/XMLSchema}short"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DemandesReglementsParLot", propOrder = {
    "listeTransactions",
    "listePortefeuilles",
    "listeMoyensPayement",
    "signature",
    "agcCod"
})
public class DemandesReglementsParLot {

    @XmlElement(required = true, nillable = true)
    protected String listeTransactions;
    @XmlElement(required = true, nillable = true)
    protected String listePortefeuilles;
    @XmlElement(required = true, nillable = true)
    protected String listeMoyensPayement;
    @XmlElement(required = true, nillable = true)
    protected String signature;
    @XmlElement(name = "agc_Cod")
    protected short agcCod;

    /**
     * Obtient la valeur de la propriété listeTransactions.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListeTransactions() {
        return listeTransactions;
    }

    /**
     * Définit la valeur de la propriété listeTransactions.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListeTransactions(String value) {
        this.listeTransactions = value;
    }

    /**
     * Obtient la valeur de la propriété listePortefeuilles.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListePortefeuilles() {
        return listePortefeuilles;
    }

    /**
     * Définit la valeur de la propriété listePortefeuilles.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListePortefeuilles(String value) {
        this.listePortefeuilles = value;
    }

    /**
     * Obtient la valeur de la propriété listeMoyensPayement.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListeMoyensPayement() {
        return listeMoyensPayement;
    }

    /**
     * Définit la valeur de la propriété listeMoyensPayement.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListeMoyensPayement(String value) {
        this.listeMoyensPayement = value;
    }

    /**
     * Obtient la valeur de la propriété signature.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Définit la valeur de la propriété signature.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignature(String value) {
        this.signature = value;
    }

    /**
     * Obtient la valeur de la propriété agcCod.
     * 
     */
    public short getAgcCod() {
        return agcCod;
    }

    /**
     * Définit la valeur de la propriété agcCod.
     * 
     */
    public void setAgcCod(short value) {
        this.agcCod = value;
    }

}
