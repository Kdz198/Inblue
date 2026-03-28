package fpt.org.inblue.security;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MentorRepository mentorRepository;

    @NotNull
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if(!user.getIsActive()){
            throw new UsernameNotFoundException("User is not active");
        }
        if(user != null){
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_"+user.getRole())
            );
            return new CustomUserDetails(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    authorities,
                    user.getIsActive()
            );
        }
        else{
            Mentor mentor = mentorRepository.findByEmail(email);
            if(mentor != null && mentor.isActive()){
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_"+mentor.getRole())
                );
                return new CustomUserDetails(
                        mentor.getId(),
                        mentor.getEmail(),
                        mentor.getPassword(),
                        authorities,
                        mentor.isActive()
                );

            }
            else{
                throw new UsernameNotFoundException("User not found");
            }
        }
    }



}
