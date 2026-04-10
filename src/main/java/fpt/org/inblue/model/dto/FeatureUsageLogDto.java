package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.enums.FeatureName;
import lombok.Data;

@Data
public class FeatureUsageLogDto {
    String token;
    FeatureName featureName;
}
