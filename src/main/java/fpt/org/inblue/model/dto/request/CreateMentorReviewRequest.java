package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateMentorReviewRequest {
    int sessionId;
    int mentorId;
    int userId;
    int rating;
    String situationNote;
    String taskNote;
    String actionNote;
    String resultNote;
    String strength;
    String weakness;
    String improve;
}
