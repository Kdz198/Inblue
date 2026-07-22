package fpt.org.inblue.utils;

import fpt.org.inblue.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Spring-managed utility bean để lấy thông tin user hiện tại từ JWT token.
 * Dùng thay cho pattern: String token = HelperUtil.getToke(); jwtUtils.getUserIdFromToken(token);
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtUtils jwtUtils;

    /**
     * Lấy userId của user đang đăng nhập từ JWT token trong request hiện tại.
     */
    public int getCurrentUserId() {
        String token = HelperUtil.getToke();
        return jwtUtils.getUserIdFromToken(token);
    }
}
