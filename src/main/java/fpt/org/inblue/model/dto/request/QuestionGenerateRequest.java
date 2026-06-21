package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionGenerateRequest {
    private String categoryName;
    private String difficulty;
    private List<String> topics;          // Chủ đề cụ thể
    // VD: ["@Async", "Bean Lifecycle", "Security"]

    private String additionalPrompt;      // Gợi ý thêm cho AI
    // VD: "Ưu tiên câu hỏi có code snippet"

}
