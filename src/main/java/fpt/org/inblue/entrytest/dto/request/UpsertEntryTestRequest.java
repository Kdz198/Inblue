package fpt.org.inblue.entrytest.dto.request;

import fpt.org.inblue.entrytest.model.EntryTest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertEntryTestRequest {
    private String name;
    private Double totalScore;
    private Integer timeLimitMinutes;
    private List<EntryTest.EntryTestSectionConfig> sectionConfigs;
    private Boolean isActive;
}
