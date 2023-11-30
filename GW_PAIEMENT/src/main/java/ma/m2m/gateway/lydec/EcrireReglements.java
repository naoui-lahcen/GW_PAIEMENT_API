
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
 *         &lt;element name="pDemandeReglements" type="{http://commun.lydec.com}DemandesReglements"/&gt;
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
    "pDemandeReglements"
})
@XmlRootElement(name = "ecrireReglements", namespace = "http://service.lydec.com")
public class EcrireReglements {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected DemandesReglements pDemandeReglements;

    /**
     * Obtient la valeur de la propriété pDemandeReglements.
     * 
     * @return
     *     possible object is
     *     {@link DemandesReglements }
     *     
     */
    public DemandesReglements getPDemandeReglements() {
        return pDemandeReglements;
    }

    /**
     * Définit la valeur de la propriété pDemandeReglements.
     * 
     * @param value
     *     allowed object is
     *     {@link DemandesReglements }
     *     
     */
    public void setPDemandeReglements(DemandesReglements value) {
        this.pDemandeReglements = value;
    }

}
