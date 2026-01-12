package app.ai.service.cv.extractortext.Interface;
import org.springframework.web.multipart.MultipartFile;

public interface IFileTextExtractor {
    boolean supports(MultipartFile file);
    String extractText(MultipartFile file) throws Exception;
}