package fpt.org.inblue.service.impl;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.CreateUserRequest;
import fpt.org.inblue.model.dto.UserEventDto;
import fpt.org.inblue.model.dto.UserInfo;
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
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @Override
    public User createUser(UserInfo user, MultipartFile avatar, MultipartFile cvFile) throws IOException {
        if(user.getId()==null) {
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
            if (!cvFile.isEmpty()) {
                String absolutePath = FileUtil.saveFile(cvFile);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "cv"));
            }
            if (!avatar.isEmpty()) {
                String absolutePath = FileUtil.saveFile(avatar);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "avatar"));
            }
            return savedUser;
        }
        else{
            User updateUser = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User Not Found"));
            updateUser.setName(user.getName());
            updateUser.setEmail(user.getEmail());
            updateUser.setBio(user.getBio());
            updateUser.setUniversity(user.getUniversity());
            updateUser.setMajor(user.getMajor());
            updateUser.setTargetPosition(user.getTargetPosition());
            updateUser.setTargetLevel(user.getTargetLevel());
            updateUser.setPassword(user.getPassword());
            if(updateUser.getAvatarUrl()!=null) {
                updateUser.setAvatarUrl(updateUser.getAvatarUrl());
                updateUser.setPublic_id(updateUser.getPublic_id());
            }
            if(updateUser.getCvUrl()!=null) {
                updateUser.setCvUrl(updateUser.getCvUrl());
                updateUser.setCv_public_id(updateUser.getCv_public_id());
            }
            User savedUser = userRepository.save(updateUser);
            if (!cvFile.isEmpty()) {
                cloudinaryService.deletePdf(updateUser.getCv_public_id());
                String absolutePath = FileUtil.saveFile(cvFile);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "cv"));
            }
            if (!avatar.isEmpty()) {
                cloudinaryService.deleteImage(updateUser.getPublic_id());
                String absolutePath = FileUtil.saveFile(avatar);
                File file = FileUtil.getFileByPath(absolutePath);
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                file.delete();
                applicationEventPublisher.publishEvent(new UserEventDto(savedUser, multipartFile, "avatar"));
            }
            return savedUser;
        }
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
