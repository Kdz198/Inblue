package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.dailyco.SessionCreationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class SetupJdRoundsRequest {

    @NotEmpty(message = "Phải có ít nhất 1 vòng phỏng vấn")
    @Valid
    private List<RoundItemDto> rounds;

    @Data
    public static class RoundItemDto {

        @NotBlank(message = "Tên vòng không được để trống")
        private String name;

        @NotNull(message = "Thứ tự vòng là bắt buộc")
        @Min(value = 1, message = "Thứ tự vòng phải bắt đầu từ 1")
        private Integer roundOrder; // Trọng tâm cho việc kéo thả của FE

        @NotNull(message = "Loại vòng là bắt buộc")
        private RoundType roundType;

        @NotNull(message = "Điểm sàn không được để trống")
        @Min(0)
        private Double passThreshold;

        private Integer reviewerId;

        @NotNull(message = "Cấu hình vòng không được thiếu")
        @Valid
        private RoundConfigDto configData;
    }

    @Data
    public static class RoundConfigDto {
        // Validation ở đây có thể nới lỏng để linh hoạt, xử lý chi tiết ở tầng Processor
        private String instruction;
        private String submissionFormat;
        private Integer timeLimitMinutes;
        private Integer maxScore;

        private String aiSystemPrompt;
        private String evaluationCriteria;

        // Chỉ dùng cho vòng QUIZ
        private List<QuizQuestionDto> quizQuestions;
        private List<Long> codingProblemsId;

        //Dùng cho vòng interview mentor
        private Round.MentorInterviewDto mentorInterview;
    }



    @Data
    public static class QuizQuestionDto {
        @NotBlank
        private String questionText;
        @NotEmpty
        private List<String> options;
        @NotBlank
        private String correctAnswer;
        @NotNull
        private Integer points;
    }
}