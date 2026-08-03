package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.response.MentorResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminApplicationDetailResponse {
    private Long id;
    private Long applicationId;
    private Long roundId;
    private ApplicationDetailStatus status;
    private Double finalScore;
    private Double hrScore;
    private String hrNote;
    private Double aiScore;
    private ApplicationDetail.RoundResult finalResult;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer mentorId;
    private List<Integer> assignedMentorIds;
    private List<MentorResponse> assignedMentors;
    private Integer sessionId;
    private Integer aiInterviewSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các thông tin bổ sung (Add-on)
    private String roundName;
    private Integer roundOrder;
    private String jdTitle;
    private String candidateName;
    private String candidateEmail;
    private String candidateAvatarUrl;
}
