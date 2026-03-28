package fpt.org.inblue.security;

import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class CustomUserDetails implements UserDetails {
    private final int userId;

    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public CustomUserDetails(int userId,  String email, String password, Collection<? extends GrantedAuthority> authorities, boolean active) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
    }

    public CustomUserDetails(int userId,Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = null;
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
