package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.SessionStatus;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thông tin sơ bộ hiển thị trên dashboard của mentor: các session mentor tham gia và các application mentor đã đánh giá.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentorDashboardSummaryResponse {
    private int totalSessions;
    private Map<SessionStatus, Long> sessionCountByStatus;
    private List<SessionItem> sessions;

    private int totalReviewedApplications;
    private List<ReviewedApplicationItem> reviewedApplications;

    // Toàn bộ feedback mà user đã chấm cho mentor này
    private int totalFeedbacks;
    private List<FeedbackItem> feedbacks;

    // Toàn bộ ứng viên/user mà mentor này đã chấm điểm (không phụ thuộc application)
    private int totalReviewedCandidates;
    private List<ReviewedCandidateItem> reviewedCandidates;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserBasicInfo {
        private int id;
        private String name;
        private String email;
        private String avatarUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackItem {
        private int sessionId;
        private UserBasicInfo user;
        private int rating;
        private String comment;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewedCandidateItem {
        private int sessionId;
        private UserBasicInfo candidate;
        private MentorReviewResponse review; // điểm (rating) và các nhận xét của mentor
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionItem {
        private int sessionId;
        private SessionStatus status;
        private int menteeId;
        private String menteeName;
        private String menteeAvatarUrl;
        private Timestamp joinTime;
        private Integer duration;
        private Integer totalPrice;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewedApplicationItem {
        private Long applicationDetailId;
        private Long applicationId;
        private String jobTitle;
        private int sessionId;

        private int candidateId;
        private String candidateName;
        private String candidateEmail;
        private String candidateAvatarUrl;

        // Điểm và nhận xét mà mentor đã chấm cho ứng viên
        private MentorReviewResponse mentorReview;

        // Feedback mà ứng viên chấm cho mentor (null nếu ứng viên chưa feedback)
        private MentorFeedbackResponse candidateFeedback;
    }
}
