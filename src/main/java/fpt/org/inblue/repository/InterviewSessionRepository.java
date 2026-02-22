package fpt.org.inblue.repository;

import fpt.org.inblue.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Integer> {
    List<InterviewSession> findByUserId(Integer userId);
}
