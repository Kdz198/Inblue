package fpt.org.inblue.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceSnapshotRequest {
    private String sessionKey;
    private int globalQuestionOrder; // Index hiện tại FE đang hiển thị
    private String imageBase64;
}
