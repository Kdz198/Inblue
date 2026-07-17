package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentorReviewResponse {
    private int rating;
    private String situationNote;
    private String taskNote;
    private String actionNote;
    private String resultNote;
    private String strength;
    private String weakness;
    private String improve;
}
