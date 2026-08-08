package fpt.org.inblue.model.dto.response.admin;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.enums.RoundType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardOverviewResponse {

    private LocalDateTime generatedAt;
    private DashboardSummary summary;
    private List<JobTrendItem> jobTrends;
    private List<PositionTrendItem> positionTrends;
    private List<ActiveInterviewItem> activeInterviews;
    private int recentTransactionDays;
    private List<RecentTransactionItem> recentTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private long totalApplications;
        private long inProgressApplications;
        private long passedApplications;
        private long failedApplications;
        private long activeInterviewCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobTrendItem {
        private int rank;
        private Long jobId;
        private String jobTitle;
        private long applicationCount;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionTrendItem {
        private int rank;
        private String position;
        private long applicationCount;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveInterviewItem {
        private Long applicationDetailId;
        private Long applicationId;
        private Integer userId;
        private String userName;
        private String userEmail;
        private Long jobId;
        private String jobTitle;
        private Long roundId;
        private Integer roundOrder;
        private String roundName;
        private RoundType roundType;
        private ApplicationDetailStatus roundStatus;
        private LocalDateTime startedAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransactionItem {
        private Integer transactionId;
        private String transactionCode;
        private long amount;
        private String description;
        private PaymentStatus status;
        private LocalDateTime createdAt;
        private Integer userId;
        private String userName;
        private String userEmail;
        private String avatarUrl;
        private Long jobId;
        private String jobTitle;
    }
}
