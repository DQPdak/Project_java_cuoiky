// package app.admin.service.impl;

// import jakarta.persistence.EntityNotFoundException;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;

// import app.admin.dto.response.AdminUserResponse;
// import app.admin.entity.User;
// import app.admin.entity.UserStatus;
// import app.admin.repository.UserRepository;
// import app.admin.service.AdminUserService;

// @Service
// public class AdminUserServiceImpl implements AdminUserService {

//     @Autowired
//     private UserRepository userRepository;

//     @Override
// public Page<AdminUserResponse> getAllUsers(String keyword, Pageable pageable) {

//     Page<User> usersPage = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);

//     return usersPage.map(user -> AdminUserResponse.builder()
//             .id(user.getId())
//             .fullName(user.getFullName())
//             .email(user.getEmail())
//             .role(user.getRole())
//             .status(user.getStatus())
//             .createdAt(user.getCreatedAt())
//             .build());
// }

//     @Override
//     public void lockUser(Long userId) {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
//         user.setStatus(UserStatus.LOCKED);
//         userRepository.save(user);
//     }

//     @Override
//     public void unlockUser(Long userId) {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
//         user.setStatus(UserStatus.ACTIVE);
//         userRepository.save(user);
//     }
// }
