package app.notification.repository;

import app.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Lấy danh sách thông báo của user, sắp xếp mới nhất lên đầu
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Đếm số lượng thông báo chưa đọc (nếu bạn muốn hiển thị số badge chính xác từ DB)
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}