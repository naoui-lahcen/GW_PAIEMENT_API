package ma.m2m.gateway.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ma.m2m.gateway.dto.ArticleDGIDto;
import ma.m2m.gateway.mappers.ArticleDGIMapper;
import ma.m2m.gateway.repository.ArticleDGIDao;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-12-11
 */

@Service
public class ArticleDGIServiceImpl implements ArticleDGIService {
	
	@Autowired
	ArticleDGIDao articleDGIDao;
	
	private ArticleDGIMapper articleDGIMapper = new ArticleDGIMapper();

	@Override
	public List<ArticleDGIDto> findArticleByIddemande(int iddemande) {
		return articleDGIMapper.modelList2VOList(articleDGIDao.findArticleByIddemande(iddemande));
	}

	@Override
	public ArticleDGIDto findVraiArticleByIddemande(int iddemande) {
		return articleDGIMapper.model2VO(articleDGIDao.findVraiArticleByIddemande(iddemande));
	}
	
	@Override
	public List<ArticleDGIDto> getArticlesByIddemandeSansFrais(int iddemande) {
		return articleDGIMapper.modelList2VOList(articleDGIDao.getArticlesByIddemandeSansFrais(iddemande));
	}
}
