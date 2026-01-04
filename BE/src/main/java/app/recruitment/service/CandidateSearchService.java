package app.recruitment.service;

import app.auth.entity.User;

import java.util.List;

public interface CandidateSearchService {
/**
* Tìm candidate theo tên / kỹ năng / GPA (nếu có)
* Note: hiện workspace không chứa trực tiếp field GPA hoặc skills trong User entity.
* Implement cơ bản theo tên; phần skill/GPA cần mở rộng DB (profile) hoặc sử dụng AI CV analysis.
*/
List<User> searchCandidates(String skill, Double minGpa, String name);
}