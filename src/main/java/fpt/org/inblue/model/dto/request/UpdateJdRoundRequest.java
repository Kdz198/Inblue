package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateJdRoundRequest {
    @NotEmpty(message = "Phải có ít nhất 1 vòng phỏng vấn")
    @Valid
    private List<UpdateJdRoundRequest.RoundItemDto> rounds;

    @Data
    public static class RoundItemDto {
        private Long id;
        @NotBlank(message = "Tên vòng không được để trống")
        private String name;
        @NotNull(message = "Thứ tự vòng là bắt buộc")
        @Min(value = 1, message = "Thứ tự vòng phải bắt đầu từ 1")
        private Integer roundOrder;
        @NotNull(message = "Loại vòng là bắt buộc")
        private RoundType roundType;

        @NotNull(message = "Điểm sàn không được để trống")
        @Min(0)
        private Double passThreshold;

        @NotNull(message = "Cấu hình vòng không được thiếu")
        @Valid
        private UpdateJdRoundRequest.RoundConfigDto configData;
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
        private List<UpdateJdRoundRequest.QuizQuestionDto> quizQuestions;
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
