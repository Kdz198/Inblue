package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.MentorProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.UserProfile;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.model.dto.CreateUserRequest;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @Override
    public User createUser(CreateUserRequest user) {
        User newUser = new User(0,user.getName(), user.getEmail(), user.getPassword(),
                user.getRole(), user.isActive(), user.getBio(), user.getAvatarUrl(),
                null, null);
        User saved = userRepository.save(newUser);
        UserProfile userProfile = new UserProfile(saved.getId(), user.getUniversity(),
                user.getMajor(), user.getTargetPosition(), user.getTargetLevel(), user.getCvUrl());
        saved.setStudentProfile(userProfile);
        return userRepository.save(saved);
    }

    @Override
    public User createMentor(CreateMentorRequest user) {
        User newUser = new User(0,user.getName(), user.getEmail(), user.getPassword(),
                user.getRole(), user.isActive(), user.getBio(), user.getAvatarUrl(),
                null, null);
        User saved = userRepository.save(newUser);
        MentorProfile mentorProfile = new MentorProfile(saved.getId(), user.getExpertise(),
                user.getYearsOfExperience(), user.getLinkedInUrl(), user.getCurrentCompany(),
                user.getRate(), user.getCertificateUrl(), user.getTotalSession());
        saved.setMentorProfile(mentorProfile);
        return userRepository.save(saved);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean deleteUser(int id) {
        if(userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
