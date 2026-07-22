package fpt.org.inblue.model.dto.request;

import java.util.List;

import fpt.org.inblue.model.dto.response.admin.AdminApplicationSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminJdApplicationsResponseDto {

    private JdSummaryDto jdInfo;
    private SummaryStatisticsDto summaryStatistics;
    private List<AdminApplicationSummaryDto> applications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JdSummaryDto {
        private Long jdId;
        private String jdTitle;
        private String companyName;
        private String companyLogo;
        private Integer totalRounds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryStatisticsDto {
        private Integer totalApplications;
        private Integer inProgressCount;
        private Integer passedCount;
        private Integer failedCount;
        private Double avgOverallScore;
    }
}
