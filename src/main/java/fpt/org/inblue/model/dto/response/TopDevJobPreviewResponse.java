package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.TargetLevel;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopDevJobPreviewResponse {
    private String source;
    private String sourceUrl;
    private String sourceJobId;
    private Boolean isExist;
    private Long existingJobDescriptionId;
    private String title;
    private String companyName;
    private String companyLogo;
    private String companyDescription;
    private String location;
    private String description;
    private String requirements;
    private String benefits;
    private String skills;
    private String salary;
    private LocalDate postedAt;
    private LocalDate validThrough;
    private TargetLevel requestedLevel;
}
