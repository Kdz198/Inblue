package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class CreateCompanyRequest {
    String name;
    private String description;
    private String status;
}
