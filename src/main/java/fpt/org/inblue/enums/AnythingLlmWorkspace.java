package fpt.org.inblue.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnythingLlmWorkspace {
    GENERAL_CHAT("inblue-general-chat"), // Workspace chat thông thường (GPT-4o)
    DOCS_ANALYSIS("inblue-docs-reader"), // Workspace nhai file lớn (Gemini)
    HR_SUPPORT("inblue-hr-internal"),    // Workspace dùng RAG tài liệu nội bộ
    CV_ANALYSIS("cv-processor"), // Workspace phân tích CV ứng viên (Gemini)
    CODING_GEN("coding-gen"), // Workspace chuyên về coding (Gemini Pro)
    EMAIL( "email-processor"), // Workspace chuyên về email (Gemini Pro)
    CODE_REVIEW("code-review-processor"), // Workspace chuyên chấm Code Review
    CODE_REVIEW_GEN("code-review-generator"); // Workspace chuyên sinh đề Code Review

    private final String slug;


}