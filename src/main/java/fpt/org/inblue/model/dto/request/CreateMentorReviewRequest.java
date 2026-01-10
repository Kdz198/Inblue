package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
