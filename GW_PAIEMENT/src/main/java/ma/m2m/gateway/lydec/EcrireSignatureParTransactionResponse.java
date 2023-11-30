
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
 *         &lt;element name="ecrireSignatureParTransactionReturn" type="{http://commun.lydec.com}ReponseSignatureParTransaction"/&gt;
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
    "ecrireSignatureParTransactionReturn"
})
@XmlRootElement(name = "ecrireSignatureParTransactionResponse", namespace = "http://service.lydec.com")
public class EcrireSignatureParTransactionResponse {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected ReponseSignatureParTransaction ecrireSignatureParTransactionReturn;

    /**
     * Obtient la valeur de la propriété ecrireSignatureParTransactionReturn.
     * 
     * @return
     *     possible object is
     *     {@link ReponseSignatureParTransaction }
     *     
     */
    public ReponseSignatureParTransaction getEcrireSignatureParTransactionReturn() {
        return ecrireSignatureParTransactionReturn;
    }

    /**
     * Définit la valeur de la propriété ecrireSignatureParTransactionReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link ReponseSignatureParTransaction }
     *     
     */
    public void setEcrireSignatureParTransactionReturn(ReponseSignatureParTransaction value) {
        this.ecrireSignatureParTransactionReturn = value;
    }

}
