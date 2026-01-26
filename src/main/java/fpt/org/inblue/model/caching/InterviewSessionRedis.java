package fpt.org.inblue.model.caching;

import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "InterviewSession", timeToLive = 3600) // 1 hour TTL
public class InterviewSessionRedis {
    @Id
    private String id;
    private InterviewBlueprintResponse blueprint;

    // Lưu trạng thái hiện tại (đang hỏi câu nào)
    private Integer currentPhaseIndex;
    private Integer currentQuestionIndex;
}
