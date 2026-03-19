package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
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
    private String public_id;

    private String expertise;
    private int yearsOfExperience;
    private String linkedInUrl;
    private String currentCompany;
    private String identityImg;
    private String public_id_identity;

    private String degreeImg;
    private String public_id_degree;

    private String otherFile;
    private String public_id_other;
    private int totalSession;
    private double averageRating;
}
