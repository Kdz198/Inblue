package fpt.org.inblue.model.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequest {
    private Long applicationId;
    private String textContent;

    @Nullable
    private MultipartFile file;

    // Dành riêng cho vòng QUIZ
    private List<String> quizAnswers;

}
