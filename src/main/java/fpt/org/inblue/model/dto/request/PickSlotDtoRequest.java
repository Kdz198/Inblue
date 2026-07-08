package fpt.org.inblue.model.dto.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PickSlotDtoRequest {
    private Long applicationDetailId;
    private Long kioskId;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
}
