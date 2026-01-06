package app.ai.service.cv.extractorContact;

/**
 * CHỨC NĂNG: Trích xuất thông tin liên hệ từ văn bản CV(Email, SĐT)
 */

// import từ Spring Framework
import org.springframework.stereotype.Service; // Annotation đánh dấu lớp Service trong Spring

import app.ai.service.cv.extractorContact.component.EmailExtractor;
import app.ai.service.cv.extractorContact.component.PhoneExtractor;
import app.ai.service.cv.extractorContact.Interface.IContactDetailExtractor;
import app.ai.service.cv.extractorContact.dto.ContactInfo;

@Service
public class ContactInfoExtractor {
    private  IContactDetailExtractor emailExtractor = new EmailExtractor();
    private  IContactDetailExtractor phoneExtractor = new PhoneExtractor();

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