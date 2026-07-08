package fpt.org.inblue.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;
}
