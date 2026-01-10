package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class CreateMentorFeedbackRequest {
    int sessionId;
    int mentorId;
    int userId;
    int rating;
    String comment;
}
