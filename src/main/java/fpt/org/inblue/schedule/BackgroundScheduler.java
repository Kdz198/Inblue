package fpt.org.inblue.schedule;

import fpt.org.inblue.model.EmailSubmission;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.repository.EmailSubmissionRepository;
import fpt.org.inblue.service.submission.EmailFetcherService;
import fpt.org.inblue.service.submission.SubmissionService;
import java.util.List;
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
    private final EmailFetcherService emailFetcherService;
    private final EmailSubmissionRepository emailSubmissionRepository;
    private final SubmissionService submissionService;

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

    @Scheduled(fixedDelay = 60000) // Chạy mỗi 30 giây
    public void scheduleProcessPendingEmails() {
        List<EmailSubmission> pendingEmails =
                emailSubmissionRepository.findByStatus(EmailSubmission.EmailStatus.PENDING);
        for (EmailSubmission email : pendingEmails) {
            if (email.getApplicationId() != null) {
                try {
                    // Đổi trạng thái sang PROCESSED đồng bộ để tránh bị quét trùng lặp ở lần tick sau
                    email.setStatus(EmailSubmission.EmailStatus.PROCESSED);
                    emailSubmissionRepository.save(email);

                    SubmitRequest submitRequest = SubmitRequest.builder()
                            .applicationId(email.getApplicationId())
                            .build();
                    submissionService.submitRound(submitRequest);
                    log.info(
                            "Successfully triggered email evaluation for email submission ID: {}, applicationId: {}",
                            email.getId(),
                            email.getApplicationId());
                } catch (Exception e) {
                    log.error("Error triggering email evaluation for email submission ID: " + email.getId(), e);
                    email.setStatus(EmailSubmission.EmailStatus.ERROR);
                    email.setErrorMessage(e.getMessage());
                    emailSubmissionRepository.save(email);
                }
            }
        }
    }
}
