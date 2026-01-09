package app.ai.service.cv.skill.component.matching.dto;
/**
 * CHỨC NĂNG: Trả về kết quả khi so sánh 2 danh sách Skill
 */

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: SkillMatchResult
 * Chức năng: Là "Hợp đồng dữ liệu" trả về cho Frontend.
 * Giúp người dùng biết rõ họ mạnh chỗ nào, yếu chỗ nào để cải thiện CV.
 */
@Data // Tự động tạo Getter, Setter, toString, equals, hashCode
@AllArgsConstructor // Tạo constructor đầy đủ tham số để khởi tạo nhanh trong Service
@NoArgsConstructor  // Tạo constructor trống (cần thiết cho một số thư viện JSON)
@Builder            // Giúp khởi tạo đối tượng theo phong cách thiết kế hiện đại
public class SkillMatchResult {
    private Set<String> matchedSkills; // Danh sách kỹ năng khớp
    private Set<String> missingSkills; // Danh sách kỹ năng thiếu
    private Set<String> extraSkills;   // Danh sách kỹ năng thừa
    private int matchCount;          // Số kỹ năng khớp
    private int totalRequired;      // Tổng số kỹ năng yêu cầu
    private double matchPercentage; // Tỷ lệ phần trăm khớp
    @Override
    public String toString() {
        return String.format(
            "--- BÁO CÁO KỸ NĂNG ---\n" +
            "Tỷ lệ khớp: %.2f%%\n" +
            "Số lượng: %d/%d kỹ năng\n" +
            "Thiếu: %s\n" +
            "----------------------",
            matchPercentage, matchCount, totalRequired, missingSkills
        ); 
    }
}

