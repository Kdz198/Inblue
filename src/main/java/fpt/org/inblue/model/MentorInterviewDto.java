package fpt.org.inblue.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorInterviewDto {
    private Integer userId;
    private Integer mentorId;
    private Integer duration;
    private Integer totalPrice;
}
