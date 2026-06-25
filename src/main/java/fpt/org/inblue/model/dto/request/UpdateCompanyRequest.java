package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class UpdateCompanyRequest {
    private Long id;
    String name;
    private String description;
    private String status;
}
