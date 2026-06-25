package fpt.org.inblue.schedule;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import fpt.org.inblue.service.submission.EmailFetcherService;

@Component
@RequiredArgsConstructor
public class BackgroundScheduler {

    private final PaymentSchedule paymentSchedule;

    private final SessionSchedule sessionSchedule;

    private final EmailFetcherService emailFetcherService;

    @Scheduled(fixedDelay = 300000)
    public void scheduleCheckPaymentStatus() {
        paymentSchedule.checkPaymentStatus();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleSendTodayMeetingNotification() {
        sessionSchedule.sendTodayMeetingNotification();
    }

    @Scheduled(fixedDelay = 300000)
    public void scheduleFetchEmails() {
        emailFetcherService.fetchEmails();
    }
}
