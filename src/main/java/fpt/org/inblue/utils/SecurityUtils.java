package fpt.org.inblue.utils;

import fpt.org.inblue.enums.Role;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.security.JwtUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        if (token == null || token.isBlank()) {
            throw new CustomException("Missing Authorization token", HttpStatus.UNAUTHORIZED);
        }
        return jwtUtils.getUserIdFromToken(token);
    }

    public Role getCurrentRole() {
        String token = HelperUtil.getToke();
        if (token == null || token.isBlank()) {
            throw new CustomException("Missing Authorization token", HttpStatus.UNAUTHORIZED);
        }
        List<String> roles = jwtUtils.getRolesFromToken(token);
        if (roles == null || roles.isEmpty()) {
            throw new CustomException("No role found in JWT token", HttpStatus.UNAUTHORIZED);
        }
        return roles.stream()
                .map(role -> role != null && role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
                .filter(role -> Role.USER.name().equals(role) || Role.MENTOR.name().equals(role))
                .findFirst()
                .map(Role::valueOf)
                .orElseThrow(() -> new CustomException("Only USER or MENTOR can do this action", HttpStatus.FORBIDDEN));
    }
}
