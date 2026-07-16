package fpt.org.inblue.security;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MentorRepository mentorRepository;

    @NotNull
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("CustomUserDetailService: Loading user by email: " + email);
        User user = userRepository.findByEmail(email);
        if (user != null && user.getIsActive()) {
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
            return new CustomUserDetails(
                    user.getId(), user.getEmail(), user.getName(), user.getPassword(), authorities, user.getIsActive());
        }
        Mentor mentor = mentorRepository.findByEmail(email);

        if (mentor != null && mentor.isActive()) {
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + mentor.getRole()));
            return new CustomUserDetails(
                    mentor.getId(),
                    mentor.getEmail(),
                    mentor.getName(),
                    mentor.getPassword(),
                    authorities,
                    mentor.isActive());
        }
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
