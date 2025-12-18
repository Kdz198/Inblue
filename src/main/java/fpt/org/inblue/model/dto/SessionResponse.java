package fpt.org.inblue.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponse {
    private String id;
    private String name;
    private String url;
    private Object api_created;
    private String created_at;
    private RoomConfig config;

    @Data
    public static class RoomConfig {
        private String nbf;//thời điểm soems nhất room có thể đc dùng
        private Long exp;
    }
}
