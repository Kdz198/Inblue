package fpt.org.inblue.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AllRoundsCompletedEvent {
    private Long applicationId;
}
