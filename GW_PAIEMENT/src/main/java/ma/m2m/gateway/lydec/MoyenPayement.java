
package ma.m2m.gateway.lydec;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "MoyenPayement", propOrder = {
//    "banqCod",
//    "moyenPai",
//    "typeMoyPai",
//    "montant",
//    "datePai"
//})
public class MoyenPayement {

    private java.lang.String banq_Cod;

    private java.lang.String moyen_Pai;

    private java.lang.String type_Moy_Pai;

    private java.math.BigDecimal montant;

    private java.util.Calendar date_Pai;

	public java.lang.String getBanq_Cod() {
		return banq_Cod;
	}

	public void setBanq_Cod(java.lang.String banq_Cod) {
		this.banq_Cod = banq_Cod;
	}

	public java.lang.String getMoyen_Pai() {
		return moyen_Pai;
	}

	public void setMoyen_Pai(java.lang.String moyen_Pai) {
		this.moyen_Pai = moyen_Pai;
	}

	public java.lang.String getType_Moy_Pai() {
		return type_Moy_Pai;
	}

	public void setType_Moy_Pai(java.lang.String type_Moy_Pai) {
		this.type_Moy_Pai = type_Moy_Pai;
	}

	public java.math.BigDecimal getMontant() {
		return montant;
	}

	public void setMontant(java.math.BigDecimal montant) {
		this.montant = montant;
	}

	public java.util.Calendar getDate_Pai() {
		return date_Pai;
	}

	public void setDate_Pai(java.util.Calendar date_Pai) {
		this.date_Pai = date_Pai;
	}

	public MoyenPayement(String banq_Cod, String moyen_Pai, String type_Moy_Pai, BigDecimal montant,
			Calendar date_Pai) {
		super();
		this.banq_Cod = banq_Cod;
		this.moyen_Pai = moyen_Pai;
		this.type_Moy_Pai = type_Moy_Pai;
		this.montant = montant;
		this.date_Pai = date_Pai;
	}

	public MoyenPayement() {
		super();
	}

	@Override
	public String toString() {
		return "MoyenPayement [banq_Cod=" + banq_Cod + ", moyen_Pai=" + moyen_Pai + ", type_Moy_Pai=" + type_Moy_Pai
				+ ", montant=" + montant + ", date_Pai=" + date_Pai + "]";
	}

    

}
