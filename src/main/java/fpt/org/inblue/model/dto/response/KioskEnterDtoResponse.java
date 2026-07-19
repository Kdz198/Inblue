package fpt.org.inblue.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KioskEnterDtoResponse {
    private String type; // MENTOR or AI
    private String roomUrl; // For Daily.co (Mentor)
    private String aiSessionKey; // For AI Interview
}
