package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CFDGIDto {

	private Integer idCFDGI;
	
	private String cF_R_OICodeclient;
	
	private String cF_R_OINReference;
	
	private String cF_R_OIConfirmUrl;
	
	private String cF_R_OIemail;
	
	private String cF_R_OIMtTotal;
	
	private String cF_R_OICodeOper;
	
	private String cF_R_OIUpdateURL;
	
	private String offerURL;
	
	private String cF_R_OIRefFacture;
	
	private int iddemande;
	
	private String refReglement;
	
	private String codeRtour;
	
	private String msg;
	
	private String refcanal;

}
