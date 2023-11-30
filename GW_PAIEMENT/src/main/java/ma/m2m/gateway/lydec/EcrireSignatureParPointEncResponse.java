
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
 *         &lt;element name="ecrireSignatureParPointEncReturn" type="{http://commun.lydec.com}ReponseSignatureParPointEnc"/&gt;
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
    "ecrireSignatureParPointEncReturn"
})
@XmlRootElement(name = "ecrireSignatureParPointEncResponse", namespace = "http://service.lydec.com")
public class EcrireSignatureParPointEncResponse {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected ReponseSignatureParPointEnc ecrireSignatureParPointEncReturn;

    /**
     * Obtient la valeur de la propriété ecrireSignatureParPointEncReturn.
     * 
     * @return
     *     possible object is
     *     {@link ReponseSignatureParPointEnc }
     *     
     */
    public ReponseSignatureParPointEnc getEcrireSignatureParPointEncReturn() {
        return ecrireSignatureParPointEncReturn;
    }

    /**
     * Définit la valeur de la propriété ecrireSignatureParPointEncReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link ReponseSignatureParPointEnc }
     *     
     */
    public void setEcrireSignatureParPointEncReturn(ReponseSignatureParPointEnc value) {
        this.ecrireSignatureParPointEncReturn = value;
    }

}
