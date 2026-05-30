package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class SubmitRequest {
    private Long applicationId;
    private Long roundId;
    private SubmitRequest.SubmissionData submissionData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmissionData {
        // Dành cho vòng tự luận, Email, SQL Script (Frontend gửi text lên)
        private String textContent;

        // Dành cho vòng upload CV hoặc file kiến trúc (Frontend gửi link file sau khi upload S3)
        private MultipartFile file;

        // Dành riêng cho vòng QUIZ
        private List<String> quizAnswers;
    }

}
