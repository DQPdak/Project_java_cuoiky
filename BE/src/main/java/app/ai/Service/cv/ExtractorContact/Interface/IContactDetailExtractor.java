package app.ai.Service.cv.ExtractorContact.Interface;

/**
 * Trích xuất thông tin liên hệ từ văn bản CV
 */
public interface IContactDetailExtractor {
   public String extract(String text);
}