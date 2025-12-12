package fpt.org.inblue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
public class UserProfile {
    @Id
    private int userId;
    private String university;
    private String major;
    private String targetPosition;
    private String targetLevel;
    private String cvUrl;
}