
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
 *         &lt;element name="ecrireSignatureReturn" type="{http://commun.lydec.com}ReponseSignature"/&gt;
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
    "ecrireSignatureReturn"
})
@XmlRootElement(name = "ecrireSignatureResponse", namespace = "http://service.lydec.com")
public class EcrireSignatureResponse {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected ReponseSignature ecrireSignatureReturn;

    /**
     * Obtient la valeur de la propriété ecrireSignatureReturn.
     * 
     * @return
     *     possible object is
     *     {@link ReponseSignature }
     *     
     */
    public ReponseSignature getEcrireSignatureReturn() {
        return ecrireSignatureReturn;
    }

    /**
     * Définit la valeur de la propriété ecrireSignatureReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link ReponseSignature }
     *     
     */
    public void setEcrireSignatureReturn(ReponseSignature value) {
        this.ecrireSignatureReturn = value;
    }

}
