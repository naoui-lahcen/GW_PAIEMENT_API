package ma.m2m.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

@Entity
@Table(name="CFDGI")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CFDGI implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idCFDGI")
	private Integer idCFDGI;
	
	@Column(name="cF_R_OICodeclient")
	private String cF_R_OICodeclient;
	
	@Column(name="cF_R_OINReference")
	private String cF_R_OINReference;
	
	@Column(name="cF_R_OIConfirmUrl")
	private String cF_R_OIConfirmUrl;
	
	@Column(name="cF_R_OIemail")
	private String cF_R_OIemail;
	
	@Column(name="cF_R_OIMtTotal")
	private String cF_R_OIMtTotal;
	
	@Column(name="cF_R_OICodeOper")
	private String cF_R_OICodeOper;
	
	@Column(name="cF_R_OIUpdateURL")
	private String cF_R_OIUpdateURL;
	
	@Column(name="offerURL")
	private String offerURL;
	
	@Column(name="cF_R_OIRefFacture")
	private String cF_R_OIRefFacture;
	
	@Column(name="id_demande")
	private int iddemande;
	
	@Column(name="refReglement")
	private String refReglement;
	
	@Column(name="codeRtour")
	private String codeRtour;
	
	@Column(name="msg")
	private String msg;
	
	@Column(name="refcanal")
	private String refcanal;

}
