package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.enums.TargetLevel;
import lombok.Data;

// Dto tu controller gui ve service
@Data
public class PracticeGenerateRequest {
    int userId;
    int aiInterviewId;
//    String majorId;
    int dateNumber;
}
