package app.ai.service.cv.extractortext.component;

import app.ai.service.cv.extractortext.Interface.IFileTextExtractor;
import app.ai.util.TextCleaningUtil;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class PDFTextExtractor implements IFileTextExtractor {
    
    // CẤU HÌNH
    private static final int NATIVE_PAGE_LIMIT = 5; // Đọc text thường: tối đa 5 trang
    private static final int OCR_PAGE_LIMIT = 3;    // Đọc OCR: tối đa 3 trang (vì OCR chậm)
    private static final int MIN_TEXT_LENGTH = 50;  // Ngưỡng tối thiểu để quyết định có phải scan hay không
    private static final int OCR_DPI = 300;         // Độ phân giải ảnh khi render (300 là chuẩn vàng cho OCR)
    
    private final TextCleaningUtil textCleaner;
    private final Tesseract tesseract;

    public PDFTextExtractor(TextCleaningUtil textCleaner, Tesseract tesseract) {
        this.textCleaner = textCleaner;
        this.tesseract = tesseract;
    }

    @Override
    public boolean supports(MultipartFile file) {
        // Kiểm tra nhanh đuôi file
        return file.getOriginalFilename() != null && 
               file.getOriginalFilename().toLowerCase().endsWith(".pdf");
    }

    /**
     * Phương thức chính điều phối luồng xử lý
     */
    @Override
    public String extractText(MultipartFile inputText) throws Exception {
        try {
            byte[] fileBytes = inputText.getBytes();
            try (PDDocument document = Loader.loadPDF(fileBytes)) {
                
                // Bước 1: Thử cách nhanh (Native Extraction)
                String text = tryExtractNativeText(document);
                
                // Bước 2: Kiểm tra xem có cần dùng OCR không
                if (isScannedContent(text)) {
                    // System.out.println("⚠️ Phát hiện PDF Scan. Chuyển sang chế độ OCR...");
                    return extractTextViaOCR(document);
                }

                // Nếu text ổn, làm sạch và trả về
                return textCleaner.clean(text);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý PDF: " + e.getMessage(), e);
        }
    }

    // --- CÁC HÀM PHỤ TRỢ (HELPER METHODS) ---

    /**
     * Logic đọc text thông thường (Sử dụng PDFBox Stripper)
     */
    private String tryExtractNativeText(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        
        // Chỉ đọc tối đa N trang đầu để tiết kiệm tài nguyên
        int pageCount = Math.min(document.getNumberOfPages(), NATIVE_PAGE_LIMIT);
        stripper.setEndPage(pageCount);
        
        // Có thể trả về null nếu PDF bị lỗi font, nên cần handle
        String text = stripper.getText(document);
        return text != null ? text : "";
    }

    /**
     * Logic quyết định xem nội dung này có phải là file Scan hay không.
     * Hiện tại: Dựa vào độ dài chuỗi ký tự lấy được.
     */
    private boolean isScannedContent(String text) {
        return text.trim().length() < MIN_TEXT_LENGTH;
    }

    /**
     * Logic điều phối việc OCR (Chuyển đổi PDF -> Ảnh -> Text)
     */
    private String extractTextViaOCR(PDDocument document) throws IOException, TesseractException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        StringBuilder ocrText = new StringBuilder();

        // Giới hạn số trang OCR
        int pagesToScan = Math.min(document.getNumberOfPages(), OCR_PAGE_LIMIT);

        for (int page = 0; page < pagesToScan; page++) {
            // Tách logic xử lý từng trang ra riêng -> Để sau này dễ dàng nâng cấp lên chạy song song (Multi-thread)
            String pageText = processPageAndOcr(pdfRenderer, page);
            ocrText.append(pageText).append("\n");
        }

        return textCleaner.clean(ocrText.toString());
    }

    /**
     * Xử lý OCR cho MỘT trang duy nhất.
     * Hàm này độc lập, nhận input là renderer và số trang -> Trả về text.
     */
    private String processPageAndOcr(PDFRenderer renderer, int pageIndex) throws IOException, TesseractException {
        // 1. Render trang PDF thành ảnh
        // Scale = 300 / 72 (72 là DPI gốc của PDF). DPI cao giúp OCR chính xác hơn.
        BufferedImage bim = renderer.renderImageWithDPI(pageIndex, OCR_DPI, ImageType.RGB);
        
        // 2. Thực hiện OCR trên tấm ảnh đó
        return tesseract.doOCR(bim);
    }
}