package fpt.org.inblue.service;

import fpt.org.inblue.model.JdPurchase;
import fpt.org.inblue.model.dto.response.MyJdPurchaseResponseDto;
import java.util.List;

public interface JdPurchaseService {

    boolean hasPurchased(Long jdId);

    JdPurchase getPurchase(Long jdId);

    List<MyJdPurchaseResponseDto> getMyPurchases();
}
