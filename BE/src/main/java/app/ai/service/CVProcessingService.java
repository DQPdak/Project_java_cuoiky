package app.ai.service;

import app.ai.models.Candidate;
import app.ai.models.Experience;
import app.ai.repository.ICandidateRepository;
import app.ai.service.cv.extractortext.CVTextExtractor;
import app.ai.service.cv.gemini.GeminiService;
import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.ai.service.cv.gemini.dto.ExperienceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CVProcessingService {

    private final CVTextExtractor textExtractor;
    private final GeminiService geminiService;
    private final ICandidateRepository candidateRepository;

    @Transactional // Quan trọng: Đảm bảo lưu thành công cả 2 bảng hoặc không lưu gì cả
    public Candidate processAndSaveCV(MultipartFile file) {
        // B1: Tách chữ
        String rawText = textExtractor.extractText(file);

        // B2: Gọi AI lấy JSON
        GeminiResponse aiData = geminiService.parseCV(rawText);

        // B3: Kiểm tra trùng lặp (Optional)
        if (aiData.getContact() != null && candidateRepository.existsByEmail(aiData.getContact().getEmail())) {
            throw new RuntimeException("Ứng viên có email này đã tồn tại trong hệ thống!");
        }

        // B4: Map từ DTO (AI) sang Entity (Database)
        Candidate candidate = new Candidate();
        
        // Map Contact
        if (aiData.getContact() != null) {
            candidate.setEmail(aiData.getContact().getEmail());
            candidate.setPhoneNumber(aiData.getContact().getPhoneNumber());
        }

        // Map Skills
        candidate.setSkills(aiData.getSkills());

        // Map Experiences
        if (aiData.getExperiences() != null) {
            List<Experience> expEntities = new ArrayList<>();
            for (ExperienceDTO dto : aiData.getExperiences()) {
                Experience exp = new Experience();
                exp.setCompany(dto.getCompany());
                exp.setRole(dto.getRole());
                exp.setStartDate(dto.getStartDate());
                exp.setEndDate(dto.getEndDate());
                exp.setDescription(dto.getDescription()); 

                exp.setCandidate(candidate);
                expEntities.add(exp);
            }
            candidate.setExperiences(expEntities);
        }

        // B5: Lưu xuống DB
        return candidateRepository.save(candidate);
    }
}
