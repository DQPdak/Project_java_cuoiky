package app.ai.Service.cv.ExtractorText.Interface;

import org.springframework.web.multipart.MultipartFile;

/** Interface định nghĩa các phương thức trích xuất văn bản từ file */

public interface IFileTextExtractor {
    boolean supports(MultipartFile file);   // nhận dạng loại file
    String extractText(MultipartFile file) throws Exception; // trích xuất văn bản

}
