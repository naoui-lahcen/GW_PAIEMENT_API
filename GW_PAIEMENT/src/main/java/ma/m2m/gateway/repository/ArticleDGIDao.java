package ma.m2m.gateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ma.m2m.gateway.model.ArticleDGI;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

@Repository
public interface ArticleDGIDao extends JpaRepository<ArticleDGI, Long> {
	
	List<ArticleDGI> findArticleByIddemande(int iddemande);

	@Query(value = "select * from MXGATEWAY.ArticleDGI articleDGI where "
			+"WHERE articleDGI.uniqueID not like '%111111111111%' and articleDGI.iddemande = (?1) limit 1 ", nativeQuery = true)
	ArticleDGI findVraiArticleByIddemande(int iddemande);
	
	@Query(value = "select * from MXGATEWAY.ArticleDGI articleDGI where "
			+"WHERE articleDGI.uniqueID not like '%111111111111%' and articleDGI.iddemande = (?1) ", nativeQuery = true)
	List<ArticleDGI> getArticlesByIddemandeSansFrais(int iddemande);
	
}
