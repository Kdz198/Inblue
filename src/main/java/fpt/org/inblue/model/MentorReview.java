package fpt.org.inblue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MentorReview {
    @Id
    @JoinColumn(name = "session_id")
    @OneToOne
    private Session sessionId;

    private int rating;
    private String reviewComment;

}
