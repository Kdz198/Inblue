package fpt.org.inblue.repository;

import fpt.org.inblue.model.Session;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findAllByUserIdOrUserId2(int userId, int userId2);

    List<Session> findAllByUserId(int userId);

    List<Session> findAllByUserId2(int userId2);

    Session findByRoomName(String roomName);
}
