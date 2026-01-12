package app.candidate.model;

import app.auth.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "candidate_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết 1-1 với User (Mỗi user chỉ có 1 hồ sơ ứng viên)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String cvFilePath; // Đường dẫn file CV gốc (nếu lưu file)

    // --- Thông tin trích xuất từ AI ---

    // Lưu danh sách kỹ năng dưới dạng JSON để linh hoạt
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> skills;

    // Lưu kinh nghiệm làm việc (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> experiences;

    // Lưu học vấn (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> educations;

    @Column(columnDefinition = "TEXT")
    private String aboutMe; // Giới thiệu bản thân (AI có thể tóm tắt)

    private String phoneNumber;
    private String address;
    private String linkedInUrl;
    private String websiteUrl;
}