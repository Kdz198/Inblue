package fpt.org.inblue.model.dto.dailyco;

import lombok.Data;

import java.util.List;

@Data
public class RecordingResponse {
    Integer total_count;
    List<RecordingMetadata> data;
}
