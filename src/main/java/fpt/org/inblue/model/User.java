package fpt.org.inblue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isActive;
    private String avatarUrl;
    private String public_id;

    private String cvUrl;
    private String cv_public_id;

    private String phone;
    private String address;
    private String linkedInUrl;
    private String githubUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<CandidateProfile> candidates;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EntryTestAttempt> entryTestAttempts = new ArrayList<>();

    public void addEntryTestAttempt(EntryTestAttempt attempt) {
        if (attempt == null) {
            return;
        }
        entryTestAttempts.add(attempt);
        attempt.setUser(this);
    }

    public void removeEntryTestAttempt(EntryTestAttempt attempt) {
        if (attempt == null) {
            return;
        }
        entryTestAttempts.remove(attempt);
        if (attempt.getUser() == this) {
            attempt.setUser(null);
        }
    }
}
