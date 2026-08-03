package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentorFeedbackResponse {
    private int rating;
    private String comment;
    private String userName;
    private String userAvatarUrl;
}
