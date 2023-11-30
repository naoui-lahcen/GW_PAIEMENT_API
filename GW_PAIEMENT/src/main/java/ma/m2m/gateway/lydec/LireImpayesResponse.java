
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
 *         &lt;element name="lireImpayesReturn" type="{http://commun.lydec.com}Impayes"/&gt;
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
    "lireImpayesReturn"
})
@XmlRootElement(name = "lireImpayesResponse", namespace = "http://service.lydec.com")
public class LireImpayesResponse {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected Impayes lireImpayesReturn;

    /**
     * Obtient la valeur de la propriété lireImpayesReturn.
     * 
     * @return
     *     possible object is
     *     {@link Impayes }
     *     
     */
    public Impayes getLireImpayesReturn() {
        return lireImpayesReturn;
    }

    /**
     * Définit la valeur de la propriété lireImpayesReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link Impayes }
     *     
     */
    public void setLireImpayesReturn(Impayes value) {
        this.lireImpayesReturn = value;
    }

}
