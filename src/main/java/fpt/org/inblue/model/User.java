package fpt.org.inblue.model;

import fpt.org.inblue.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isActive;
    private String avatarUrl;
    private String public_id;

    private String cvUrl;
    private String cv_public_id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
