package fpt.org.inblue.event;

import fpt.org.inblue.service.JourneySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JourneySummaryEventListener {
    private final JourneySummaryService journeySummaryService;

    @Async
    @EventListener
    public void handleAllRoundsCompleted(AllRoundsCompletedEvent event) {
        try {
            log.info("Received AllRoundsCompletedEvent. Start journey summary generation for applicationId={}",
                    event.getApplicationId());
            journeySummaryService.generate(event.getApplicationId());
            log.info("Finished handling AllRoundsCompletedEvent for applicationId={}", event.getApplicationId());
        } catch (Exception e) {
            log.error("Failed to generate journey summary for applicationId={}", event.getApplicationId(), e);
        }
    }
}
