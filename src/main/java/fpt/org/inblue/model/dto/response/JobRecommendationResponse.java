package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.JobDescriptionStatus;
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
public class JobRecommendationResponse {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private TargetLevel level;
    private Double salaryMin;
    private Double salaryMax;
    private Long price;
    private String currency;
    private List<String> skillTags;
    private String companyName;
    private String companyLogo;
    private JobDescriptionStatus status;
    private LocalDateTime deadlineAt;
    private Integer appliedCount;
}
