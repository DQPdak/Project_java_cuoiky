package app.admin.service.impl;

import app.admin.dto.response.AdminUserResponse;
import app.admin.service.AdminUserService;
import app.auth.model.User;
import app.auth.model.enums.UserRole;
import app.auth.model.enums.UserStatus;
import app.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Long getCurrentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Không xác định được user đang đăng nhập");
        }

        String email = auth.getName(); // thường là email
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException("Không tìm thấy user theo email đăng nhập: " + email))
                .getId();
    }

    @Override
    public Page<AdminUserResponse> getAllUsers(String keyword, Pageable pageable) {
        Long excludeId = getCurrentAdminId();

        String k = (keyword == null) ? "" : keyword.trim();

        // ✅ loại trừ chính admin đang đăng nhập, vẫn search + paging
        Page<User> usersPage = userRepository.searchUsersExcludeId(excludeId, k, pageable);

        // ✅ dùng PageImpl để tránh lỗi type inference trong VS Code
        List<AdminUserResponse> responseList = usersPage.getContent().stream()
                .map(user -> AdminUserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .userRole(user.getUserRole()) // ✅ khớp DTO của bạn
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();

        return new PageImpl<>(responseList, pageable, usersPage.getTotalElements());
    }

    @Override
    public void lockUser(Long userId) {
        Long currentAdminId = getCurrentAdminId();
        if (userId.equals(currentAdminId)) {
            throw new IllegalStateException("Không thể tự khóa chính mình");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getUserRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Không thể khóa tài khoản ADMIN");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            return;
        }

        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long userId) {
        Long currentAdminId = getCurrentAdminId();
        if (userId.equals(currentAdminId)) {
            throw new IllegalStateException("Không thể tự mở khóa chính mình");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            return;
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
