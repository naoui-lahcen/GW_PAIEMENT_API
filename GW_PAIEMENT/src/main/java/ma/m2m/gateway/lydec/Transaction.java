
package ma.m2m.gateway.lydec;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "Transaction", propOrder = {
//    "numTrans",
//    "dateTrans",
//    "dateVal",
//    "agcCod",
//    "guichetCod",
//    "matr",
//    "typeTrans",
//    "etatTrans",
//    "mtDebite",
//    "mtFacture",
//    "mtCrediteCred",
//    "mtEncMp",
//    "mtEncEsp",
//    "mtCrediteVers",
//    "mtCrediteProv",
//    "mtAnnuleTimbre",
//    "mtRembCheq",
//    "mtOd",
//    "trAnnul",
//    "trRecu"
//})
public class Transaction {

    private int num_Trans;

    private java.util.Calendar date_Trans;

    private java.util.Date date_Val;

    private short agc_Cod;

    private short guichet_Cod;

    private int matr;

    private java.lang.String type_Trans;

    private java.lang.String etat_Trans;

    private java.math.BigDecimal mt_Debite;

    private java.math.BigDecimal mt_Facture;

    private java.math.BigDecimal mt_Credite_Cred;

    private java.math.BigDecimal mt_Enc_Mp;

    private java.math.BigDecimal mt_Enc_Esp;

    private java.math.BigDecimal mt_Credite_Vers;

    private java.math.BigDecimal mt_Credite_Prov;

    private java.math.BigDecimal mt_Annule_Timbre;

    private java.math.BigDecimal mt_Remb_Cheq;

    private java.math.BigDecimal mt_Od;

    private int tr_Annul;

    private java.lang.String tr_Recu;

	public int getNum_Trans() {
		return num_Trans;
	}

	public void setNum_Trans(int num_Trans) {
		this.num_Trans = num_Trans;
	}

	public java.util.Calendar getDate_Trans() {
		return date_Trans;
	}

	public void setDate_Trans(java.util.Calendar date_Trans) {
		this.date_Trans = date_Trans;
	}

	public java.util.Date getDate_Val() {
		return date_Val;
	}

	public void setDate_Val(java.util.Date date_Val) {
		this.date_Val = date_Val;
	}

	public short getAgc_Cod() {
		return agc_Cod;
	}

	public void setAgc_Cod(short agc_Cod) {
		this.agc_Cod = agc_Cod;
	}

	public short getGuichet_Cod() {
		return guichet_Cod;
	}

	public void setGuichet_Cod(short guichet_Cod) {
		this.guichet_Cod = guichet_Cod;
	}

	public int getMatr() {
		return matr;
	}

	public void setMatr(int matr) {
		this.matr = matr;
	}

	public java.lang.String getType_Trans() {
		return type_Trans;
	}

	public void setType_Trans(java.lang.String type_Trans) {
		this.type_Trans = type_Trans;
	}

	public java.lang.String getEtat_Trans() {
		return etat_Trans;
	}

	public void setEtat_Trans(java.lang.String etat_Trans) {
		this.etat_Trans = etat_Trans;
	}

	public java.math.BigDecimal getMt_Debite() {
		return mt_Debite;
	}

	public void setMt_Debite(java.math.BigDecimal mt_Debite) {
		this.mt_Debite = mt_Debite;
	}

	public java.math.BigDecimal getMt_Facture() {
		return mt_Facture;
	}

	public void setMt_Facture(java.math.BigDecimal mt_Facture) {
		this.mt_Facture = mt_Facture;
	}

	public java.math.BigDecimal getMt_Credite_Cred() {
		return mt_Credite_Cred;
	}

	public void setMt_Credite_Cred(java.math.BigDecimal mt_Credite_Cred) {
		this.mt_Credite_Cred = mt_Credite_Cred;
	}

	public java.math.BigDecimal getMt_Enc_Mp() {
		return mt_Enc_Mp;
	}

	public void setMt_Enc_Mp(java.math.BigDecimal mt_Enc_Mp) {
		this.mt_Enc_Mp = mt_Enc_Mp;
	}

	public java.math.BigDecimal getMt_Enc_Esp() {
		return mt_Enc_Esp;
	}

	public void setMt_Enc_Esp(java.math.BigDecimal mt_Enc_Esp) {
		this.mt_Enc_Esp = mt_Enc_Esp;
	}

	public java.math.BigDecimal getMt_Credite_Vers() {
		return mt_Credite_Vers;
	}

	public void setMt_Credite_Vers(java.math.BigDecimal mt_Credite_Vers) {
		this.mt_Credite_Vers = mt_Credite_Vers;
	}

	public java.math.BigDecimal getMt_Credite_Prov() {
		return mt_Credite_Prov;
	}

	public void setMt_Credite_Prov(java.math.BigDecimal mt_Credite_Prov) {
		this.mt_Credite_Prov = mt_Credite_Prov;
	}

	public java.math.BigDecimal getMt_Annule_Timbre() {
		return mt_Annule_Timbre;
	}

	public void setMt_Annule_Timbre(java.math.BigDecimal mt_Annule_Timbre) {
		this.mt_Annule_Timbre = mt_Annule_Timbre;
	}

	public java.math.BigDecimal getMt_Remb_Cheq() {
		return mt_Remb_Cheq;
	}

	public void setMt_Remb_Cheq(java.math.BigDecimal mt_Remb_Cheq) {
		this.mt_Remb_Cheq = mt_Remb_Cheq;
	}

	public java.math.BigDecimal getMt_Od() {
		return mt_Od;
	}

	public void setMt_Od(java.math.BigDecimal mt_Od) {
		this.mt_Od = mt_Od;
	}

	public int getTr_Annul() {
		return tr_Annul;
	}

	public void setTr_Annul(int tr_Annul) {
		this.tr_Annul = tr_Annul;
	}

	public java.lang.String getTr_Recu() {
		return tr_Recu;
	}

	public void setTr_Recu(java.lang.String tr_Recu) {
		this.tr_Recu = tr_Recu;
	}

	public Transaction(int num_Trans, Calendar date_Trans, Date date_Val, short agc_Cod, short guichet_Cod, int matr,
			String type_Trans, String etat_Trans, BigDecimal mt_Debite, BigDecimal mt_Facture,
			BigDecimal mt_Credite_Cred, BigDecimal mt_Enc_Mp, BigDecimal mt_Enc_Esp, BigDecimal mt_Credite_Vers,
			BigDecimal mt_Credite_Prov, BigDecimal mt_Annule_Timbre, BigDecimal mt_Remb_Cheq, BigDecimal mt_Od,
			int tr_Annul, String tr_Recu) {
		super();
		this.num_Trans = num_Trans;
		this.date_Trans = date_Trans;
		this.date_Val = date_Val;
		this.agc_Cod = agc_Cod;
		this.guichet_Cod = guichet_Cod;
		this.matr = matr;
		this.type_Trans = type_Trans;
		this.etat_Trans = etat_Trans;
		this.mt_Debite = mt_Debite;
		this.mt_Facture = mt_Facture;
		this.mt_Credite_Cred = mt_Credite_Cred;
		this.mt_Enc_Mp = mt_Enc_Mp;
		this.mt_Enc_Esp = mt_Enc_Esp;
		this.mt_Credite_Vers = mt_Credite_Vers;
		this.mt_Credite_Prov = mt_Credite_Prov;
		this.mt_Annule_Timbre = mt_Annule_Timbre;
		this.mt_Remb_Cheq = mt_Remb_Cheq;
		this.mt_Od = mt_Od;
		this.tr_Annul = tr_Annul;
		this.tr_Recu = tr_Recu;
	}

	public Transaction() {
		super();
	}

	@Override
	public String toString() {
		return "Transaction [num_Trans=" + num_Trans + ", date_Trans=" + date_Trans + ", date_Val=" + date_Val
				+ ", agc_Cod=" + agc_Cod + ", guichet_Cod=" + guichet_Cod + ", matr=" + matr + ", type_Trans="
				+ type_Trans + ", etat_Trans=" + etat_Trans + ", mt_Debite=" + mt_Debite + ", mt_Facture=" + mt_Facture
				+ ", mt_Credite_Cred=" + mt_Credite_Cred + ", mt_Enc_Mp=" + mt_Enc_Mp + ", mt_Enc_Esp=" + mt_Enc_Esp
				+ ", mt_Credite_Vers=" + mt_Credite_Vers + ", mt_Credite_Prov=" + mt_Credite_Prov
				+ ", mt_Annule_Timbre=" + mt_Annule_Timbre + ", mt_Remb_Cheq=" + mt_Remb_Cheq + ", mt_Od=" + mt_Od
				+ ", tr_Annul=" + tr_Annul + ", tr_Recu=" + tr_Recu + "]";
	}

    

}
