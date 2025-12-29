package app.ai.Service.cv.skill.Component;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.config.SkillDisplayConfig;

/**
 * CHỨC NĂNG: Chuẩn hóa định dạng tên Kỹ Năng
 * + Định dạng hiển thị thống nhất (ví dụ: quocte -> Quốc Tế)
 * + Chuẩn hóa so sánh (ví dụ: Java, java, JAVA -> java)
 * + Kiểm tra biến thể: Xác định "VueJS" và "Vue.js" là cùng một loại.
 */
@Component
public class SkillFormatter {
    /**
     * Chuyển đổi tên skill về định dạng hiển thị chuẩn.
     * Quy trình: 
     * 1. Kiểm tra trong file skill-format.yml (special-cases).
     * 2. Nếu không có, áp dụng các quy tắc thông minh (Regex).
     * 3. Cuối cùng, viết hoa chữ cái đầu.
     */
    @Autowired
    private SkillDisplayConfig skillConfig;

    public String format(String skill){
        if(skill == null || skill.isEmpty()){
            return skill;
        }

        String lowerSkill = skill.toLowerCase().trim();
        // Bước 1: Kiểm tra trong file cấu hình special-cases
        if(skillConfig.getSpecialCases() != null && skillConfig.getSpecialCases().containsKey(lowerSkill)){
            return skillConfig.getSpecialCases().get(lowerSkill);
        }

        // Bước 2: Áp dụng các quy tắc thông minh cho các từ chưa có trong file cấu hình
         // Nếu là ký tự viết tắt (<= 3 ký tự), viết hoa toàn bộ
        if(lowerSkill.length() <= 3){
            return lowerSkill.toUpperCase();
        }
         // Nếu kết thúc bằng JS mà không có dấu chấm, thêm dấu chấm
        if(lowerSkill.endsWith("js") && !lowerSkill.contains(".")){
            String name = lowerSkill.substring(0, lowerSkill.length() - 2);
            return capitalizeFirstLetter(name) + ".js";
        }
        // Bước 3: Viết hoa chữ cái đầu
        return capitalizeFirstLetter(lowerSkill);
}
    /**
     * Chuẩn hóa text so sánh (Xóa khoảng trắng, dấu chấm, gạch ngang).
     */
    public String normalize(String input){
        if(input == null){
            return "";
        }
        return input.toLowerCase().replaceAll("[.\\s-]", "").trim();
    }
    /**
     * kiểm tra biến thể giữa 2 chuỗi skill
     */
    public boolean isVariation(String skillA, String skillB){
        return normalize(skillA).equals(normalize(skillB));
    }

    // Viết hoa chữ cái đầu của một từ
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase();
    }
}