package app.candidate.service;

import app.ai.service.cv.CVAnalysisService;
import app.ai.service.cv.dto.CVAnalysisResult;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.ai.service.cv.skill.component.scoring.dto.SkillScore; // Import SkillScore để map
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CVAnalysisService cvAnalysisService;

    @Transactional
    public CandidateProfile uploadAndAnalyzeCV(Long userId, MultipartFile file) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // KHÔNG CẦN TẠO FILE TẠM NỮA -> Truyền thẳng MultipartFile
        log.info("Starting CV analysis for user: {}", user.getEmail());
        CVAnalysisResult analysisResult = cvAnalysisService.analyzeCV(file);

        // Lấy hoặc tạo mới Profile
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder().user(user).build());

        updateProfileFromAnalysis(profile, analysisResult);

        return candidateProfileRepository.save(profile);
    }

    private void updateProfileFromAnalysis(CandidateProfile profile, CVAnalysisResult result) {
        if (result.getContactInfo() != null) {
            profile.setPhoneNumber(result.getContactInfo().getPhoneNumber());
        }

        if (result.getSkills() != null) {
            List<String> skillNames = result.getSkills().stream()
                    .map(SkillScore::getSkillName)
                    .collect(Collectors.toList());
            profile.setSkills(skillNames);
        }

        if (result.getExperience() != null) {
            List<Map<String, Object>> expList = new ArrayList<>();
            Map<String, Object> expMap = new HashMap<>();
            expMap.put("totalYears", result.getExperience().getTotalYears());
            expMap.put("level", result.getExperience().getLevel());
            expList.add(expMap);
            profile.setExperiences(expList);
        }

        if (profile.getAboutMe() == null && result.getRawText() != null) {
            String raw = result.getRawText();
            profile.setAboutMe(raw.substring(0, Math.min(raw.length(), 500)) + "...");
        }
    }

    public CandidateProfile getProfile(Long userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}