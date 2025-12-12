package fpt.org.inblue.repository;

import fpt.org.inblue.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByUser_Id(int userId);
}
