package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.model.CandidateProfile;
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
public class AdminApplicationFullDetailResponseDto {

    private ApplicationOverviewDto applicationOverview;
    private JobDescriptionInfoDto jobDescriptionInfo;
    private CandidateInfoDto candidateInfo;
    private List<AdminRoundDetailDto> roundDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationOverviewDto {
        private Long applicationId;
        private ApplicationStatus status;
        private Double overallScore;
        private Integer currentRoundOrder;
        private String currentRoundName;
        private Integer totalRounds;
        private LocalDateTime appliedAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobDescriptionInfoDto {
        private Long jdId;
        private String title;
        private String level;
        private Double salaryMin;
        private Double salaryMax;
        private String currency;
        private Long companyId;
        private String companyName;
        private String companyLogo;
    }

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
        private CandidateProfile profile;
    }
}
