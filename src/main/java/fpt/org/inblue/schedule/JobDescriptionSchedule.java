package fpt.org.inblue.schedule;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.repository.JobDescriptionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionSchedule {

    private final JobDescriptionRepository jobDescriptionRepository;

    /**
     * Chạy mỗi ngày lúc 00:00.
     * Tìm tất cả JD có status = OPEN và deadlineAt đã qua → chuyển sang CLOSED.
     */
    public void closeExpiredJobDescriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<JobDescription> expiredJds = jobDescriptionRepository
                .findByStatusAndDeadlineAtBefore(JobDescriptionStatus.OPEN, now);

        if (expiredJds.isEmpty()) {
            log.info("[JD Schedule] Không có JD nào hết hạn.");
            return;
        }

        for (JobDescription jd : expiredJds) {
            jd.setStatus(JobDescriptionStatus.CLOSED);
            log.info("[JD Schedule] Đóng JD id={}, title='{}', deadline={}", jd.getId(), jd.getTitle(), jd.getDeadlineAt());
        }

        jobDescriptionRepository.saveAll(expiredJds);
        log.info("[JD Schedule] Đã đóng {} JD hết hạn.", expiredJds.size());
    }
}
