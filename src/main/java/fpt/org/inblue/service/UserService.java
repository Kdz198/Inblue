package fpt.org.inblue.service;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.CreateUserRequest;
import fpt.org.inblue.model.dto.UserInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    public List<User> getAll();
    public User getById(int id);
    public User createUser(UserInfo data, MultipartFile avatar, MultipartFile cvFile) throws IOException;
    public User updateUser(User user);
    public boolean deleteUser(int id);

}
