package app.content.service;

import app.content.dto.request.ArticleCreateRequest;
import app.content.dto.request.ArticleUpdateRequest;
import app.content.dto.response.ArticleResponse;
import app.content.model.Article;
import app.content.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository repo;

    private ArticleResponse toResponse(Article a) {
        return ArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .category(a.getCategory())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    // PUBLIC: chỉ thấy PUBLISHED
    public List<ArticleResponse> listPublished() {
        return repo.findByStatusOrderByCreatedAtDesc("PUBLISHED")
                .stream().map(this::toResponse).toList();
    }

    public ArticleResponse getPublishedById(Long id) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));
        if (!"PUBLISHED".equalsIgnoreCase(a.getStatus())) {
            throw new RuntimeException("Bài viết chưa được xuất bản");
        }
        return toResponse(a);
    }

    // ADMIN: thấy tất cả
    public List<ArticleResponse> listAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public ArticleResponse getById(Long id) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));
        return toResponse(a);
    }

    public ArticleResponse create(ArticleCreateRequest req) {
        Article a = Article.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.getCategory())
                .status(req.getStatus() == null ? "DRAFT" : req.getStatus())
                .build();
        return toResponse(repo.save(a));
    }

    public ArticleResponse update(Long id, ArticleUpdateRequest req) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));

        a.setTitle(req.getTitle());
        a.setContent(req.getContent());
        a.setCategory(req.getCategory());
        if (req.getStatus() != null) a.setStatus(req.getStatus());

        return toResponse(repo.save(a));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Không tìm thấy bài viết id=" + id);
        repo.deleteById(id);
    }
}
