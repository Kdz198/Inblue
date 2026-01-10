package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class UpdateMentorFeedbackRequest {
    int id;
    int rating;
    String comment;
}
