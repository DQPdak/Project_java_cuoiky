package app.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // 1. Lấy tên file gốc (ví dụ: "CV_NguyenVanA.pdf")
            String originalFileName = file.getOriginalFilename();
            
            // Xử lý trường hợp tên file bị null (ít khi xảy ra nhưng nên có)
            if (originalFileName == null) {
                originalFileName = "cv_file"; 
            }

            // 2. Upload với tham số public_id được set cứng bằng tên file gốc
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", "auto",       // Tự động nhận diện loại file
                    "public_id", originalFileName, // ✅ QUAN TRỌNG: Ép dùng tên file gốc làm ID (để giữ đuôi .pdf/.docx)
                    "unique_filename", true,       // Thêm ký tự ngẫu nhiên để tránh trùng lặp
                    "folder", "phantichcv/cv"
                )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary: " + e.getMessage());
        }
    }
}