package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.model.enums.JobDescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobDescriptionRequest {
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private String level; // Intern, Fresher, Junior, Middle, Senior
    private Double salaryMin;
    private Double salaryMax;
    private String currency;
    private JobDescriptionStatus status; // OPEN, CLOSED, DRAFT
    private LocalDateTime deadlineAt;
    private Long companyId;
}


