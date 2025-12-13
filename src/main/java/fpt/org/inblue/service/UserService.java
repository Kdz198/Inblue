package fpt.org.inblue.service;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.CreateUserRequest;

import java.io.IOException;
import java.util.List;

public interface UserService {
    public List<User> getAll();
    public User getById(int id);
    public User createUser(CreateUserRequest user) throws IOException;
    public User updateUser(User user);
    public boolean deleteUser(int id);

}
