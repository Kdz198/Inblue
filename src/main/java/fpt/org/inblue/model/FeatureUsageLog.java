package fpt.org.inblue.model;

import fpt.org.inblue.enums.FeatureName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class FeatureUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int userId;
    @Enumerated(EnumType.STRING)
    FeatureName featureName;
    @CreationTimestamp
    Timestamp useAt;
}
