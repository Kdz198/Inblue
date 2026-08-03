package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.model.ApplicationDetail;
import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationDetailBasicProjection {
    Long getId();
    Long getApplicationId();
    Long getRoundId();
    ApplicationDetailStatus getStatus();
    Double getFinalScore();
    Double getHrScore();
    String getHrNote();
    Double getAiScore();
    ApplicationDetail.RoundResult getFinalResult();
    LocalDateTime getStartedAt();
    LocalDateTime getCompletedAt();
    Integer getMentorId();
    List<Integer> getAssignedMentorIds();
    Integer getSessionId();
    Integer getAiInterviewSessionId();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
