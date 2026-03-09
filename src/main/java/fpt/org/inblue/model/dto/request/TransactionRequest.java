package fpt.org.inblue.model.dto.request;


import lombok.Data;

@Data
public class TransactionRequest {
    private long amount;
    private int userId;

}
