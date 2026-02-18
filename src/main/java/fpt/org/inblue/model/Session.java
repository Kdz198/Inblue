package fpt.org.inblue.model;

import fpt.org.inblue.model.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String roomName;
    // Thông tin người dùng 1
    private int userId;
    private String participantId1;
    private Timestamp startTime1;
    private Timestamp endTime1;
    private Long durationSeconds1;

    // Thông tin người dùng 2
    private int userId2;
    private String participantId2;
    private Timestamp startTime2;
    private Timestamp endTime2;
    private Long durationSeconds2;
    // URL phòng Daily.co
    private String roomUrl;
    private Timestamp joinTime;

    private String recordUrl;

    private SessionStatus status;
}
