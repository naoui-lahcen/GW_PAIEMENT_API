package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-10-02 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmetteurDto {

	private String emtCode;
	
	private String emtLibelle;
	
	private String emtSyspay;
	
	private String emtbindebut;
	
	private String emtbinfin;
	
	private String emtCodeserv;

}
