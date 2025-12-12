package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private boolean isActive;
    private String bio;
    private String avatarUrl;
    @JoinColumn(name = "mentor_profile")
    @OneToOne
    private MentorProfile mentorProfile;
    @JoinColumn(name = "student_profile")
    @OneToOne
    private UserProfile studentProfile;
}
