package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.BookingStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskHistoryResponseDto {

    private Long bookingId;
    private Long kioskId;
    private Long applicationDetailId;

    // Applicant Information
    private Integer applicantUserId;
    private String applicantName;
    private String applicantEmail;
    private String avatarUrl;

    // Application & JD Context
    private Long applicationId;
    private Long jdId;
    private String jdTitle;

    // Booking Details
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private BookingStatus status;
    private String sessionKey;
    private String notes;
    private LocalDateTime createdAt;
}
