
package ma.m2m.gateway.lydec;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SignaturesParPointEnc complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SignaturesParPointEnc"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="listeSignaturesParPointEnc" type="{http://commun.lydec.com}SignatureParPointEnc" maxOccurs="unbounded"/&gt;
 *         &lt;element name="numeroSignature" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="agcCod" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignaturesParPointEnc", propOrder = {
    "listeSignaturesParPointEnc",
    "numeroSignature",
    "agcCod"
})
public class SignaturesParPointEnc {

    @XmlElement(required = true, nillable = true)
    protected List<SignatureParPointEnc> listeSignaturesParPointEnc;
    protected int numeroSignature;
    protected int agcCod;

    /**
     * Gets the value of the listeSignaturesParPointEnc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listeSignaturesParPointEnc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListeSignaturesParPointEnc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SignatureParPointEnc }
     * 
     * 
     */
    public List<SignatureParPointEnc> getListeSignaturesParPointEnc() {
        if (listeSignaturesParPointEnc == null) {
            listeSignaturesParPointEnc = new ArrayList<SignatureParPointEnc>();
        }
        return this.listeSignaturesParPointEnc;
    }

    /**
     * Obtient la valeur de la propriété numeroSignature.
     * 
     */
    public int getNumeroSignature() {
        return numeroSignature;
    }

    /**
     * Définit la valeur de la propriété numeroSignature.
     * 
     */
    public void setNumeroSignature(int value) {
        this.numeroSignature = value;
    }

    /**
     * Obtient la valeur de la propriété agcCod.
     * 
     */
    public int getAgcCod() {
        return agcCod;
    }

    /**
     * Définit la valeur de la propriété agcCod.
     * 
     */
    public void setAgcCod(int value) {
        this.agcCod = value;
    }

}
