
package ma.m2m.gateway.lydec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "DemandesReglements", propOrder = {
//    "listePortefeuilles",
//    "listeMoyensPayement",
//    "transaction",
//    "agcCod"
//})
public class DemandesReglements {

    //@XmlElement(required = true, nillable = true)
    protected Portefeuille[] listePortefeuilles;
    //@XmlElement(required = true, nillable = true)
    protected MoyenPayement[] listeMoyensPayement;
    //@XmlElement(required = true, nillable = true)
    protected Transaction transaction;
    //@XmlElement(name = "agc_Cod")
    protected short agc_Cod;
    
    
	public Portefeuille[] getListePortefeuilles() {
		return listePortefeuilles;
	}
	public void setListePortefeuilles(Portefeuille[] listePortefeuilles) {
		this.listePortefeuilles = listePortefeuilles;
	}
	public MoyenPayement[] getListeMoyensPayement() {
		return listeMoyensPayement;
	}
	public void setListeMoyensPayement(MoyenPayement[] listeMoyensPayement) {
		this.listeMoyensPayement = listeMoyensPayement;
	}
	public Transaction getTransaction() {
		return transaction;
	}
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	public short getAgc_Cod() {
		return agc_Cod;
	}
	public void setAgc_Cod(short agc_Cod) {
		this.agc_Cod = agc_Cod;
	}
	public DemandesReglements(Portefeuille[] listePortefeuilles, MoyenPayement[] listeMoyensPayement,
			Transaction transaction, short agc_Cod) {
		super();
		this.listePortefeuilles = listePortefeuilles;
		this.listeMoyensPayement = listeMoyensPayement;
		this.transaction = transaction;
		this.agc_Cod = agc_Cod;
	}
	public DemandesReglements() {
		super();
	}
	@Override
	public String toString() {
		return "DemandesReglements [listePortefeuilles=" + Arrays.toString(listePortefeuilles)
				+ ", listeMoyensPayement=" + Arrays.toString(listeMoyensPayement) + ", transaction=" + transaction
				+ ", agc_Cod=" + agc_Cod + "]";
	}

   
}
