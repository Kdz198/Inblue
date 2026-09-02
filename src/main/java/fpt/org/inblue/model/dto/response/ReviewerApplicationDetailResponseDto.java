package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
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
public class ReviewerApplicationDetailResponseDto {
    private Long id;
    private Long applicationId;
    private Long roundId;
    private ApplicationDetailStatus status;
    private Double finalScore;
    private ApplicationDetail.SubmissionData submissionData;
    private Double aiScore;
    private Object aiFeedback;
    private ApplicationDetail.StructuredAiFeedback structuredAiFeedback;
    private Double hrScore;
    private String hrNote;
    private ApplicationDetail.RoundResult finalResult;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer mentorId;
    private List<Integer> assignedMentorIds;
    private List<MentorResponse> assignedMentors;
    private Object mentorReview;
    private Integer sessionId;
    private Integer aiInterviewSessionId;
    private ApplicationDetail.RoundSessionInfo sessionInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin bổ sung
    private String jobTitle; // Tên vị trí tuyển dụng
    private String roundName; // Tên vòng thi
    private String instruction; // Đề bài / Hướng dẫn làm bài
    private Round.RoundConfig roundConfig; // Cấu hình chi tiết & đề bài vòng thi
}
