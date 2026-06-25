package fpt.org.inblue.service.impl;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.caching.InterviewBehaviorRedis;
import fpt.org.inblue.model.dto.request.FaceSnapshotRequest;
import fpt.org.inblue.model.dto.response.FaceSnapshotResponse;
import fpt.org.inblue.repository.caching.InterviewBehaviorRedisRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.ProctoringService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProctoringServiceImpl implements ProctoringService {

    private final InterviewBehaviorRedisRepository behaviorRedisRepo;
    private final ApiClient ApiClient;

    @Override
    public void trackSnapshot(FaceSnapshotRequest request) {
        FaceSnapshotResponse response = ApiClient.callApi(
                PythonService.VISION,
                ApiPath.PROCTORING_SNAPSHOT_API,
                HttpMethod.POST,
                request,
                FaceSnapshotResponse.class);
        log.info(
                "Nhận phản hồi từ Python Proctoring API cho session [{}]: status={}, warningType={}",
                request.getSessionKey(),
                response.getStatus(),
                response.getWarningType());

        if ("WARNING".equals(response.getStatus())) {
            InterviewBehaviorRedis behaviorDoc = behaviorRedisRepo
                    .findById(request.getSessionKey())
                    .orElse(InterviewBehaviorRedis.builder()
                            .sessionKey(request.getSessionKey())
                            .build());

            behaviorDoc
                    .getBehavioralRecord()
                    .computeIfAbsent(request.getGlobalQuestionOrder(), k -> new ArrayList<>())
                    .add(response.getWarningType());

            behaviorRedisRepo.save(behaviorDoc);
        }
    }

    @Override
    public Map<Integer, List<String>> getAndClearBehavioralRecord(String sessionKey) {
        InterviewBehaviorRedis behaviorDoc =
                behaviorRedisRepo.findById(sessionKey).orElse(null);

        Map<Integer, List<String>> record = (behaviorDoc != null) ? behaviorDoc.getBehavioralRecord() : new HashMap<>();

        if (behaviorDoc != null) {
            behaviorRedisRepo.deleteById(sessionKey);
        }

        return record;
    }
}
