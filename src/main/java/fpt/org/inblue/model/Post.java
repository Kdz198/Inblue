package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.PostStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int postId;
    @Column(columnDefinition = "TEXT")
    String title;
    @Column(columnDefinition = "TEXT")
    String content;
    @Column(columnDefinition = "TEXT")
    String summary;
    PostStatus status;
    @JoinColumn(name = "author_id")
    @ManyToOne
    User author;
    @CreationTimestamp
    Date creationDate;
    @UpdateTimestamp
    Date lastModifiedDate;
    @JoinColumn(name ="major_id")
    @ManyToOne
    Major major;
    String coverImgUrl;
    String public_id;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<String> tags;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "post_id", referencedColumnName = "postId")
    List<PostLike> likes = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "post_id", referencedColumnName = "postId")
    List<PostComment> comments = new ArrayList<>();

}
