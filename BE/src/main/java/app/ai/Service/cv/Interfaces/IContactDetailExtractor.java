package app.ai.Service.cv.Interfaces;

/**
 * Trích xuất thông tin liên hệ từ văn bản CV
 */
public interface IContactDetailExtractor {
   public String extract(String text);
}