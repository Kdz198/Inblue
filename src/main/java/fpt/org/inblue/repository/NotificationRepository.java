package fpt.org.inblue.repository;

import fpt.org.inblue.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByUser_Id(int userId);
}
