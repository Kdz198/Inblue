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

    Mentor getMentorById(int id);

    Mentor findByEmail(String email);

    int countMentorByIsActive(boolean active);
}
