package app.content.controller;

import app.auth.dto.response.MessageResponse; // nếu đỏ -> sửa đúng package MessageResponse của bạn
import app.content.dto.response.ArticleResponse;
import app.content.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService service;

    // GET /api/articles  (public)
    @GetMapping
    public ResponseEntity<MessageResponse> listPublished() {
        List<ArticleResponse> data = service.listPublished();
        return ResponseEntity.ok(MessageResponse.success("Danh sách bài viết (public)", data));
    }

    // GET /api/articles/{id} (public)
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getPublishedById(@PathVariable Long id) {
        ArticleResponse data = service.getPublishedById(id);
        return ResponseEntity.ok(MessageResponse.success("Chi tiết bài viết (public)", data));
    }
}
