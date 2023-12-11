package ma.m2m.gateway.dto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-11-23 
 */

public class Cartes {
	
	private String carte;
	
	private String pcidsscarte;
	
	private Integer year;
	
	//private MonthDto monthDto;
	
	private String mois;
	
	private String moisValue;


	public String getCarte() {
		return carte;
	}

	public void setCarte(String carte) {
		this.carte = carte;
	}

	public String getPcidsscarte() {
		return pcidsscarte;
	}

	public void setPcidsscarte(String pcidsscarte) {
		this.pcidsscarte = pcidsscarte;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	
	public String getMois() {
		return mois;
	}

	public void setMois(String mois) {
		this.mois = mois;
	}

	public String getMoisValue() {
		return moisValue;
	}

	public void setMoisValue(String moisValue) {
		this.moisValue = moisValue;
	}
//	public MonthDto getMonthDto() {
//		return monthDto;
//	}
//
//	public void setMonthDto(MonthDto monthDto) {
//		this.monthDto = monthDto;
//	}

	public Cartes() {
		super();
	}

	public Cartes(String carte, String pcidsscarte) {
		super();
		this.carte = carte;
		this.pcidsscarte = pcidsscarte;
	}

	public Cartes(String carte, String pcidsscarte, Integer year, String mois, String moisValue) {
		super();
		this.carte = carte;
		this.pcidsscarte = pcidsscarte;
		this.year = year;
		this.mois = mois;
		this.moisValue = moisValue;
	}

	@Override
	public String toString() {
		return "Cartes [carte=" + carte + ", pcidsscarte=" + pcidsscarte + ", year=" + year + ", mois=" + mois
				+ ", moisValue=" + moisValue + "]";
	}


}
