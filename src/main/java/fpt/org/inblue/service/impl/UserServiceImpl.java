package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.CreateUserRequest;
import fpt.org.inblue.model.dto.UserCvDtoRequest;
import fpt.org.inblue.model.enums.Role;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.UserService;
import fpt.org.inblue.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @Override
    public User createUser(CreateUserRequest user) throws IOException {
        User userBuilder = User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(Role.USER)
                .isActive(true)
                .bio(user.getBio())
                .university(user.getUniversity())
                .major(user.getMajor())
                .targetPosition(user.getTargetPosition())
                .targetLevel(user.getTargetLevel())
                .build();
        User savedUser = userRepository.save(userBuilder);
        if (!user.getCvFile().isEmpty()) {
            String absolutePath = FileUtil.saveFile(user.getCvFile());
            File file = FileUtil.getFileByPath(absolutePath);
            MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
            file.delete();
            applicationEventPublisher.publishEvent(new UserCvDtoRequest(savedUser, multipartFile,"cv"));
        }
        if(!user.getAvatar().isEmpty()) {
            String absolutePath = FileUtil.saveFile(user.getAvatar());
            File file = FileUtil.getFileByPath(absolutePath);
            MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
            file.delete();
            applicationEventPublisher.publishEvent(new UserCvDtoRequest(savedUser, multipartFile,"avatar"));
        }
        return savedUser;
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
