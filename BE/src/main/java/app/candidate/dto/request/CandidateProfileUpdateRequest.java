package app.candidate.dto.request;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CandidateProfileUpdateRequest {
    private String aboutMe;
    private String phoneNumber;
    private String address;
    private String linkedInUrl;
    private String websiteUrl;
    private List<String> skills;
    private List<Map<String, Object>> experiences;
    private List<Map<String, Object>> educations;
}