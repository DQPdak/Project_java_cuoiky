package app.recruitment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import app.auth.model.User;
import app.auth.model.enums.UserRole;
import app.auth.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LƯU Ý:
 * - Hiện tại `User` trong workspace (see [`app.auth.entity.User`](BE/src/main/java/app/auth/entity/User.java))
 *   không có fields skills hoặc gpa. Vì vậy implement hiện tại hỗ trợ tìm theo tên + role=CANDIDATE.
 * - Để search theo skills/gpa, cần mở rộng schema (ví dụ table candidate_profiles.skills, candidate_profiles.gpa)
 *   hoặc sử dụng CVAnalysisService để trích xuất kỹ năng từ cvUrl (nếu lưu CV).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateSearchServiceImpl implements CandidateSearchService {

    private final UserRepository userRepository;

    @Override
    public List<User> searchCandidates(String skill, Double minGpa, String name) {
        // filter role = CANDIDATE
        List<User> candidates;
        if (name != null && !name.isBlank()) {
            candidates = userRepository.findByStatus(null) // placeholder call -> UserRepository hiện không có findByRoleAndName, dùng generic approach
                    .stream()
                    .filter(u -> u.getUserRole() == UserRole.CANDIDATE)
                    .filter(u -> u.getFullName() != null && u.getFullName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            candidates = userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == UserRole.CANDIDATE)
                    .collect(Collectors.toList());
        }

        // Skill / GPA filtering: chưa có trường lưu trữ trong User -> TODO: extend DB or use CVAnalysisService
        if (skill != null && !skill.isBlank()) {
            log.warn("Skill filter requested ('{}') but User.skills not present in schema. Returning name-filtered results only.", skill);
            // Optional: further filter by heuristic (e.g., candidate.fullName contains skill) - not implemented
        }
        if (minGpa != null) {
            log.warn("GPA filter requested ({}) but User.gpa not present in schema. Ignored.", minGpa);
        }
        return candidates;
    }
}