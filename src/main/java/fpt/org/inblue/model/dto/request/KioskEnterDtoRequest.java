package fpt.org.inblue.model.dto.request;

import lombok.Data;

@Data
public class KioskEnterDtoRequest {
    private String sessionKey;
    private Long kioskId;
}
