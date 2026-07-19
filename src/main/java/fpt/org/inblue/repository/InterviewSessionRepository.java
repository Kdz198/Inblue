package fpt.org.inblue.repository;

import fpt.org.inblue.model.InterviewSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Integer> {
    List<InterviewSession> findByUserId(Integer userId);
    InterviewSession findBySessionKey(String sessionKey);
}
