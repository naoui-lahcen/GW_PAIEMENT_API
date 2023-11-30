
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
 *         &lt;element name="ecrireReglementsReturn" type="{http://commun.lydec.com}ReponseReglements"/&gt;
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
    "ecrireReglementsReturn"
})
@XmlRootElement(name = "ecrireReglementsResponse", namespace = "http://service.lydec.com")
public class EcrireReglementsResponse {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected ReponseReglements ecrireReglementsReturn;

    /**
     * Obtient la valeur de la propriété ecrireReglementsReturn.
     * 
     * @return
     *     possible object is
     *     {@link ReponseReglements }
     *     
     */
    public ReponseReglements getEcrireReglementsReturn() {
        return ecrireReglementsReturn;
    }

    /**
     * Définit la valeur de la propriété ecrireReglementsReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link ReponseReglements }
     *     
     */
    public void setEcrireReglementsReturn(ReponseReglements value) {
        this.ecrireReglementsReturn = value;
    }

}
