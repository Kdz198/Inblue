package fpt.org.inblue.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // Cực quan trọng: Nếu AI lỡ trả về field lạ thì không bị lỗi
public class InterviewBlueprintResponse {

    // Phân tích chiến thuật (Tại sao AI lại chia như thế này?)
    // VD: "Do ứng viên là Fresher nhưng làm dự án khó, nên chiến thuật là..."
    @JsonProperty("strategy_analysis")
    private String strategyAnalysis;

    // Danh sách các giai đoạn (Intro -> Project -> Tech...)
    @JsonProperty("blueprint")
    private List<InterviewPhase> blueprint;

    // ========================================================================
    // INNER CLASS: PHASE (GIAI ĐOẠN)
    // ========================================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewPhase {

        @JsonProperty("phase_name")
        private String phaseName;

        @JsonProperty("duration_minutes")
        private Integer durationMinutes;

        // Danh sách câu hỏi tuyến tính (Backend chỉ cần duyệt list này là xong)
        @JsonProperty("questions")
        private List<InterviewQuestion> questions;
    }

    // ========================================================================
    // INNER CLASS: QUESTION (CÂU HỎI CỤ THỂ)
    // ========================================================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewQuestion {

        @JsonProperty("order")
        private Integer order;

        // Lời thoại kịch bản (Agent sẽ đọc hoặc paraphrase lại câu này)
        // VD: "Trong dự án Library, bạn dùng Spring Security để làm gì?"
        @JsonProperty("question_text")
        private String questionText;

        // Từ khóa để query RAG tìm đáp án chuẩn chấm điểm
        // VD: "spring_security_authentication_flow"
        @JsonProperty("rag_keyword")
        private String ragKeyword;

        // Loại câu hỏi (CORE / FOLLOW_UP)
        @JsonProperty("question_type")
        private String questionType;
    }
}