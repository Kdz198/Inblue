package fpt.org.inblue.model.dto.dailyco;

import java.util.List;
import lombok.Data;

@Data
public class RecordingResponse {
    Integer total_count;
    List<RecordingMetadata> data;
}
