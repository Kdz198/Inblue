package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Mentor {
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

    private String expertise;
    private String yearsOfExperience;
    private String linkedInUrl;
    private String currentCompany;
    private int rate;
    @ElementCollection
    @CollectionTable(name = "mentor_certificates", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "url")
    private Map<String, String> certificateUrl;
    private int totalSession;
}
