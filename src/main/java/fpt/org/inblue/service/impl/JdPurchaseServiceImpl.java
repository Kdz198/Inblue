package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.JdPurchaseStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.JdPurchase;
import fpt.org.inblue.repository.JdPurchaseRepository;
import fpt.org.inblue.service.JdPurchaseService;
import fpt.org.inblue.utils.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JdPurchaseServiceImpl implements JdPurchaseService {

    private final JdPurchaseRepository jdPurchaseRepository;
    private final SecurityUtils securityUtils;

    @Override
    public boolean hasPurchased(Long jdId) {
        int userId = securityUtils.getCurrentUserId();
        return jdPurchaseRepository.existsByUserIdAndJdIdAndStatus(userId, jdId, JdPurchaseStatus.PURCHASED);
    }

    @Override
    public JdPurchase getPurchase(Long jdId) {
        int userId = securityUtils.getCurrentUserId();
        return jdPurchaseRepository
                .findByUserIdAndJdIdAndStatus(userId, jdId, JdPurchaseStatus.PURCHASED)
                .orElseThrow(() -> new CustomException("Không tìm thấy gói đã mua cho JD này", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<JdPurchase> getMyPurchases() {
        int userId = securityUtils.getCurrentUserId();
        return jdPurchaseRepository.findAllByUserId(userId);
    }
}
