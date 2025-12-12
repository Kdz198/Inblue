package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.Notification;
import fpt.org.inblue.repository.NotificationRepository;
import fpt.org.inblue.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotifications(int userId) {
        //nếu !=0 thì là tất cả tb của user còn nếu = 0 thì là thông báo của admin
        if(userId!=0){
            return notificationRepository.findAllByUser_Id(userId);
        }

        else{
            return notificationRepository.findAllByUser_Id(0);
        }
    }

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public boolean checkRead(int notificationId) {
        if(notificationRepository.existsById(notificationId)){
            Notification notification = notificationRepository.findById(notificationId).get();
            notification.setRead(true);
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }
}
