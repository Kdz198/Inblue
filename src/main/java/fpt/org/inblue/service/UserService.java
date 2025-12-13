package fpt.org.inblue.service;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.model.dto.CreateUserRequest;

import java.util.List;

public interface UserService {
    public List<User> getAll();
    public User getById(int id);
    public User createUser(CreateUserRequest user);
    public User updateUser(User user);
    public boolean deleteUser(int id);
    public User createMentor(CreateMentorRequest mentor);
}
