package fpt.org.inblue.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnythingLlmWorkspace {
    CV_ANALYSIS("cv-processor"), // Workspace phân tích CV ứng viên (Gemini)
    CODING_GEN("coding-gen"), // Workspace chuyên về coding (Gemini Pro)
    EMAIL("email-processor"), // Workspace chuyên về email (Gemini Pro)
    CODE_REVIEW("code-review-processor"), // Workspace chuyên chấm Code Review
    CODE_REVIEW_GEN("code-review-generator"), // Workspace chuyên sinh đề Code Review
    QUIZ_GEN("quiz-generator"); // Workspace chuyên sinh đề Quiz
    private final String slug;
}
