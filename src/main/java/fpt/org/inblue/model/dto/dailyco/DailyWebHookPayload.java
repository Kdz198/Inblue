package fpt.org.inblue.model.dto.dailyco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DailyWebHookPayload {
    private String event;
    private PayloadData payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayloadData {
        private String roomName;
        @JsonProperty("session_id")
        private String participantId;
        private String recording_id;
    }
}
