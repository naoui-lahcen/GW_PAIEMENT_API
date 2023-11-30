
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
 *         &lt;element name="pDemandeReglementsParLot" type="{http://commun.lydec.com}DemandesReglementsParLot"/&gt;
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
    "pDemandeReglementsParLot"
})
@XmlRootElement(name = "ecrireReglementsParLot", namespace = "http://service.lydec.com")
public class EcrireReglementsParLot {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected DemandesReglementsParLot pDemandeReglementsParLot;

    /**
     * Obtient la valeur de la propriété pDemandeReglementsParLot.
     * 
     * @return
     *     possible object is
     *     {@link DemandesReglementsParLot }
     *     
     */
    public DemandesReglementsParLot getPDemandeReglementsParLot() {
        return pDemandeReglementsParLot;
    }

    /**
     * Définit la valeur de la propriété pDemandeReglementsParLot.
     * 
     * @param value
     *     allowed object is
     *     {@link DemandesReglementsParLot }
     *     
     */
    public void setPDemandeReglementsParLot(DemandesReglementsParLot value) {
        this.pDemandeReglementsParLot = value;
    }

}
