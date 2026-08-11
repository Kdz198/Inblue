package fpt.org.inblue.schedule;

import fpt.org.inblue.service.JourneySummaryService;
import fpt.org.inblue.service.submission.EmailSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackgroundScheduler {

    private final SessionSchedule sessionSchedule;
    private final EmailSubmissionService emailSubmissionService;
    private final JobDescriptionSchedule jobDescriptionSchedule;
    private final JourneySummaryService journeySummaryService;
    private final SummaryAudioScheduler summaryAudioScheduler;

    //    @Scheduled(fixedDelay = 300000)
    //    public void scheduleCheckPaymentStatus() {
    //        paymentSchedule.checkPaymentStatus();
    //    }

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

    // Chạy mỗi ngày lúc 00:00 để đóng các JD đã hết hạn (deadlineAt < now)
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCloseExpiredJobDescriptions() {
        jobDescriptionSchedule.closeExpiredJobDescriptions();
    }

    // Chạy mỗi 30 phút một lần để scan và sinh script tóm tắt cho các record JourneySummary chưa có script
    @Scheduled(cron = "0 0/30 * * * ?")
    public void scheduleGenerateSummaryScripts() {
        journeySummaryService.generateMissingScripts();
    }

    @Scheduled(fixedDelay = 120000)
    public void scheduleGenerateMissingAudio() throws FileNotFoundException {
        log.info("Generating missing audio");
        summaryAudioScheduler.scheduleGenerateMissingAudio();
    }
}
