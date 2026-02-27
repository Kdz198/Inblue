package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.model.dto.request.PracticeQuestionRequest;
import lombok.Data;

import java.util.List;

@Data
public class PracticeSetAIResponse {
        String practiceSetName;
        String objective;
        int dateNumber;
        List<PracticeQuestionRequest> questions;
}
