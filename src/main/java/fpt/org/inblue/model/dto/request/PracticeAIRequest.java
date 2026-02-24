package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.InterviewResultDetail;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;
//Dto gửi qua AI service để tạo bộ ôn luyện cá nhân hóa
@Data
public class PracticeAIRequest {
    List<InterviewResultDetail.QAResult> qaResults;
    String targetRole; // vd "Java Fresher Software Engineer", " Data Analyst Intern"
    private String targetLevel;
    private String candidateIntroduction;
    // số lượng bộ đề muốn ôn
    int practiceSetRequest;
}
