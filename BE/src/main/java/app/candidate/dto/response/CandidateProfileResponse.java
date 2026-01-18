package app.candidate.dto.response;

import app.ai.service.cv.gemini.dto.ExperienceDTO;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CandidateProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String aboutMe;
    private String linkedInUrl;
    private String cvFilePath;
    
    // List này an toàn vì là List String/Object thường, không phải Hibernate Proxy
    private List<String> skills;
    private List<ExperienceDTO> experiences; 
}