
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
 *         &lt;element name="pSignatureComplete" type="{http://commun.lydec.com}SignatureComplete"/&gt;
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
    "pSignatureComplete"
})
@XmlRootElement(name = "ecrireSignature", namespace = "http://service.lydec.com")
public class EcrireSignature {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected SignatureComplete pSignatureComplete;

    /**
     * Obtient la valeur de la propriété pSignatureComplete.
     * 
     * @return
     *     possible object is
     *     {@link SignatureComplete }
     *     
     */
    public SignatureComplete getPSignatureComplete() {
        return pSignatureComplete;
    }

    /**
     * Définit la valeur de la propriété pSignatureComplete.
     * 
     * @param value
     *     allowed object is
     *     {@link SignatureComplete }
     *     
     */
    public void setPSignatureComplete(SignatureComplete value) {
        this.pSignatureComplete = value;
    }

}
