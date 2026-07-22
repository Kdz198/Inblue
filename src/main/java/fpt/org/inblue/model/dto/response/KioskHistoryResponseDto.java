package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.enums.TargetLevel;
import java.time.LocalDateTime;
import java.util.List;
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
    private Long applicationId;

    // Concise Candidate Info
    private CandidateInfoDto candidateInfo;

    // Job Description & Company Info
    private JobDescriptionInfoDto jobDescriptionInfo;

    // Booking Details
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private BookingStatus status;
    private String sessionKey;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CandidateInfoDto {
        private Integer userId;
        private String name;
        private String email;
        private String avatarUrl;
        private String cvUrl;
        private String targetRole;
        private String targetLevel;
        private List<String> technicalSkills;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobDescriptionInfoDto {
        private Long jdId;
        private String title;
        private TargetLevel level;
        private Double salaryMin;
        private Double salaryMax;
        private String currency;
        private Long companyId;
        private String companyName;
        private String companyLogo;
    }
}
