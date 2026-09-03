package fpt.org.inblue.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PineconeEmbeddingRequest {
    private String model;
    private Parameters parameters;
    private List<Input> inputs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {
        @JsonProperty("input_type")
        private String inputType;

        private String truncate;
        private Integer dimension;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        private String text;
    }
}
