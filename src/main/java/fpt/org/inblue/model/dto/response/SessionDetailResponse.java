package fpt.org.inblue.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import fpt.org.inblue.enums.SessionStatus;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionDetailResponse {
    private int id;
    private String roomName;
    private int userId;
    private String participantId1;
    private Timestamp startTime1;
    private Timestamp endTime1;
    private Long durationSeconds1;
    private int mentorId;
    private String participantId2;
    private Timestamp startTime2;
    private Timestamp endTime2;
    private Long durationSeconds2;
    private String roomUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private Timestamp joinTime;

    private String recordUrl;
    private SessionStatus status;
    private Integer duration;
    private Integer totalPrice;
    private String transactionCode;
    private String sessionKey;
    private Long kioskId;

    private MentorReviewResponse mentorReview;

    private MentorFeedbackResponse mentorFeedback;
}
