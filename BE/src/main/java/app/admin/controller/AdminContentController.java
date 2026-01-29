package app.admin.controller;

import app.admin.dto.request.RejectJobPostingRequest;
import app.admin.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping("/pending")
    public ResponseEntity<?> pending(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminContentService.getPending(pageable));
    }

    @GetMapping("/posts")
    public ResponseEntity<?> posts(@RequestParam(defaultValue = "ALL") String status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminContentService.getAll(status, pageable));
    }

    @PutMapping("/posts/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, Authentication auth) {
        Long adminId = 1L; // TODO: lấy từ principal/JWT
        adminContentService.approve(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/posts/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                   @Valid @RequestBody RejectJobPostingRequest req,
                                   Authentication auth) {
        Long adminId = 1L; // TODO: lấy từ principal/JWT
        adminContentService.reject(id, adminId, req.reason());
        return ResponseEntity.ok().build();
    }
}
