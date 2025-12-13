package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.enums.Role;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMentorRequest {
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
    private Map<String, String> certificateUrl;
    private int totalSession;
}
