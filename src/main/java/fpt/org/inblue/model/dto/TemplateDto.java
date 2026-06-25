package fpt.org.inblue.model.dto;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round.RoundConfig;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class TemplateDto {

    @Data
    @Builder
    public static class SummaryResponse {
        private Long id;
        private String name;
        private String category;
        private String description;
    }

    // DTO dùng cho màn hình chi tiết để Autofill
    @Data
    @Builder
    public static class DetailResponse {
        private Long id;
        private String name;
        private String category;
        private String description;
        private List<RoundItem> rounds;
    }

    @Data
    @Builder
    public static class RoundItem {
        private String name;
        private Integer roundOrder;
        private RoundType roundType;
        private Double passThreshold;
        private RoundConfig configData;
    }
}
