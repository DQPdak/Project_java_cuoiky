package app.content.controller;

import app.auth.dto.response.MessageResponse; // nếu đỏ -> sửa đúng package MessageResponse của bạn
import app.content.dto.request.ArticleCreateRequest;
import app.content.dto.request.ArticleUpdateRequest;
import app.content.dto.response.ArticleResponse;
import app.content.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService service;

    // GET /api/admin/articles  (admin: list all)
    @GetMapping
    public ResponseEntity<MessageResponse> listAll() {
        List<ArticleResponse> data = service.listAll();
        return ResponseEntity.ok(MessageResponse.success("Danh sách bài viết (admin)", data));
    }

    // GET /api/admin/articles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getById(@PathVariable Long id) {
        ArticleResponse data = service.getById(id);
        return ResponseEntity.ok(MessageResponse.success("Chi tiết bài viết (admin)", data));
    }

    // POST /api/admin/articles
    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody ArticleCreateRequest req) {
        ArticleResponse data = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.success("Tạo bài viết thành công", data));
    }

    // PUT /api/admin/articles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ArticleUpdateRequest req) {
        ArticleResponse data = service.update(id, req);
        return ResponseEntity.ok(MessageResponse.success("Cập nhật bài viết thành công", data));
    }

    // DELETE /api/admin/articles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(MessageResponse.success("Xóa bài viết thành công"));
    }
}
