package app.ai.util;

import org.springframework.stereotype.Component;

/**
 * CHỨC NĂNG:
 * - Chuẩn hóa văn bản thô sau khi trích xuất.
 * - Loại bỏ ký tự rác, khoảng trắng thừa.
 * * LÝ DO TÁCH FILE (SOLID):
 * - DRY (Don't Repeat Yourself): Tránh viết lại hàm cleanText trong mọi class Extractor.
 * - Dễ dàng sửa đổi quy tắc làm sạch text tại một nơi duy nhất.
 */
@Component
public class TextCleaningUtil {

    public String clean(String text) {
        if (text == null) {
            return "";
        }
        return text
            // Gộp nhiều khoảng trắng liên tiếp thành 1
            .replaceAll("\\s+", " ")
            // Chỉ giữ lại chữ cái, số và các ký tự câu cơ bản, loại bỏ ký tự lạ
            .replaceAll("[^\\p{L}\\p{N}\\s@.+\\-():/,]", "")
            // Chuẩn hóa xuống dòng: Nhiều hơn 3 dòng trống thì gom về 2 dòng
            .replaceAll("\n{3,}", "\n\n")
            // Cắt khoảng trắng đầu/cuối
            .trim();
    }
}