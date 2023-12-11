package ma.m2m.gateway.service;

import java.util.List;
import ma.m2m.gateway.dto.ArticleDGIDto;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

public interface ArticleDGIService {
	
	List<ArticleDGIDto> findArticleByIddemande(int iddemande);
	
	ArticleDGIDto findVraiArticleByIddemande(int iddemande);
	
	List<ArticleDGIDto> getArticlesByIddemandeSansFrais(int iddemande);

}
