package fpt.org.inblue.repository;

import fpt.org.inblue.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findAllByUserIdOrUserId2(int userId, int userId2);

    Session findByRoomName(String roomName);
}
