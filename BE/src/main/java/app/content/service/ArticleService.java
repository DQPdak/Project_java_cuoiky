package app.content.service;

import app.content.model.Article;
import app.content.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository repo;

    public List<Article> getPublished() {
        return repo.findByStatusOrderByCreatedAtDesc("PUBLISHED");
    }

    public Article create(Article article) {
        return repo.save(article);
    }
}
