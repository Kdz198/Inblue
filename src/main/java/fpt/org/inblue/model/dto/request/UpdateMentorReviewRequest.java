package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class UpdateMentorReviewRequest {
    int id;
    int rating;
    String situationNote;
    String taskNote;
    String actionNote;
    String resultNote;
    String strength;
    String weakness;
    String improve;
}
