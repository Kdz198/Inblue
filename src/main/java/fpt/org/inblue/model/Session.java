package fpt.org.inblue.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    // Thông tin người dùng 1 ( mentee )
    private int userId;
    private String participantId1;
    private Timestamp startTime1;
    private Timestamp endTime1;
    private Long durationSeconds1;

    // Thông tin người dùng 2 ( mentor )
    private int userId2;
    private String participantId2;
    private Timestamp startTime2;
    private Timestamp endTime2;
    private Long durationSeconds2;
    // URL phòng Daily.co
    private String roomUrl;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss.SSS",
            timezone = "Asia/Ho_Chi_Minh"
    )
    private Timestamp joinTime;

    private String recordUrl;

    private SessionStatus status;
}
