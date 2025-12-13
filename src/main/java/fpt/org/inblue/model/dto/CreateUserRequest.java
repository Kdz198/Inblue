package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
    private boolean isActive;
    private String bio;
    private String avatarUrl;
    private String university;
    private String major;
    private String targetPosition;
    private String targetLevel;
    private String cvUrl;
}
