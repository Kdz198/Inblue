package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.FaceSnapshotRequest;

import java.util.List;
import java.util.Map;

public interface ProctoringService {

    // FE gọi hàm này liên tục để track
    void trackSnapshot(FaceSnapshotRequest request);

    // Main Service gọi hàm này lúc kết thúc phỏng vấn để "thu hoạch"
    Map<Integer, List<String>> getAndClearBehavioralRecord(String sessionKey);
}
