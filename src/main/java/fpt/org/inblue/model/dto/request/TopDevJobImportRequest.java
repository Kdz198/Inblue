package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.TargetLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopDevJobImportRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String companyName;

    private String companyLogo;
    private String companyDescription;

    private String description;
    private String requirements;
    private String benefits;
    private String skills;
    private String location;
    private String salary;
    private String source;
    private String sourceUrl;
    private String sourceJobId;
    private TargetLevel requestedLevel;
}
