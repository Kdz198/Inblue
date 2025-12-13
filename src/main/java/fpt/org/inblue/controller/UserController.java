package fpt.org.inblue.controller;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.CreateMentorRequest;
import fpt.org.inblue.model.dto.CreateUserRequest;
import fpt.org.inblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin( "*")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping("create-student")
    public ResponseEntity<User> createUser(CreateUserRequest user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("create-mentor")
    public ResponseEntity<User> createMentor(CreateMentorRequest user) {
        User createdUser = userService.createMentor(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getById(id));
    }
    @PutMapping
    public ResponseEntity<User> updateUser(User user) {
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }
}
