package fpt.org.inblue.model.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScheduleEventDto {

    private String id;
    private String title;
    private String description;
    private String eventType; // APPLICATION_ROUND, KIOSK_BOOKING, MENTOR_SESSION
    private LocalDateTime start;
    private LocalDateTime end;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, SCHEDULED, CONFIRMED, CANCELLED, etc.
    private String location;
    private String meetingType; // ONLINE, OFFLINE, KIOSK
    private String roomUrl;
    private String color;

    // Metadata / Linked IDs
    private Long applicationId;
    private Long applicationDetailId;
    private Long kioskId;
    private Integer sessionId;
    private String sessionKey;
    private String jobTitle;
    private String roundName;
}
