package app.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import app.admin.dto.response.AdminUserResponse;

public interface AdminUserService {
    Page<AdminUserResponse> getAllUsers(String keyword, Pageable pageable);
    void lockUser(Long userId);
    void unlockUser(Long userId);
}
