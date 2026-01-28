package fpt.org.inblue.model.dto.dailyco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DailyWebHookPayload {
    @JsonProperty("type")
    private String event;
    private PayloadData payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayloadData {
        @JsonProperty("room")
        private String roomName;
        @JsonProperty("session_id")
        private String participantId;
        private String recording_id;
    }
}
