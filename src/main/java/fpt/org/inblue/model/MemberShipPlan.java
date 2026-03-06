package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.PlanName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.UniqueElements;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class MemberShipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    PlanName name;
    int price;
    int max_ai_interview;
    int max_practice_sets;
    int max_quiz_sets;
    Integer durationDays;       // Gói FREE thì = 0

    //gói new: 0
    // gói free: 1 tháng reset 1 lần (2,2,2)
}
