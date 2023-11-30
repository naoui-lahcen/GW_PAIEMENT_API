
package ma.m2m.gateway.lydec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pSignaturesParTransaction" type="{http://commun.lydec.com}SignaturesParTransaction"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pSignaturesParTransaction"
})
@XmlRootElement(name = "ecrireSignatureParTransaction", namespace = "http://service.lydec.com")
public class EcrireSignatureParTransaction {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected SignaturesParTransaction pSignaturesParTransaction;

    /**
     * Obtient la valeur de la propriété pSignaturesParTransaction.
     * 
     * @return
     *     possible object is
     *     {@link SignaturesParTransaction }
     *     
     */
    public SignaturesParTransaction getPSignaturesParTransaction() {
        return pSignaturesParTransaction;
    }

    /**
     * Définit la valeur de la propriété pSignaturesParTransaction.
     * 
     * @param value
     *     allowed object is
     *     {@link SignaturesParTransaction }
     *     
     */
    public void setPSignaturesParTransaction(SignaturesParTransaction value) {
        this.pSignaturesParTransaction = value;
    }

}
