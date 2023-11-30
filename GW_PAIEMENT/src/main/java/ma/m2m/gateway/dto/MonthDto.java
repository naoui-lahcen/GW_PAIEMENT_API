package ma.m2m.gateway.dto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class MonthDto {
	
	private String month;
	
	private String value;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public MonthDto(String month, String valueMonth) {
		super();
		this.month = month;
		this.value = value;
	}

	public MonthDto() {
		super();
	}

	@Override
	public String toString() {
		return "MonthDto [month=" + month + ", value=" + value + "]";
	}
	
	
	

}
