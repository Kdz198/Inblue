package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.MeetingType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Một lịch hẹn do ứng viên đề xuất đang chờ mentor hiện tại duyệt (vòng Mentor Review, ONLINE).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentorPendingScheduleResponse {
    private Long applicationDetailId;
    private Long applicationId;
    private Long roundId;
    private String roundName;
    private Integer roundOrder;
    private String jobTitle;

    private Integer candidateUserId;
    private String candidateName;
    private String candidateEmail;
    private String candidateAvatarUrl;

    private Integer mentorId;
    private MeetingType meetingType;
    private String proposedJoinTime; // ISO-8601
    private Integer proposedDurationMinutes;

    private LocalDateTime requestedAt;
}
