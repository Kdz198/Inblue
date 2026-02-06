package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.Major;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.model.enums.TargetLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeRequest {
    String practiceSetName;
    String objective;
    TargetLevel target;
    int majorId;
   List<PracticeQuestionRequest> questions;
}

