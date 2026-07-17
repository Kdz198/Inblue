package fpt.org.inblue.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private int expiration;

    public String generateToken(Authentication auth) {
        CustomUserDetails userPrincipal = (CustomUserDetails) auth.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(String.valueOf(userPrincipal.getUserId()))
                .claim("roles", roles)
                .claim("email", userPrincipal.getEmail())
                .claim("name", userPrincipal.getName())
                .claim("avatarUrl", userPrincipal.getAvatarUrl() != null ? userPrincipal.getAvatarUrl() : "")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(
                        io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes()),
                        io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> getRolesFromToken(String token) {
        return (List<String>) getClaimsFromToken(token).get("roles");
    }

    public Integer getUserIdFromToken(String token) {
        return Integer.valueOf(getClaimsFromToken(token).getSubject());
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    public String getNameFromToken(String token) {
        return getClaimsFromToken(token).get("name", String.class);
    }

    public String getAvatarUrlFromToken(String token) {
        return getClaimsFromToken(token).get("avatarUrl", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
