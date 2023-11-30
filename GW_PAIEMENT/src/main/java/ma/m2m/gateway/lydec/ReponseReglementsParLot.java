
package ma.m2m.gateway.lydec;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ReponseReglementsParLot complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ReponseReglementsParLot"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ok" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="numerosTransactions" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReponseReglementsParLot", propOrder = {
    "message",
    "ok",
    "numerosTransactions"
})
public class ReponseReglementsParLot {

    @XmlElement(required = true, nillable = true)
    protected String message;
    protected boolean ok;
    @XmlElement(type = Integer.class)
    protected List<Integer> numerosTransactions;

    /**
     * Obtient la valeur de la propriété message.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Définit la valeur de la propriété message.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Obtient la valeur de la propriété ok.
     * 
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * Définit la valeur de la propriété ok.
     * 
     */
    public void setOk(boolean value) {
        this.ok = value;
    }

    /**
     * Gets the value of the numerosTransactions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the numerosTransactions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNumerosTransactions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getNumerosTransactions() {
        if (numerosTransactions == null) {
            numerosTransactions = new ArrayList<Integer>();
        }
        return this.numerosTransactions;
    }

}
