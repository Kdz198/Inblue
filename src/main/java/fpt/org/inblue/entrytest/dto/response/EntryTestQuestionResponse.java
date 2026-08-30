package fpt.org.inblue.entrytest.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryTestQuestionResponse {
    private String itemId;
    private Integer questionBankId;
    private String questionText;
    private List<String> options;
    private String categoryName;
    private String difficulty;
    private Double maxScore;
    private Integer displayOrder;
}
