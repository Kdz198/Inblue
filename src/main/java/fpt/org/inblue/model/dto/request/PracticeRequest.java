package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.Major;
import fpt.org.inblue.model.PracticeQuestion;
import fpt.org.inblue.model.enums.QuestionLevel;
import fpt.org.inblue.model.enums.TargetLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeRequest {
    //có thể null cho trường hợp gửi qua AI service để tạo bộ đề ôn luyện cá nhân hóa
    int aiInterviewId;
    int userId;
    String practiceSetName;
    String objective;
    TargetLevel target;
    int majorId;
    int dateNumber;
   List<PracticeQuestionRequest> questions;

}

