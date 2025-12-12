package fpt.org.inblue.service;

import fpt.org.inblue.model.User;

import java.util.List;

public interface UserService {
    public List<User> getAll();
    public User getById(int id);
    public User createUser(User user);
    public User updateUser(User user);
    public boolean deleteUser(int id);
}
