package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.enums.TargetLevel;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOpenJdResponseDto {

    private Long jdId;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private TargetLevel level;
    private Double salaryMin;
    private Double salaryMax;
    private String currency;
    private Long price;
    private JobDescriptionStatus status;
    private Integer roundsCount;
    private LocalDateTime deadlineAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CompanySummaryDto company;
    private ApplicationStatisticsDto statistics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanySummaryDto {
        private Long id;
        private String name;
        private String logoUrl;
        private String bannerUrl;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationStatisticsDto {
        private Integer totalApplications;
        private Integer inProgressCount;
        private Integer passedCount;
        private Integer failedCount;
    }
}
