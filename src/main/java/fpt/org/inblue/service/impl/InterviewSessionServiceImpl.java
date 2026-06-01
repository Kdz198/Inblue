package fpt.org.inblue.service.impl;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.enums.InterviewEnums;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.caching.InterviewSessionRedis;
import fpt.org.inblue.model.dto.request.InterviewSetupRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest;
import fpt.org.inblue.model.dto.request.OrchestratorRequest.JobRequirementData;
import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.caching.InterviewSessionRedisRepository;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.InterviewSessionService;
import fpt.org.inblue.service.LLMApiClient;
import fpt.org.inblue.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl implements InterviewSessionService {


    private final LLMApiClient LLMApiClient;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewSessionRedisRepository sessionRedisRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ApplicationEventPublisher applicationEventPublisher;

    private record jobDescription (String jd_text) {}

    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public JobRequirementData getJobRequirementFromJD(String description) {

        return LLMApiClient.callApi(
                PythonService.LLM,
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
        options.put("interview_modes", Arrays.stream(InterviewEnums.InterviewMode.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        // 2. Danh sách Difficulty
        options.put("difficulties", Arrays.stream(InterviewEnums.DifficultyLevel.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        // 3. Danh sách Language
        options.put("languages", Arrays.stream(InterviewEnums.Language.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        options.put("domains", Arrays.stream(InterviewEnums.JobDomain.values())
                .map(this::convertEnumToMap)
                .collect(Collectors.toList()));

        return options;
    }


    @Transactional
    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public String createSession(InterviewSetupRequest request)  {

        OrchestratorRequest pythonPayload = OrchestratorRequest.builder()
                .candidateProfile(request.getCandidateProfile())
                .jobRequirement(request.getJobRequirement())
                .sessionConfig(request.getSessionConfig())
                .build();


        InterviewBlueprintResponse blueprint = LLMApiClient.callApi(
                PythonService.LLM,
                ApiPath.ORCHESTRATOR_API,
                HttpMethod.POST,
                pythonPayload,
                InterviewBlueprintResponse.class
        );

        if (blueprint == null || blueprint.getBlueprint() == null || blueprint.getBlueprint().isEmpty()) {
            throw new RuntimeException("Python Orchestrator trả về Blueprint rỗng!");
        }

        //Todo sau này async khúc save vô DB

        User user = User.builder().id(request.getUserId()).build();

        String sessionKey = UUID.randomUUID().toString();

        InterviewSession session = InterviewSession.builder()
                .user(user) // Lấy từ FE
                .blueprint(blueprint)        // Lưu JSON Blueprint
                .sessionKey(sessionKey)

                // Lưu Snapshot dữ liệu đầu vào (để sau này đối chứng)
                .candidateProfile(request.getCandidateProfile())
                .jobRequirement(request.getJobRequirement())
                .sessionConfig(request.getSessionConfig())

                // Các field indexing
                .mode(request.getSessionConfig().getInterviewMode())
                .domain(request.getSessionConfig().getDomain())
                .status(InterviewSession.SessionStatus.CREATED)

                .build();

        // Save xuống DB (Hibernate tự handle JSONB)
        session = sessionRepository.save(session);
        String sessionId = session.getId().toString();

        InterviewSessionRedis sessionRedis = InterviewSessionRedis.builder()
                .id(sessionKey)
                .dbId(session.getId())
                .blueprint(blueprint)
                .currentPhaseIndex(0)
                .currentQuestionIndex(0)
                .build();
        sessionRedisRepository.save(sessionRedis);
        return sessionKey;
    }

    @Override
    public List<InterviewSession> getAllSessionsForUser(Integer userId) {

        List<InterviewSession> sessions = sessionRepository.findByUserId(userId);

        for (InterviewSession session : sessions) {
            if (session.getStatus() == InterviewSession.SessionStatus.IN_PROGRESS) {
                // Kiểm tra Redis xem session này còn tồn tại không
                Optional<InterviewSessionRedis> redisOpt = sessionRedisRepository.findById(session.getSessionKey());
                if (redisOpt.isEmpty()) {
                    // Nếu Redis đã bị xóa (hết TTL), cập nhật trạng thái session thành CANCELLED
                    session.setStatus(InterviewSession.SessionStatus.CANCELLED);
                    sessionRepository.save(session);
                    System.out.println( "Session " + session.getId() + " đã bị hủy do không còn trong Redis.");
                }
            }

            if( session.getStatus() == InterviewSession.SessionStatus.CANCELLED) {
                // Nếu chưa hoàn thành, xóa blueprint để giảm tải dữ liệu trả về cho FE
                session.setBlueprint( null);
            }
        }




        return  sessionRepository.saveAll(sessions);
    }

    @Override
    public InterviewSession getSessionById(Integer sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session không tồn tại với ID: " + sessionId));
    }

    @Override
    public InterviewSessionRedis getSessionFromCache(String sesssionKey) {
            Optional<InterviewSessionRedis> sessionOpt = sessionRedisRepository.findById(sesssionKey);
            if (sessionOpt.isPresent()) {
                return sessionOpt.get();
            }
        throw new RuntimeException("Session không tồn tại trong cache hoặc đã hết hạn!");
    }

    // Helper method để convert Enum thành Map cho gọn code
    private Map<String, String> convertEnumToMap(Object enumVal) {
        Map<String, String> map = new HashMap<>();

        // Dùng reflection hoặc ép kiểu để lấy dữ liệu (vì các Enum đều có cấu trúc giống nhau)
        if (enumVal instanceof InterviewEnums.InterviewMode e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        } else if (enumVal instanceof InterviewEnums.DifficultyLevel e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        } else if (enumVal instanceof InterviewEnums.Language e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        }
        else if (enumVal instanceof InterviewEnums.JobDomain e) {
            map.put("key", e.name());
            map.put("label", e.getLabel());
            map.put("description", e.getDescription());
        }

        return map;
    }
}
