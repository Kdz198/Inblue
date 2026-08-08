package fpt.org.inblue.model.dto.response.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApplicationsPerUserResponse {

    private LocalDateTime generatedAt;
    private long totalApplications;
    private long uniqueApplicants;
    private double averageApplicationsPerUser;
}
