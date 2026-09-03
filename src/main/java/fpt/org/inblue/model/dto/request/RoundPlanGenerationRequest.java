package fpt.org.inblue.model.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundPlanGenerationRequest {

    private CompanyContext company;
    private JobDescriptionContext jobDescription;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyContext {
        private String name;
        private String description;
        private String logoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobDescriptionContext {
        private Long id;
        private String title;
        private String description;
        private String requirements;
        private String benefits;
        private Object level;
    }
}
