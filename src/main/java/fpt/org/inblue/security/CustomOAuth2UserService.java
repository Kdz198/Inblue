package fpt.org.inblue.security;

import fpt.org.inblue.model.Mentor;
import fpt.org.inblue.model.User;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.repository.MentorRepository;
import fpt.org.inblue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MentorRepository mentorRepository;

    private User linkUserAccount(String email, String name) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setIsActive(true);
            user.setRole(Role.USER);
            userRepository.save(user);
        }
        return user;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        Mentor mentor = mentorRepository.findByEmail(email);
        if(mentor != null && mentor.isActive()){
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + mentor.getRole().toString()));
            DefaultOAuth2User defaultUser = new DefaultOAuth2User(authorities, attributes, "sub");
            return new CustomOAuth2User(defaultUser, mentor.getId(), mentor.getRole().toString());
        }
        else {
            User user = linkUserAccount(email, name);
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()));
            DefaultOAuth2User defaultUser = new DefaultOAuth2User(authorities, attributes, "sub");
            return new CustomOAuth2User(defaultUser, user.getId(), user.getRole().toString());
        }

    }
}
