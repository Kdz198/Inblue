package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class MentorProfile {
    @Id
    private int userId;
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
