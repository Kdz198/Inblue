package fpt.org.inblue.security;

import java.util.Collection;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class CustomUserDetails implements UserDetails {
    private final int userId;

    private final String email;
    private final String name;
    private final String avatarUrl;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public CustomUserDetails(
            int userId,
            String email,
            String name,
            String avatarUrl,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean active) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
    }

    public CustomUserDetails(
            int userId,
            String email,
            String name,
            String avatarUrl,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.password = null;
        this.authorities = authorities;
        this.active = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
