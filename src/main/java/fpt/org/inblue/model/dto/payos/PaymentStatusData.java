package fpt.org.inblue.model.dto.payos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusData {
    private String status;
    private Long orderCode;
    private Integer amount;
}