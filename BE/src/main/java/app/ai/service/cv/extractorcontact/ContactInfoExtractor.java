package app.ai.service.cv.extractorcontact;

import org.springframework.stereotype.Service;
import app.ai.service.cv.extractorcontact.Interface.IContactDetailExtractor;
import app.ai.service.cv.extractorcontact.component.EmailExtractor;
import app.ai.service.cv.extractorcontact.component.PhoneExtractor;
import app.ai.service.cv.extractorcontact.dto.ContactInfo;

@Service
public class ContactInfoExtractor {
    
    // Sử dụng Interface để đảm bảo tính lỏng lẻo (Loose Coupling)
    private final IContactDetailExtractor emailExtractor;
    private final IContactDetailExtractor phoneExtractor;

    // Spring sẽ tự động tiêm (Inject) các Component EmailExtractor và PhoneExtractor vào đây
    public ContactInfoExtractor(EmailExtractor emailExtractor, PhoneExtractor phoneExtractor) {
        this.emailExtractor = emailExtractor;
        this.phoneExtractor = phoneExtractor;
    }

    /**
     * Phương thức trích xuất thông tin liên hệ (Email, SĐT) từ văn bản CV
     */
    public ContactInfo extract(String cvText) {
        ContactInfo info = new ContactInfo();

        // Gọi các extractor con để xử lý
        info.setEmail(emailExtractor.extract(cvText));
        info.setPhoneNumber(phoneExtractor.extract(cvText));
        
        return info;
    }
}