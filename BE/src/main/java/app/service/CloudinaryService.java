package app.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // Xác định loại file dựa vào contentType
            String contentType = file.getContentType();
            String resourceType;

            if (contentType != null && contentType.startsWith("image")) {
                resourceType = "image"; // ảnh
            } else {
                resourceType = "raw";   // PDF, DOCX, ZIP...
            }

            // Upload file lên Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "folder", "phantichcv/cv", 
                    "public_id", UUID.randomUUID().toString()
                )
            );

            // Trả về đường dẫn URL online (https://res.cloudinary.com/...)
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload file lên Cloudinary: " + e.getMessage());
        }
    }
}