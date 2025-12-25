package app.ai.Service.cv.ExtractorContact;

/**
 * CHỨC NĂNG: Trích xuất thông tin liên hệ từ văn bản CV(Email, SĐT)
 */

// import từ Spring Framework
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring

import app.ai.Service.cv.ExtractorContact.Component.EmailExtractor;
import app.ai.Service.cv.ExtractorContact.Component.PhoneExtractor;
import app.ai.dto.ContactInfo;

@Service
public class ContactInfoExtractor {
    private final EmailExtractor emailExtractor;
    private final PhoneExtractor phoneExtractor;

    public ContactInfoExtractor(EmailExtractor emailExtractor, PhoneExtractor phoneExtractor) {
        this.emailExtractor = emailExtractor;
        this.phoneExtractor = phoneExtractor;
    }
    /**
     * Phương thức trích xuất thông tin liên hệ (Email, SĐT) từ văn bản CV
     */
    public ContactInfo extract (String cvText){
        ContactInfo info = new ContactInfo();

        info.setEmail(emailExtractor.extract(cvText));
        info.setPhoneNumber(phoneExtractor.extract(cvText));
        return info;
    }

    
}