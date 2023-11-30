
package ma.m2m.gateway.lydec;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SignaturesParTransaction complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SignaturesParTransaction"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="listeSFactures" type="{http://commun.lydec.com}SFacture" maxOccurs="unbounded"/&gt;
 *         &lt;element name="listeSTransactions" type="{http://commun.lydec.com}STransaction" maxOccurs="unbounded"/&gt;
 *         &lt;element name="agcCod" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numeroSignature" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignaturesParTransaction", propOrder = {
    "listeSFactures",
    "listeSTransactions",
    "agcCod",
    "numeroSignature"
})
public class SignaturesParTransaction {

    @XmlElement(required = true, nillable = true)
    protected List<SFacture> listeSFactures;
    @XmlElement(required = true, nillable = true)
    protected List<STransaction> listeSTransactions;
    protected int agcCod;
    protected int numeroSignature;

    /**
     * Gets the value of the listeSFactures property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listeSFactures property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListeSFactures().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SFacture }
     * 
     * 
     */
    public List<SFacture> getListeSFactures() {
        if (listeSFactures == null) {
            listeSFactures = new ArrayList<SFacture>();
        }
        return this.listeSFactures;
    }

    /**
     * Gets the value of the listeSTransactions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listeSTransactions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListeSTransactions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link STransaction }
     * 
     * 
     */
    public List<STransaction> getListeSTransactions() {
        if (listeSTransactions == null) {
            listeSTransactions = new ArrayList<STransaction>();
        }
        return this.listeSTransactions;
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

}
