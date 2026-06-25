package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class CreateMentorFeedbackRequest {
    int sessionId;
    int mentorId;
    int userId;
    int rating;
    String comment;
}
