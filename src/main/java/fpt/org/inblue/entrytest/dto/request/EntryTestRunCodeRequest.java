package fpt.org.inblue.entrytest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryTestRunCodeRequest {
    @NotBlank(message = "itemId is required")
    private String itemId;

    @NotBlank(message = "language is required")
    private String language;

    @NotNull(message = "sourceCode is required")
    @NotEmpty(message = "sourceCode cannot be empty")
    private List<String> sourceCode;
}
