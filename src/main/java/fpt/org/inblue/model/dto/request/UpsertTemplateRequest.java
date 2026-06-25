package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round.RoundConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class UpsertTemplateRequest {

    @NotBlank(message = "Tên Template không được để trống")
    private String name;

    @NotBlank(message = "Category không được để trống (VD: FAANG, BACKEND)")
    private String category;

    private String description;

    @NotEmpty(message = "Template phải có ít nhất 1 vòng phỏng vấn")
    @Valid
    private List<TemplateRoundItem> rounds;

    @Data
    public static class TemplateRoundItem {
        @NotBlank
        private String name;

        @NotNull
        private Integer roundOrder;

        @NotNull
        private RoundType roundType;

        private Double passThreshold;

        private RoundConfig configData;
    }
}
