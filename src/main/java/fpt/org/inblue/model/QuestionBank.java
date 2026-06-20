package fpt.org.inblue.model;

import fpt.org.inblue.enums.QuestionLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class QuestionBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JoinColumn(name = "question_category_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private QuestionCategory questionCategory;
    @Enumerated(EnumType.STRING)
    private QuestionLevel questionLevel;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> options; // ["A. Spring Boot", "B. Node.js", ...]

    @Column(nullable = false)
    private String correctAnswer; // "A"
    @Builder.Default
    private Boolean isDeleted = false;
}
