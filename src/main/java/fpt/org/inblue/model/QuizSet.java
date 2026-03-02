package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class QuizSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int quizId;
    String quizName;
    double score;
    @JoinColumn(name = "practice_set_id")
    @ManyToOne
    PracticeSet practiceSet;
    @CreationTimestamp
    Timestamp createdAt;
    boolean isSubmitted;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<QuizItem> questions;
}
