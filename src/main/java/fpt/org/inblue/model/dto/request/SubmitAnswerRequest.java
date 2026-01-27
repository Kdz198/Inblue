package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private String sessionKey;
    private String answer;
}