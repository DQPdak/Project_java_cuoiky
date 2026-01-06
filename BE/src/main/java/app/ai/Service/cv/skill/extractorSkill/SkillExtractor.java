package app.ai.service.cv.skill.extractorSkill;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * CHỨC NĂNG:
 * Trích xuất kỹ năng từ văn bản CV
 * So khớp kỹ năng CV dựa trên yêu cầu công việc
 * Gợi ý kỹ năng liên quan để bổ sung vào CV
 * 
 * Ý TƯỞNG:
 * - Keyword Matching: So sánh text với database kỹ năng
 * - Fuzzy Matching: Xử lý lỗi chính tả, biến thể từ ngữ
 * - Categorization: Phân loại kỹ năng theo nhóm (kỹ năng cứng, kỹ năng mềm)
 * 
 */

// import từ Spring Framework
import org.springframework.stereotype.Service; // Đánh dấu lớp này là một service trong Spring Boot

import app.ai.service.cv.skill.extractorSkill.component.SkillDatabase;
import app.ai.service.cv.skill.extractorSkill.component.SkillFormatter;

// import từ java Core
import java.util.*;
/**
 * CHỨC NĂNG:
 * - Sử dụng SkillDatabase để lấy danh sách kỹ năng mẫu
 * - Sử dụng SkillFormatter để chuẩn hóa định dạng kỹ năng
 */

@Service
public class SkillExtractor {

    @Autowired
    private SkillDatabase skillDatabase;

    @Autowired
    private SkillFormatter skillFormatter;

    // Trích xuất tất cả kỹ năng từ văn bản CV và trả về danh sách đã chuẩn hóa
    public List<String> extract(String text){
        if (text == null || text.isEmpty()){return new ArrayList<>();}
        
        Set<String> foundSkill = new HashSet<>();
        // Chuẩn hoa văn bản đầu vào để tìm kiếm không phân biệt hoa thường/ký tự đặc biệt
        String textToSearch = skillFormatter.normalize(text);

        // Duyệt qua toàn bộ Catergory skill trong database
        for(String category : skillDatabase.getAllCategories()){
            for(String skill : skillDatabase.getSkillsByCategory(category)){
                String normalizedSkill = skillFormatter.normalize(skill);
                // Kiểm tra nếu kỹ năng có trong văn bản CV
                if(textToSearch.contains(normalizedSkill)){
                    // Thêm kỹ năng đã chuẩn hóa vào tập kết quả
                    foundSkill.add(skillFormatter.format(skill));
                }
            }
        }
        return new ArrayList<>(foundSkill);
    }

    // Trích xuất kỹ năng theo danh mục cụ thể

    public Map<String, Set<String>> extractByCategory(String text){
        if (text == null || text.isEmpty()){return new HashMap<>();}

        Map<String, Set<String>> result = new HashMap<>();
        String textToSearch = skillFormatter.normalize(text);

        for(String category : skillDatabase.getAllCategories()){
            Set<String> skillsInCategory = new HashSet<>();
            for(String skill : skillDatabase.getSkillsByCategory(category)){
                String normalizedSkill = skillFormatter.normalize(skill);
                if(textToSearch.contains(normalizedSkill)){
                    skillsInCategory.add(skillFormatter.format(skill));
                }
            }
            if(!skillsInCategory.isEmpty()){
                result.put(category, skillsInCategory);
            }
        }
        return result;
    }
}
