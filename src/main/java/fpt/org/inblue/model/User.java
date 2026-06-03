package fpt.org.inblue.model;

import fpt.org.inblue.enums.Major;
import fpt.org.inblue.enums.Role;
import jakarta.persistence.*;
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

    private String university;
    @Enumerated(EnumType.STRING)
    private Major major;
    private String cvUrl;
    private String cv_public_id;
}
