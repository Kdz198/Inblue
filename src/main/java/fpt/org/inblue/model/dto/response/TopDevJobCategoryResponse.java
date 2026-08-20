package fpt.org.inblue.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopDevJobCategoryResponse {

    private String code;
    private Integer id;
    private String displayName;
}
