
package ma.m2m.gateway.lydec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ReponseSignature complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ReponseSignature"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="numero" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="validation" type="{http://www.w3.org/2001/XMLSchema}short"/&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ok" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReponseSignature", propOrder = {
    "numero",
    "validation",
    "message",
    "ok"
})
public class ReponseSignature {

    protected int numero;
    protected short validation;
    @XmlElement(required = true, nillable = true)
    protected String message;
    protected boolean ok;

    /**
     * Obtient la valeur de la propriété numero.
     * 
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Définit la valeur de la propriété numero.
     * 
     */
    public void setNumero(int value) {
        this.numero = value;
    }

    /**
     * Obtient la valeur de la propriété validation.
     * 
     */
    public short getValidation() {
        return validation;
    }

    /**
     * Définit la valeur de la propriété validation.
     * 
     */
    public void setValidation(short value) {
        this.validation = value;
    }

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

}
