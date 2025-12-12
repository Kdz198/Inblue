package fpt.org.inblue.service;

import fpt.org.inblue.model.Notification;

import java.util.List;

public interface NotificationService {
    public List<Notification> getNotifications(int userId);
    public Notification createNotification(Notification notification);
    ;
    public boolean checkRead(int notificationId);
}
