package fpt.org.inblue.model.dto.dailyco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionCreationRequest {
    DailyCoCreationRequest dailyCoCreationRequest;
    int userId;
    int mentorId;
    Timestamp joinTime;
}
