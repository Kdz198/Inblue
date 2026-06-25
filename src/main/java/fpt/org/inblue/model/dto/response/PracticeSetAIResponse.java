package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.dto.request.PracticeQuestionRequest;
import java.util.List;
import lombok.Data;

@Data
public class PracticeSetAIResponse {
    String practiceSetName;
    String objective;
    int dateNumber;
    List<PracticeQuestionRequest> questions;
}
