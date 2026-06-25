package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.PlanName;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionResponse {
    PlanName planName;
    int price;
    int durationDays;

    int maxAiInterview;
    int maxPracticeSets;
    int maxQuizSets;

    int aiInterviewUsed;
    int practiceSetUsed;
    int quizSetUsed;

    int aiInterviewRemaining;
    int practiceSetRemaining;
    int quizSetRemaining;

    LocalDate expiredAt;
    boolean isActive;
}
