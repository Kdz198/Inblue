package fpt.org.inblue.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinSessionDtoRequest {
    private String sessionName;
    private int userId;
    private String participantId;
}
