package app.ai.Service.cv.skill.Component;

/**
 * CHỨC NĂNG:
 * - Tạo 1 database kỹ năng mẫu để phục vụ việc trích xuất kỹ năng từ CV
 * - database này có thể mở rộng, bổ sung thêm kỹ năng mới trong tương lai
 * - đây không phải data theo kiểu sql mà chỉ là 1 class khái niệm lưu trữ kỹ năng tạm thời
 */

import org. springframework.stereotype.Component;
import java.util.*;

@Component
public class SkillDatabase {
    // Map lưu trữ danh mục kỹ năng và các kỹ năng tương ứng
    private final Map<String, Set<String>> skillCategories;

    // Khởi tạo database kỹ năng mẫu
    public SkillDatabase() {
        this.skillCategories = new HashMap<>(); // Tạo 1 HashMap rỗng để lưu trữ kỹ năng
        initializeSkillDatabase();
    }

    // Phương thức khởi tạo dữ liệu mẫu với các kỹ năng phổ biến
    /**
     * Ý TƯỞNG:
     *  - thêm vào skillCategories các Category 
     *  - thêm vào Category các skill tương ứng
     */
    private void initializeSkillDatabase() {
       // Kỹ năng lập trình
        skillCategories.put("PROGRAMMING", new HashSet<>(Arrays.asList(
                "Java", "Python", "C++", "JavaScript", "Ruby", "Go", "Swift", "Kotlin"
        )));

        // Frameworks và thư viện phổ biến
        skillCategories.put("FRAMEWORK", new HashSet<>(Arrays.asList(
                "spring boot", "spring", "django", "flask", "fastapi",
            "react", "reactjs", "react.js", "angular", "vue", "vue.js",
            "node.js", "nodejs", "express", "nestjs", 
            ".net", "asp.net", "laravel", "rails", "hibernate",
            "flutter", "react native"
        )));

        // Kỹ năng sử dụng cơ sở dữ liệu
        skillCategories.put("DATABASE", new HashSet<>(Arrays.asList(
                "mysql", "postgresql", "postgres", "mongodb", "redis",
            "oracle","sql server", "mssql", "sqlite","elasticsearch",
            "cassandra", "dynamodb", "firebase","mariadb", "couchdb"
        )));

        // Kỹ năng thiết kế đồ họa
        skillCategories.put("DESIGN", new HashSet<>(Arrays.asList(
                "Adobe Photoshop", "Adobe Illustrator", "Figma", "Sketch", "InDesign"
        )));

        // Kỹ năng Sử dụng công cụ và phần mềm hỗ trợ
        skillCategories.put("TOOLS", new HashSet<>(Arrays.asList(
                "git", "github", "gitlab", "jira", "confluence",
            "postman", "swagger", "maven", "gradle", "npm", "webpack"
        )));

        // Kỹ năng mềm
        skillCategories.put("SOFT_SKILLS", new HashSet<>(Arrays.asList(
                "teamwork", "leadership", "communication", "problem solving",
            "agile", "scrum", "kanban", "project management",
            "critical thinking", "time management"
        )));     
}
    // Phương thức lấy toàn bộ kỹ năng trong 1 danh mục
    /**
     * Ý TƯỞNG:
     * Lấy các giá trị HashSet từ Map dựa trên khóa danh mục
     * Nếu danh mục không tồn tại, trả về một HashSet rỗng
     * 
     */
    public Set<String> getSkillsByCategory(String category) {
        return skillCategories.getOrDefault(category, new HashSet<>());
    }

    // Phương thức lấy toàn bộ danh mục kỹ năng
    public Set<String> getAllCategories() {
        return skillCategories.keySet();
    }

    // Phương thức lấy toàn bộ kỹ năng trong database
    public Map<String, Set<String>> getAllSkills() {
        return new HashMap<>(skillCategories);
    }

    // Kiểm tra skill có tồn tại trong database không
    public boolean containsSkill(String skill) {
        String lowerSkill = skill.toLowerCase();
        // vòng lập này sẽ xét qua từng tập hợp Set<String> trong skillCategories
        for (Set<String> skills : skillCategories.values()){
            if (skills.contains(lowerSkill)) // contains dùng để kiểm tra xem lowerSkill có trong tập hợp Set<String> không
                {
                return true;
            }
        }
        return false;
    }

    // Tìm category của 1 skill
    public String getCategoryOfSkill(String skill){
        String lowerSkill = skill.toLowerCase();
// entrySet() là lấy từng cập <Key, Values> trong biến Map ra
// Map.Entry<String, Set<String>> là 1 biến map gồm 1 cặp <Key, Values>
        for (Map.Entry<String, Set<String>> entry : skillCategories.entrySet()){
            if (entry.getValue().contains(lowerSkill)){
                return entry.getKey();
            }
        }
        return null;
    }

    // thêm 1 skill mới vào Category có sẵn
    // computeIfAbsent(Category, k -> new HashSet<>()) kiểm tra xem key có tồn tại nếu không thì tạo 1 values mới thuộc key đó
    // add thêm giá trị vào values mới đó 
    public void addSkill(String Category, String skill){
        skillCategories.computeIfAbsent(Category, k -> new HashSet<>()).add(skill.toLowerCase());
    }

     // Thêm nhiều skills cùng lúc
     public void addSkills(String category, Set<String> skills){
        // Tạo thêm chiểu cho skillCategories.computeIfAbsent(category, k -> new HashSet<>()) để khi cập nhật value cho categorySkills thì skillCategories với key category cũng được cập nhật 
        Set<String> categorySkills = skillCategories.computeIfAbsent(category, k -> new HashSet<>());
        // xét từng skill trong skills rồi thêm các skill được xét đó ở dạng chữ viết thường vào categorySkills
        skills.forEach(skill -> categorySkills.add(skill.toLowerCase()));
     }

// Xóa skill khỏi database
public boolean removeSkill(String skill){
    String lowerSkill = skill.toLowerCase();

    for(Set<String> Skills : skillCategories.values()){
        if (Skills.remove(lowerSkill)){
            return true;
        }
    }
    return false;
}

}