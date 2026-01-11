package app.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import app.ai.service.cv.CVAnalysisService;
import app.ai.service.cv.dto.CVAnalysisResult;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cv")
public class CVAnalysisController {

    @Autowired
    private CVAnalysisService cvAnalysisService;

    /**
     * API Phân tích CV toàn diện (AI-Powered)
     * ---------------------------------------------------
     * Method: POST
     * URL: http://localhost:8080/api/cv/analyze
     * Body (form-data):
     * - file: (File CV - PDF/Word/Image)
     * - jobDescription: (Text - Mô tả công việc để so khớp)
     * - targetRole: (Text - Vị trí ứng tuyển, VD: Java Backend)
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeCV(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false, defaultValue = "") String jobText,
            @RequestParam(value = "targetRole", required = false, defaultValue = "Candidate") String targetRole
    ) {
        try {
            // 1. Validate đầu vào
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng upload file CV!"));
            }

            // 2. Gọi Service xử lý (Đây là hàm tổng hợp mọi trí tuệ nãy giờ của bạn)
            // System.out.println("--- Bắt đầu phân tích CV: " + file.getOriginalFilename() + " ---");
            CVAnalysisResult result = cvAnalysisService.analyzeComprehensive(file, jobText, targetRole);

            // 3. Trả về kết quả JSON
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi ra console để debug
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi xử lý CV: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}