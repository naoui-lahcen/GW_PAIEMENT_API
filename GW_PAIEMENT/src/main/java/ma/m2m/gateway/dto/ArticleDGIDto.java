package ma.m2m.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArticleDGIDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer idArticleDGI;
	
	private String uniqueID;
	
	private String name;
	
	private String price ;
	
	private String type;
	
	private String cF_R_COMMONE;
	
	private String commande;
	
	private int iddemande;

}
