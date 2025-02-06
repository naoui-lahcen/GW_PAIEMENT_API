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
@Table(name="ArticleDGI")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArticleDGI implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idArticleDGI")
	private Integer idArticleDGI;
	
	@Column(name="uniqueID")
	private String uniqueID;
	
	@Column(name="name")
	private String name;
	
	@Column(name="price")
	private String price ;
	
	@Column(name="type")
	private String type;
	
	@Column(name="cF_R_COMMONE")
	private String cF_R_COMMONE;
	
	@Column(name="commande")
	private String commande;
	
	@Column(name="id_demande")
	private int iddemande;

}
