package projekt.zespolowy.zero_waste.services.EducationalServices.Article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import projekt.zespolowy.zero_waste.dto.ArticleDTO;
import projekt.zespolowy.zero_waste.entity.EducationalEntities.Articles.Article;
import projekt.zespolowy.zero_waste.entity.EducationalEntities.Articles.ArticleCategory;
import projekt.zespolowy.zero_waste.mapper.ArticleMapper;
import projekt.zespolowy.zero_waste.repository.ArticleRepository;
import projekt.zespolowy.zero_waste.services.TagService;
import projekt.zespolowy.zero_waste.services.UserService;

import java.util.Optional;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final TagService tagService;
    private final UserService userService;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, ArticleMapper articleMapper, TagService tagService, UserService userService) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
        this.tagService = tagService;
        this.userService = userService;
    }

    @Override
    public Article createArticle(ArticleDTO articleDTO) {
        Article article = articleMapper.toEntity(articleDTO, tagService);
        article.setAuthor(userService.getCurrentUser());
        return articleRepository.save(article);
    }
    @Override
    public Article updateArticle(Long id, ArticleDTO articleDTO) {
        return articleRepository.findById(id).map(existingArticle -> {
            articleMapper.afterUpdateArticleFromDTO(articleDTO, existingArticle, tagService);
            return articleRepository.save(existingArticle);
        }).orElseThrow(() -> new RuntimeException("Article not found with id " + id));
    }
    @Override
    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Override
    public Page<Article> getArticlesByCategory(ArticleCategory category, Pageable pageable) {
        if (category != null) {
            return articleRepository.findByArticleCategory(category, pageable);
        } else {
            return articleRepository.findAll(pageable);
        }
    }

    @Override
    public Page<Article> getArticlesByTitle(String title, Pageable pageable) {
        if(title != null && !title.trim().isEmpty()){
            return articleRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            return articleRepository.findAll(pageable);
        }
    }

    @Override
    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public Page<Article> findByTags_NameIgnoreCase(String tagName, Pageable pageable) {
        return articleRepository.findByTags_NameIgnoreCase(tagName, pageable);
    }

    @Override
    public Page<Article> findArticles(ArticleCategory category, String title, String tagName, Pageable pageable) {
        Specification<Article> specification = Specification.where(null);
        if (category != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("articleCategory"), category));
        }
        if (title != null && !title.trim().isEmpty()) {
            specification = specification.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (tagName != null && !tagName.trim().isEmpty()) {
            specification = specification.and((root, query, cb) -> cb.equal(root.join("tags").get("name"), tagName));
        }
        return articleRepository.findAll(specification, pageable);
    }
}
