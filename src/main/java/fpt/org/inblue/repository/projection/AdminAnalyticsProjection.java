package fpt.org.inblue.repository.projection;

import fpt.org.inblue.enums.ApplicationDetailStatus;
import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.enums.RoundType;
import java.time.LocalDateTime;

public final class AdminAnalyticsProjection {

    private AdminAnalyticsProjection() {}

    public interface JobTrend {
        Long getJobId();

        String getJobTitle();

        Long getApplicationCount();
    }

    public interface PositionTrend {
        String getPosition();

        Long getApplicationCount();
    }

    public interface ApplicationStatusCount {
        ApplicationStatus getStatus();

        Long getApplicationCount();
    }

    public interface ApplicationUserStats {
        Long getTotalApplications();

        Long getUniqueApplicants();
    }

    public interface ActiveInterview {
        Long getApplicationDetailId();

        Long getApplicationId();

        Integer getUserId();

        String getUserName();

        String getUserEmail();

        Long getJobId();

        String getJobTitle();

        Long getRoundId();

        Integer getRoundOrder();

        String getRoundName();

        RoundType getRoundType();

        ApplicationDetailStatus getRoundStatus();

        java.time.LocalDateTime getStartedAt();

        java.time.LocalDateTime getUpdatedAt();
    }

    public interface RecentTransaction {
        Integer getTransactionId();

        String getTransactionCode();

        Long getAmount();

        String getDescription();

        PaymentStatus getStatus();

        LocalDateTime getCreatedAt();

        Integer getUserId();

        String getUserName();

        String getUserEmail();

        String getAvatarUrl();

        Long getJobId();

        String getJobTitle();
    }
}
