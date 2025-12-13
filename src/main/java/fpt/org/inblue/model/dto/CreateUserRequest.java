package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.enums.Role;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private String bio;
    @Nullable
    MultipartFile avatar ;

    private String university;
    private String major;
    private String targetPosition;
    private String targetLevel;
    @Nullable
    MultipartFile cvFile;
}
