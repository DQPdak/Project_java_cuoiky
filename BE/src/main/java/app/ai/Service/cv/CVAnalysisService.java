package app.ai.Service.cv;

import org.apache.commons.math3.analysis.function.Exp;
import org.checkerframework.checker.units.qual.A;

/**
 * CHỨC NĂNG:
 * - Điều phối các services liên quan đến phân tích CV
 * 
 * Các service liên quan:
 * - CVTextExtractor: Phân tích cấu trúc CV, trích xuất thông tin cơ bản
 * - SkillExtractionService: Nhận diện và trích xuất kỹ năng từ CV
 * - ContactInfoExtractor: Trích xuất thông tin liên hệ (Email, SĐT)
 * - CVAnalysisService: Phân tích tổng thể CV, đánh giá mức độ phù hợp với vị trí tuyển dụng
 */

// import từ Spring Framework
import org.springframework.beans.factory.annotation.Autowired; // Annotation để tự động tiêm phụ thuộc
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring
import org.springframework.web.multipart.MultipartFile; // Lớp đại diện cho tệp tải lên trong HTTP request

import app.ai.Service.cv.*;
import app.ai.Service.cv.ExtractorContact.ContactInfoExtractor;
import app.ai.Service.cv.ExtractorText.CVTextExtractor;

// import từ java Core
import java.util.*;
import java.util.concurrent.*;

@Service
public class CVAnalysisService {

@Autowired
private CVTextExtractor cvTextExtractor; 

// @Autowired
// private CVAnalysisService cvAnalysisService;

@Autowired
private ContactInfoExtractor contactInfoExtractor;

// chưa xong
}
