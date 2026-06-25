package fpt.org.inblue.model.caching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "InterviewBehavior", timeToLive = 3600)
public class InterviewBehaviorRedis {

    @Id
    private String sessionKey; // Vẫn dùng sessionKey làm ID để dễ tìm

    // Map chứa lỗi: Key = globalIndex, Value = Danh sách lỗi
    @Builder.Default
    private Map<Integer, List<String>> behavioralRecord = new HashMap<>();
}
