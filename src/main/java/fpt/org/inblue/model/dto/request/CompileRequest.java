package fpt.org.inblue.model.dto.request;

import fpt.org.inblue.enums.CompilerLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompileRequest {
    private Long problemId;
    private CompilerLanguage language;
    private List<String> sourceCode;
    private Boolean isTest;
    // nếu true là compile đơn ,nếu false là nộp bài
}
