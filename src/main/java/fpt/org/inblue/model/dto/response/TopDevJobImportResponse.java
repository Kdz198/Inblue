package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.JobDescriptionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopDevJobImportResponse {
    private Long companyId;
    private Long jobDescriptionId;
    private String companyName;
    private String jobDescriptionTitle;
    private JobDescriptionStatus jobDescriptionStatus;
    private boolean companyCreated;
}
