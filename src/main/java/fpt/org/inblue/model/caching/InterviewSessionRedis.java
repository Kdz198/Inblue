package fpt.org.inblue.model.caching;

import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "InterviewSession", timeToLive = 7200) // 1 hour TTL
public class InterviewSessionRedis {
    @Id
    private String id;
    private InterviewBlueprintResponse blueprint;

    private int dbId; // Liên kết về DB chính

    // Lưu trạng thái hiện tại (đang hỏi câu nào)
    private Integer currentPhaseIndex;
    private Integer currentQuestionIndex;

    // --- [QUAN TRỌNG] State để nhớ AI đã quyết định gì ở lượt trước ---
    private String currentQuestionText;
    private QuestionType currentQuestionType;

    @Builder.Default
    private List<InterviewExchange> chatHistory = new LinkedList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterviewExchange implements Serializable {
        private String phaseName;     // Vd: Technical Core
        private int questionId;       // ID câu hỏi trong phase
        private int questionOrder;    // Vd: 1, 2, 3
        private String questionText;  // Nội dung câu hỏi lúc đó
        private String answerText;    // Ứng viên trả lời
        private String submittedAt;     // Timestamp (System.currentTimeMillis())
        private String currentQuestionText;
        private QuestionType type; // BLUEPRINT hoặc FOLLOW_UP
    }

    // Enum định nghĩa loại câu hỏi
    public enum QuestionType {
        BLUEPRINT, // Câu hỏi gốc trong kịch bản (Mỏ neo)
        FOLLOW_UP  // Câu hỏi bồi (AI tự nghĩ ra)
    }
}
