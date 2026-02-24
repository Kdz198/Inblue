package fpt.org.inblue.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceSnapshotResponse {

    // Trạng thái chung: "NORMAL", "WARNING", "ERROR"
    private String status;

    // Loại cảnh báo (Lưu DB/Redis): "EYE_LEFT", "HEAD_DOWN"...
    @JsonProperty("warning_type")
    private String warningType;

    // Câu thông báo (Hiển thị FE nếu cần): "LIẾC TRÁI", "CÚI ĐẦU"...
    private String message;

    // Các thông số chi tiết (Tọa độ, góc quay...)
    private Metrics metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private Double width;
        private Double pitch;
        private Double yaw;

        @JsonProperty("gaze_x")
        private Double gazeX;

        @JsonProperty("gaze_y")
        private Double gazeY;
    }
}