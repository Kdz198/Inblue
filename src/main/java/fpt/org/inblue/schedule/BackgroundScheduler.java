package fpt.org.inblue.schedule;

import fpt.org.inblue.service.submission.EmailSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackgroundScheduler {

    private final PaymentSchedule paymentSchedule;
    private final SessionSchedule sessionSchedule;
    private final EmailSubmissionService emailSubmissionService;

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
        emailSubmissionService.fetchEmails();
    }

    @Scheduled(fixedDelay = 60000) // Chạy mỗi 60 giây
    public void scheduleProcessPendingEmails() {
        emailSubmissionService.processEmailSchedule();
    }
}
