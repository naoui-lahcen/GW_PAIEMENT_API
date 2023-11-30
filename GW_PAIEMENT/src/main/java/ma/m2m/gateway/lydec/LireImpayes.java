
package ma.m2m.gateway.lydec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pCritere",
    "pTypeCritere",
    "pNom",
    "pAgence"
})
@XmlRootElement(name = "lireImpayes", namespace = "http://service.lydec.com")
public class LireImpayes {

    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected String pCritere;
    @XmlElement(namespace = "http://service.lydec.com")
    protected int pTypeCritere;
    @XmlElement(namespace = "http://service.lydec.com", required = true, nillable = true)
    protected String pNom;
    @XmlElement(namespace = "http://service.lydec.com")
    protected int pAgence;

    /**
     * Obtient la valeur de la propriété pCritere.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPCritere() {
        return pCritere;
    }

    /**
     * Définit la valeur de la propriété pCritere.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPCritere(String value) {
        this.pCritere = value;
    }

    /**
     * Obtient la valeur de la propriété pTypeCritere.
     * 
     */
    public int getPTypeCritere() {
        return pTypeCritere;
    }

    /**
     * Définit la valeur de la propriété pTypeCritere.
     * 
     */
    public void setPTypeCritere(int value) {
        this.pTypeCritere = value;
    }

    /**
     * Obtient la valeur de la propriété pNom.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPNom() {
        return pNom;
    }

    /**
     * Définit la valeur de la propriété pNom.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPNom(String value) {
        this.pNom = value;
    }

    /**
     * Obtient la valeur de la propriété pAgence.
     * 
     */
    public int getPAgence() {
        return pAgence;
    }

    /**
     * Définit la valeur de la propriété pAgence.
     * 
     */
    public void setPAgence(int value) {
        this.pAgence = value;
    }

}
