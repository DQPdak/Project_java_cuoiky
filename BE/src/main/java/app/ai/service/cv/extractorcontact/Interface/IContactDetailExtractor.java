package app.ai.service.cv.extractorcontact.Interface;

/**
 * Trích xuất thông tin liên hệ từ văn bản CV
 */
public interface IContactDetailExtractor {
   public String extract(String text);
}