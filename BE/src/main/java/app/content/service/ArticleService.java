package app.content.service;

import app.content.dto.request.ArticleCreateRequest;
import app.content.dto.request.ArticlePublishRequest;
import app.content.dto.request.ArticleUpdateRequest;
import app.content.dto.response.ArticleResponse;
import app.content.model.Article;
import app.content.repository.ContentArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ContentArticleRepository repo;

    private ArticleResponse toResponse(Article a) {
        return ArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .slug(a.getSlug())
                .thumbnailUrl(a.getThumbnailUrl())
                .isPublished(a.getIsPublished())
                .authorId(a.getAuthorId())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    // ✅ Public: chỉ lấy bài đã publish
    public List<ArticleResponse> listPublic() {
        return repo.findByIsPublishedTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    // ✅ Public: lấy bài theo id nhưng chỉ nếu published
    public ArticleResponse getPublicById(Long id) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));
        if (!Boolean.TRUE.equals(a.getIsPublished())) {
            throw new RuntimeException("Bài viết chưa được xuất bản");
        }
        return toResponse(a);
    }

    // ✅ Admin: list tất cả
    public List<ArticleResponse> listAdmin() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    // ✅ Admin: get bất kỳ
    public ArticleResponse getAdminById(Long id) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));
        return toResponse(a);
    }

    // ✅ Admin: create
    public ArticleResponse create(ArticleCreateRequest req) {
        String slug = (req.getSlug() == null || req.getSlug().isBlank())
                ? toSlug(req.getTitle())
                : req.getSlug();

        Article a = Article.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .slug(slug)
                .thumbnailUrl(req.getThumbnailUrl())
                .isPublished(req.getIsPublished() != null && req.getIsPublished())
                .authorId(req.getAuthorId())
                .build();

        return toResponse(repo.save(a));
    }

    // ✅ Admin: update
    public ArticleResponse update(Long id, ArticleUpdateRequest req) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));

        a.setTitle(req.getTitle());
        a.setContent(req.getContent());

        if (req.getSlug() != null && !req.getSlug().isBlank()) {
            a.setSlug(req.getSlug());
        } else if (a.getSlug() == null || a.getSlug().isBlank()) {
            a.setSlug(toSlug(req.getTitle()));
        }

        a.setThumbnailUrl(req.getThumbnailUrl());

        if (req.getIsPublished() != null) {
            a.setIsPublished(req.getIsPublished());
        }

        if (req.getAuthorId() != null) {
            a.setAuthorId(req.getAuthorId());
        }

        return toResponse(repo.save(a));
    }

    // ✅ Admin: publish/unpublish nhanh
    public ArticleResponse setPublish(Long id, ArticlePublishRequest req) {
        Article a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));
        a.setIsPublished(req.isPublished());
        return toResponse(repo.save(a));
    }

    // ✅ Admin: delete
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bài viết id=" + id);
        }
        repo.deleteById(id);
    }

    // Helper: tạo slug từ title (không dấu, gạch ngang)
    private String toSlug(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("[^a-z0-9\\s-]", "");
        s = s.replaceAll("\\s+", "-");
        s = s.replaceAll("-{2,}", "-");
        return s;
    }
}
