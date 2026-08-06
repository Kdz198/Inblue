package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.ApplicationStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationLookupResponse {
    private Long id;
    private Integer userId;
    private Long jdId;
    private String applicationName;
    private Integer currentRoundOrder;
    private ApplicationStatus status;
    private Double overallScore;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
