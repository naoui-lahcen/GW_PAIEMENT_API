
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
 *         &lt;element name="pSignaturesParPointEnc" type="{http://commun.lydec.com}SignaturesParPointEnc"/&gt;
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
    "pSignaturesParPointEnc"
})
@XmlRootElement(name = "ecrireSignatureParPointEnc", namespace = "http://service.lydec.com")
public class EcrireSignatureParPointEnc {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected SignaturesParPointEnc pSignaturesParPointEnc;

    /**
     * Obtient la valeur de la propriété pSignaturesParPointEnc.
     * 
     * @return
     *     possible object is
     *     {@link SignaturesParPointEnc }
     *     
     */
    public SignaturesParPointEnc getPSignaturesParPointEnc() {
        return pSignaturesParPointEnc;
    }

    /**
     * Définit la valeur de la propriété pSignaturesParPointEnc.
     * 
     * @param value
     *     allowed object is
     *     {@link SignaturesParPointEnc }
     *     
     */
    public void setPSignaturesParPointEnc(SignaturesParPointEnc value) {
        this.pSignaturesParPointEnc = value;
    }

}
