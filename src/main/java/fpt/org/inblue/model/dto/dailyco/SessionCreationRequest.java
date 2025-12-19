package fpt.org.inblue.model.dto.dailyco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionCreationRequest {
    DailyCoCreationRequest dailyCoCreationRequest;
    int userId;
    int mentorId;

}
