package fpt.org.inblue.model.dto.response;

import fpt.org.inblue.enums.JdPurchaseStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyJdPurchaseResponseDto {

    private Long id;
    private JdPurchaseStatus status;
    private LocalDateTime purchasedAt;
    private LocalDateTime usedAt;
    private LocalDateTime validUntil;

    private EnrichedJobDescription jobDescription;
    private EnrichedPayment payment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrichedJobDescription {
        private Long id;
        private String title;
        private String companyName;
        private String thumbnailUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnrichedPayment {
        private Integer id;
        private Long amount;
        private String currency;
        private String method;
    }
}
