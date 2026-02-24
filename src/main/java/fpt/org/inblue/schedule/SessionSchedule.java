package fpt.org.inblue.schedule;

import fpt.org.inblue.model.Notification;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.NotificationRepository;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionSchedule {
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void sendTodayMeetingNotification(){
        List<Session> sessions = sessionRepository.findAll();
        for(Session session : sessions){
            if(session.getJoinTime().toLocalDateTime().toLocalDate().isEqual(java.time.LocalDate.now())){
                Notification notification = new Notification();
                User user = userRepository.findById(session.getUserId()).orElse(null);
                if(user!=null){
                    notification.setUser(user);
                    notification.setTitle("Thông bá cuộc họp hôm nay");
                    notification.setMessage("Bạn có cuộc họp với mentor vào lúc "+session.getJoinTime()+". Hãy chuẩn bị và tham gia đúng giờ nhé!");
                    notificationRepository.save(notification);
                }
            }
        }
    }

}
