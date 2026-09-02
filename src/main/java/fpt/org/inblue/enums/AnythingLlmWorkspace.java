package fpt.org.inblue.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnythingLlmWorkspace {
    CV_PARSE("parse-cv"), // Workspace parse CV ứng viên (Gemini)
    CV_ANALYSIS("cv-processor"), // Workspace phân tích CV ứng viên (Gemini)
    CODING_GEN("coding-gen"), // Workspace chuyên về coding (Gemini Pro)
    EMAIL("email-processor"), // Workspace chuyên về email (Gemini Pro)
    CODE_REVIEW("code-review-processor"), // Workspace chuyên chấm Code Review
    CODE_REVIEW_GEN("code-review-generator"), // Workspace chuyên sinh đề Code Review
    QUIZ_GEN("quiz-generator"), // Workspace chuyên sinh đề Quiz
    PIPELINE_GEN("pipeline-generator"), // Workspace đề xuất pipeline và evaluation plan từ JD
    SKILL_TAGS("skill-tag-extractor"), // Workspace trích xuất và chuẩn hóa skill từ JD
    ENHANCE_TRANSCRIPT("enhance-transcript"), // Workspace refine transcripts cho AI interview)
    SUMMARY_SCRIPT_GEN("summary-script-gen"), // Workspace chuyên sinh script tóm tắt
    SUMMARY_REPORT("summary-report-gen"); // Workspace chuyên sinh báo cáo tóm tắt
    private final String slug;
}
