package fpt.org.inblue.service.impl;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.dto.request.OrchestratorRequest.*;
import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import fpt.org.inblue.model.enums.InterviewEnums.*;
import fpt.org.inblue.service.InterviewSessionService;
import fpt.org.inblue.service.PythonApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl implements InterviewSessionService {


    private final PythonApiClient pythonApiClient;
    private record jobDescription (String jd_text) {}

    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public JobRequirementData getJobRequirementFromJD(String description) {

        return pythonApiClient.callApi(
                ApiPath.JD_API,
                HttpMethod.POST,
                new jobDescription(description),
                JobRequirementData.class
        );
    }

    @Override
    public Map<String, Object> getInterviewConfigOptions() {
        Map<String, Object> options = new HashMap<>();

        // 1. Danh sách Interview Mode
        options.put("interview_modes", Arrays.stream(InterviewMode.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        // 2. Danh sách Difficulty
        options.put("difficulties", Arrays.stream(DifficultyLevel.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        // 3. Danh sách Language
        options.put("languages", Arrays.stream(Language.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        return options;
    }



    // Helper method để convert Enum thành Map cho gọn code
    private Map<String, String> convertEnumToMap(Object enumVal) {
        Map<String, String> map = new HashMap<>();

        // Dùng reflection hoặc ép kiểu để lấy dữ liệu (vì các Enum đều có cấu trúc giống nhau)
        if (enumVal instanceof InterviewMode e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        } else if (enumVal instanceof DifficultyLevel e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        } else if (enumVal instanceof Language e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        }

        return map;
    }
}
