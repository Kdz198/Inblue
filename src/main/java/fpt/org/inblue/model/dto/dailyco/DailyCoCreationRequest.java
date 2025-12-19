package fpt.org.inblue.model.dto.dailyco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyCoCreationRequest {
    String name;
    String privacy;
    Properties properties;

    @Data
    public static class Properties {
        int max_participants;
        boolean start_video_off;
        boolean start_audio_off;
        boolean enable_screenshare;
        int exp;
        //thời gian hết hạn này là thời gian hết hạn mà người dùng đc join vào room, sau thời gian này room vẫn tồn tại nhưng ko ai join đc
        private String enable_recording;
    }
}
