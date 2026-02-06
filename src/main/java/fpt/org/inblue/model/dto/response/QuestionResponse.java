package fpt.org.inblue.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
    private String sessionKey;
    private boolean isFinished;

    // Thông tin câu hỏi hiện tại
    private String phaseName;
    private Integer currentQuestionIndex; // Để FE hiện thanh progress bar (Câu 1/10)
    private Integer totalQuestionsInPhase;
    private String questionContent;
    private String questionType; // BLUEPRINT hoặc FOLLOW_UP
}