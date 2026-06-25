package fpt.org.inblue.repository;

import fpt.org.inblue.model.LlmChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmChatLogRepository extends JpaRepository<LlmChatLog, Long> {}
