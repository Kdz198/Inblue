package fpt.org.inblue.model.dto;

import fpt.org.inblue.enums.FeatureName;
import lombok.Data;

@Data
public class FeatureUsageLogDto {
    String token;
    FeatureName featureName;
}
