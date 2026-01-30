package app.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import app.admin.dto.response.AdminUserResponse;
import app.admin.service.AdminUserService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/admin/users")
// @PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Page<AdminUserResponse> getUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return adminUserService.getAllUsers(keyword, pageable);
    }

    @PutMapping("/{id}/lock")
    public void lockUser(@PathVariable Long id) {
        adminUserService.lockUser(id);
    }

    @PutMapping("/{id}/unlock")
    public void unlockUser(@PathVariable Long id) {
        adminUserService.unlockUser(id);
    }
}
