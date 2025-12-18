package fpt.org.inblue.repository;

import fpt.org.inblue.model.Mentor;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentorRepository extends JpaRepository<Mentor, Integer> {
    // Cập nhật Avatar
    @Modifying
    @Transactional
    @Query("UPDATE Mentor m SET m.avatarUrl = :url, m.public_id = :publicId WHERE m.id = :id")
    void updateAvatar(@Param("id") int id, @Param("url") String url, @Param("publicId") String publicId);

    // Cập nhật Identity Card
    @Modifying
    @Transactional
    @Query("UPDATE Mentor m SET m.identityImg = :url, m.public_id_identity = :publicId WHERE m.id = :id")
    void updateIdentityCard(@Param("id") int id, @Param("url") String url, @Param("publicId") String publicId);

    // Cập nhật Degree
    @Modifying
    @Transactional
    @Query("UPDATE Mentor m SET m.degreeImg = :url, m.public_id_degree = :publicId WHERE m.id = :id")
    void updateDegree(@Param("id") int id, @Param("url") String url, @Param("publicId") String publicId);

    // Cập nhật Other File
    @Modifying
    @Transactional
    @Query("UPDATE Mentor m SET m.otherFile = :url, m.public_id_other = :publicId WHERE m.id = :id")
    void updateOtherFile(@Param("id") int id, @Param("url") String url, @Param("publicId") String publicId);
}
