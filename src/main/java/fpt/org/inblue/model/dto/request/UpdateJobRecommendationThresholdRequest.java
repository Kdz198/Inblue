package fpt.org.inblue.model.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobRecommendationThresholdRequest {
    @NotNull(message = "thresholdPercent is required")
    @DecimalMin(value = "0.00", message = "thresholdPercent must be greater than or equal to 0")
    @DecimalMax(value = "100.00", message = "thresholdPercent must be less than or equal to 100")
    @Digits(integer = 3, fraction = 2, message = "thresholdPercent must have at most 2 decimal places")
    private BigDecimal thresholdPercent;
}
