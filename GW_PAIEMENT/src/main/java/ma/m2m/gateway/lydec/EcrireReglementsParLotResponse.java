
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
 *         &lt;element name="ecrireReglementsParLotReturn" type="{http://commun.lydec.com}ReponseReglementsParLot"/&gt;
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
    "ecrireReglementsParLotReturn"
})
@XmlRootElement(name = "ecrireReglementsParLotResponse", namespace = "http://service.lydec.com")
public class EcrireReglementsParLotResponse {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected ReponseReglementsParLot ecrireReglementsParLotReturn;

    /**
     * Obtient la valeur de la propriété ecrireReglementsParLotReturn.
     * 
     * @return
     *     possible object is
     *     {@link ReponseReglementsParLot }
     *     
     */
    public ReponseReglementsParLot getEcrireReglementsParLotReturn() {
        return ecrireReglementsParLotReturn;
    }

    /**
     * Définit la valeur de la propriété ecrireReglementsParLotReturn.
     * 
     * @param value
     *     allowed object is
     *     {@link ReponseReglementsParLot }
     *     
     */
    public void setEcrireReglementsParLotReturn(ReponseReglementsParLot value) {
        this.ecrireReglementsParLotReturn = value;
    }

}
