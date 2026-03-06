package fpt.org.inblue.repository;

import fpt.org.inblue.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.senderId = :id1 AND m.senderType = :type1 AND m.recipientId = :id2 AND m.recipientType = :type2) OR " +
            "(m.senderId = :id2 AND m.senderType = :type2 AND m.recipientId = :id1 AND m.recipientType = :type1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> getHistory(
            @Param("id1") int id1, @Param("type1") String type1,
            @Param("id2") int id2, @Param("type2") String type2
    );
}
