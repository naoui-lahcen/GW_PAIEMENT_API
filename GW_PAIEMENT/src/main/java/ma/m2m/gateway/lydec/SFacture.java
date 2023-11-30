
package ma.m2m.gateway.lydec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SFacture complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SFacture"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="numeroFacture" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numeroLigne" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="numTransLydec" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SFacture", propOrder = {
    "numeroFacture",
    "numeroLigne",
    "numTransLydec"
})
public class SFacture {

    protected int numeroFacture;
    protected int numeroLigne;
    protected int numTransLydec;

    /**
     * Obtient la valeur de la propriété numeroFacture.
     * 
     */
    public int getNumeroFacture() {
        return numeroFacture;
    }

    /**
     * Définit la valeur de la propriété numeroFacture.
     * 
     */
    public void setNumeroFacture(int value) {
        this.numeroFacture = value;
    }

    /**
     * Obtient la valeur de la propriété numeroLigne.
     * 
     */
    public int getNumeroLigne() {
        return numeroLigne;
    }

    /**
     * Définit la valeur de la propriété numeroLigne.
     * 
     */
    public void setNumeroLigne(int value) {
        this.numeroLigne = value;
    }

    /**
     * Obtient la valeur de la propriété numTransLydec.
     * 
     */
    public int getNumTransLydec() {
        return numTransLydec;
    }

    /**
     * Définit la valeur de la propriété numTransLydec.
     * 
     */
    public void setNumTransLydec(int value) {
        this.numTransLydec = value;
    }

}
