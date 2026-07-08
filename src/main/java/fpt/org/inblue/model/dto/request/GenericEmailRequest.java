package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class GenericEmailRequest {
    private String toEmail;
    private String subject;
    private String body;
}
