package fpt.org.inblue.model.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCompanyRequest {
     String name;
    private String description;
    private String status;
}
