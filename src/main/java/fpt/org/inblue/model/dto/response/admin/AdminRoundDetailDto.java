package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.MentorReview;
import fpt.org.inblue.model.Round;
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
public class AdminRoundDetailDto {

    private Long applicationDetailId;
    private Long roundId;
    private Integer roundOrder;
    private String roundName;
    private RoundType roundType;
    private Double passThreshold;
    private Integer reviewerId;
    private Round.RoundConfig roundConfig; // Đề bài, câu hỏi, hướng dẫn và cấu hình vòng thi

    private ApplicationDetailStatus status;
    private Double aiScore;
    private ApplicationDetail.AiFeedback aiFeedback;
    private ApplicationDetail.StructuredAiFeedback structuredAiFeedback;

    private Double hrScore;
    private String hrNote;

    private Double finalScore;
    private ApplicationDetail.RoundResult finalResult;

    private ApplicationDetail.SubmissionData submissionData;
    private ApplicationDetail.RoundSessionInfo sessionInfo;

    private Integer mentorId;
    private List<Integer> assignedMentorIds;
    private List<MentorResponse> assignedMentors;
    private MentorReview mentorReview;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
