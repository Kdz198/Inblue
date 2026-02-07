package fpt.org.inblue.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Builder
//unique chỉ đc 1 value cho mỗi cột trong bảng còn dùng uniqueConstraint thì kết hợp nhiều cột với nhau để tạo thành 1 giá trị duy nhất
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @CreationTimestamp
    Timestamp createdAt;
}

