package fpt.org.inblue.model.dto.dailyco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordingMetadata {
    private String id;
    private String room_name;
    private String status;
}
