package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.ApplicationStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminApplicationSummaryDto {

    private Long applicationId;
    private Integer userId;
    private String candidateName;
    private String candidateEmail;
    private String avatarUrl;
    private String targetRole;
    private String targetLevel;

    private ApplicationStatus status;
    private Double overallScore;
    private Integer currentRoundOrder;
    private String currentRoundName;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
