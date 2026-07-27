package fpt.org.inblue.controller;

import fpt.org.inblue.model.dto.response.MyJdPurchaseResponseDto;
import fpt.org.inblue.service.JdPurchaseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jd-purchases")
@RequiredArgsConstructor
public class JdPurchaseController {

    private final JdPurchaseService jdPurchaseService;

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkPurchased(@RequestParam Long jdId) {
        return ResponseEntity.ok(jdPurchaseService.hasPurchased(jdId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyJdPurchaseResponseDto>> getMyPurchases() {
        return ResponseEntity.ok(jdPurchaseService.getMyPurchases());
    }
}
