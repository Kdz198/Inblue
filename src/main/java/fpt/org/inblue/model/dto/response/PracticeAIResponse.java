package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.dto.request.PracticeQuestionRequest;
import fpt.org.inblue.model.enums.TargetLevel;
import lombok.Data;

import java.util.List;

@Data
public class PracticeAIResponse {
    String practiceSetName;
    String objective;
    int dateNumber;
    List<PracticeQuestionRequest> questions;
}
