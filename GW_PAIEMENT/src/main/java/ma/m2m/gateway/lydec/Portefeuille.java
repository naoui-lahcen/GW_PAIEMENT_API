
package ma.m2m.gateway.lydec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "Portefeuille", propOrder = {
//    "facNum",
//    "ligne"
//})
public class Portefeuille {

    protected int fac_Num;
    protected int ligne;



    public int getFac_Num() {
		return fac_Num;
	}

	public void setFac_Num(int fac_Num) {
		this.fac_Num = fac_Num;
	}

    public int getLigne() {
        return ligne;
    }

    public void setLigne(int value) {
        this.ligne = value;
    }

	public Portefeuille(int fac_Num, int ligne) {
		super();
		this.fac_Num = fac_Num;
		this.ligne = ligne;
	}

	public Portefeuille() {
		super();
	}

	@Override
	public String toString() {
		return "Portefeuille [fac_Num=" + fac_Num + ", ligne=" + ligne + "]";
	}

}
