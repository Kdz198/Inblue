package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.UserProfile;
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
    public User createUser(User user) {
        User saved = userRepository.save(user);
        int id = saved.getId();
        UserProfile userProfile = new UserProfile(id,user.getStudentProfile().getUniversity(),
                user.getStudentProfile().getMajor(),
                user.getStudentProfile().getTargetPosition(),
                user.getStudentProfile().getTargetLevel(),
                user.getStudentProfile().getCvUrl());
        user.setStudentProfile(userProfile);
        return saved;
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
