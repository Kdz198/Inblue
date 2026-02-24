package fpt.org.inblue.service.impl;


import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.model.InterviewResultDetail;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.caching.InterviewSessionRedis;
import fpt.org.inblue.model.dto.request.OrchestratorConductRequest;
import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.GradingResponse;
import fpt.org.inblue.model.dto.response.OrchestratorAnalysisResponse;
import fpt.org.inblue.model.dto.response.QuestionResponse;
import fpt.org.inblue.model.enums.PythonService;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.caching.InterviewSessionRedisRepository;
import fpt.org.inblue.service.InterviewProcessService;
import fpt.org.inblue.service.ProctoringService;
import fpt.org.inblue.service.PythonApiClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewProcessServiceImpl implements InterviewProcessService {

    private final InterviewSessionRedisRepository redisRepository;
    private final InterviewSessionRepository sessionRepository;
    private final PythonApiClient pythonApiClient;
    private final ProctoringService proctoringService;

    @Override
    public QuestionResponse getCurrentQuestion(String sessionKey) {
        InterviewSessionRedis session = redisRepository.findById(sessionKey)
                .orElseThrow(() -> new RuntimeException("Session not found or expired"));

        if (session.getCurrentQuestionText() == null) {
            var firstQ = session.getBlueprint().getBlueprint().get(0).getQuestions().get(0);
            session.setCurrentQuestionText(firstQ.getQuestionText());
            session.setCurrentQuestionType(InterviewSessionRedis.QuestionType.BLUEPRINT);
            redisRepository.save(session);
        }

        return buildQuestionResponse(session);
    }

    @Override
    public QuestionResponse submitAnswer(SubmitAnswerRequest request) {
        // 1. Lấy Session
        InterviewSessionRedis session = redisRepository.findById(request.getSessionKey())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // --- BƯỚC 1: LƯU CÂU TRẢ LỜI CŨ ---
        var currentPhase = session.getBlueprint().getBlueprint().get(session.getCurrentPhaseIndex());

        var typeToSave = session.getCurrentQuestionType() != null ? session.getCurrentQuestionType() : InterviewSessionRedis.QuestionType.BLUEPRINT;
        var textToSave = session.getCurrentQuestionText() != null ? session.getCurrentQuestionText() :
                currentPhase.getQuestions().get(session.getCurrentQuestionIndex()).getQuestionText();

        var exchange = InterviewSessionRedis.InterviewExchange.builder()
                .phaseName(currentPhase.getPhaseName())
                .questionOrder(session.getCurrentQuestionIndex())
                .questionText(textToSave)
                .answerText(request.getAnswer())
                .type(typeToSave)
                .submittedAt(LocalDateTime.now().toString())
                .build();

        session.getChatHistory().add(exchange);

        // --- BƯỚC 2: CHUẨN BỊ CONTEXT & GỌI AI ---
        var contextExchanges = getContextForAI(session.getChatHistory());
        var currentAnchorInfo = getCurrentAnchorInfo(session);
        var nextAnchorInfo = peekNextBlueprintQuestion(session);

        // [FIXED] Truyền session vào đây
        var pythonRequest = buildPythonRequest(currentAnchorInfo, nextAnchorInfo, contextExchanges, session);

        OrchestratorAnalysisResponse aiResponse = pythonApiClient.callApi(
                PythonService.LLM,
                ApiPath.ANALYZER_API,
                HttpMethod.POST,
                pythonRequest,
                OrchestratorAnalysisResponse.class
        );


        // --- BƯỚC 3: XỬ LÝ RESPONSE AI ---
        if (aiResponse.getAction() == OrchestratorAnalysisResponse.AnalysisAction.DRILL_DOWN) {
            session.setCurrentQuestionType(InterviewSessionRedis.QuestionType.FOLLOW_UP);
            session.setCurrentQuestionText(aiResponse.getResponseText());
        } else {
            if (nextAnchorInfo == null) {
                finishSession(session, request.getSessionKey());
                return QuestionResponse.builder()
                        .isFinished(true)
                        .sessionKey(session.getId())
                        .questionContent("Cảm ơn bạn đã hoàn thành buổi phỏng vấn!")
                        .build();
            }

            session.setCurrentPhaseIndex(nextAnchorInfo.getPhaseIndex());
            session.setCurrentQuestionIndex(nextAnchorInfo.getQuestionIndex());
            session.setCurrentQuestionType(InterviewSessionRedis.QuestionType.BLUEPRINT);
            session.setCurrentQuestionText(aiResponse.getResponseText());
        }

        redisRepository.save(session);
        return buildQuestionResponse(session);
    }

    // ======================================================================
    // HELPERS
    // ======================================================================

    private QuestionResponse buildQuestionResponse(InterviewSessionRedis session) {
        var phases = session.getBlueprint().getBlueprint();
        var currentPhase = phases.get(session.getCurrentPhaseIndex());

        return QuestionResponse.builder()
                .sessionKey(session.getId())
                .isFinished(false)
                .phaseName(currentPhase.getPhaseName())
                .currentQuestionIndex(session.getCurrentQuestionIndex() + 1)
                .totalQuestionsInPhase(currentPhase.getQuestions().size())
                .questionContent(session.getCurrentQuestionText())
                .questionType(session.getCurrentQuestionType().name())
                .build();
    }

    private List<InterviewSessionRedis.InterviewExchange> getContextForAI(List<InterviewSessionRedis.InterviewExchange> fullHistory) {
        List<InterviewSessionRedis.InterviewExchange> context = new ArrayList<>();
        for (int i = fullHistory.size() - 1; i >= 0; i--) {
            var item = fullHistory.get(i);
            context.add(0, item);
            if (item.getType() == InterviewSessionRedis.QuestionType.BLUEPRINT) {
                break;
            }
        }
        return context;
    }

    private AnchorInfo getCurrentAnchorInfo(InterviewSessionRedis session) {
        var phase = session.getBlueprint().getBlueprint().get(session.getCurrentPhaseIndex());
        var question = phase.getQuestions().get(session.getCurrentQuestionIndex());
        return new AnchorInfo(session.getCurrentPhaseIndex(), session.getCurrentQuestionIndex(), question.getQuestionText(), phase.getPhaseName());
    }

    @Data
    @AllArgsConstructor
    private static class NextAnchorInfo {
        int phaseIndex;
        int questionIndex;
        String questionText;
    }

    private NextAnchorInfo peekNextBlueprintQuestion(InterviewSessionRedis session) {
        var blueprint = session.getBlueprint().getBlueprint();
        int p = session.getCurrentPhaseIndex();
        int q = session.getCurrentQuestionIndex();

        if (q + 1 < blueprint.get(p).getQuestions().size()) {
            return new NextAnchorInfo(p, q + 1, blueprint.get(p).getQuestions().get(q + 1).getQuestionText());
        }
        if (p + 1 < blueprint.size()) {
            return new NextAnchorInfo(p + 1, 0, blueprint.get(p + 1).getQuestions().get(0).getQuestionText());
        }
        return null;
    }

    // [FIXED] Đã thêm tham số session vào đây
    private OrchestratorConductRequest buildPythonRequest(
            AnchorInfo current,
            NextAnchorInfo next,
            List<InterviewSessionRedis.InterviewExchange> exchanges,
            InterviewSessionRedis session // <--- Thêm cái này
    ) {
        List<OrchestratorConductRequest.HistoryItem> historyItems = new ArrayList<>();

        for (var ex : exchanges) {
            historyItems.add(OrchestratorConductRequest.HistoryItem.builder().role("AI").content(ex.getQuestionText()).build());
            historyItems.add(OrchestratorConductRequest.HistoryItem.builder().role("USER").content(ex.getAnswerText()).build());
        }

        var reqCurrent = OrchestratorConductRequest.AnchorInfo.builder()
                .questionText(current.getQuestionText())
                .phaseName(current.getPhaseName())
                .build();

        OrchestratorConductRequest.AnchorInfo reqNext = null;
        if (next != null) {
            // [FIXED] Lấy tên Phase từ Blueprint trong Session (RAM) chứ không query DB
            String nextPhaseName = "";
            try {
                nextPhaseName = session.getBlueprint().getBlueprint().get(next.phaseIndex).getPhaseName();
            } catch (Exception e) {
                nextPhaseName = "Next Phase";
            }

            reqNext = OrchestratorConductRequest.AnchorInfo.builder()
                    .questionText(next.getQuestionText())
                    .phaseName(nextPhaseName)
                    .build();
        }

        return OrchestratorConductRequest.builder()
                .currentAnchor(reqCurrent)
                .nextAnchor(reqNext)
                .contextHistory(historyItems)
                .build();
    }


    private void finishSession(InterviewSessionRedis redisSession, String sessionKey) {
        InterviewSession dbSession = sessionRepository.findById(redisSession.getDbId())
                .orElseThrow(() -> new RuntimeException("DB Session not found"));

        // 1. [MỚI] THU HOẠCH BEHAVIOR TỪ PROCTORING SERVICE
        // Lấy data và đồng thời xóa luôn bản ghi trong Redis cho sạch sẽ
        Map<Integer, List<String>> behaviorMap = proctoringService.getAndClearBehavioralRecord(redisSession.getId());

        // 2. [CẬP NHẬT] Truyền behaviorMap vào hàm xử lý
        List<InterviewResultDetail.QAResult> gradedHistory = gradeAndMapFullHistory(redisSession.getChatHistory(), behaviorMap);

        // Tính điểm trung bình (Chỉ tính trên các câu có điểm - tức là câu Anchor)
        double avgScore = gradedHistory.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(InterviewResultDetail.QAResult::getScore)
                .average().orElse(0.0);

        InterviewResultDetail resultDetail = InterviewResultDetail.builder()
                .history(gradedHistory)
                .aiOverviewFeedback("Đã chấm điểm xong.")
                .build();

        dbSession.setResultDetail(resultDetail);
        dbSession.setOverallScore(avgScore);
        dbSession.setStatus(InterviewSession.SessionStatus.COMPLETED);
        dbSession.setResult(determineEvaluationResult(avgScore));
        dbSession.setCompletedAt(LocalDateTime.now());

        sessionRepository.save(dbSession);
        redisRepository.delete(redisSession);
    }


    // ======================================================================
    // LOGIC GROUPING & CHẤM ĐIỂM (GIỮ LẠI FULL HISTORY)
    // ======================================================================

    private List<InterviewResultDetail.QAResult> gradeAndMapFullHistory(List<InterviewSessionRedis.InterviewExchange> fullHistory, Map<Integer, List<String>> behaviorMap) {
        List<InterviewResultDetail.QAResult> finalResults = new ArrayList<>();
        List<InterviewSessionRedis.InterviewExchange> currentGroup = new ArrayList<>();

        // Biến này để đánh số thứ tự tăng dần toàn cục (0, 1, 2, 3...) thay vì reset theo phase
        int globalOrderCounter = 0;

        for (var exchange : fullHistory) {
            // Nếu gặp Mỏ neo MỚI và group cũ đã có dữ liệu -> Xử lý group cũ
            if (exchange.getType() == InterviewSessionRedis.QuestionType.BLUEPRINT && !currentGroup.isEmpty()) {
                // Xử lý group cũ: Chấm điểm và Add tất cả vào finalResults
                processGroupAndAddToResult(currentGroup, finalResults, globalOrderCounter, behaviorMap);

                // Cập nhật lại global counter (cộng thêm số lượng câu hỏi trong group vừa xử lý)
                globalOrderCounter += currentGroup.size();

                currentGroup.clear();
            }
            currentGroup.add(exchange);
        }

        // Xử lý group cuối cùng
        if (!currentGroup.isEmpty()) {
            processGroupAndAddToResult(currentGroup, finalResults, globalOrderCounter, behaviorMap);
        }

        return finalResults;
    }

    private void processGroupAndAddToResult(
            List<InterviewSessionRedis.InterviewExchange> group,
            List<InterviewResultDetail.QAResult> finalResults,
            int startOrderIndex,
            Map<Integer, List<String>> behaviorMap // [THÊM MỚI] Map chứa lỗi hành vi từ Redis
    ) {
        // 1. CHẤM ĐIỂM CẢ GROUP (Lấy điểm cho chủ đề này)
        GradingResponse gradingRes;
        try {
            // Gửi cả list (Anchor + Follow-ups) qua Python
            gradingRes = pythonApiClient.callApi(
                    PythonService.LLM,
                    ApiPath.GRADING_API,
                    HttpMethod.POST,
                    group,
                    GradingResponse.class
            );
        } catch (Exception e) {
            System.err.println("Grading error: " + e.getMessage());
            gradingRes = new GradingResponse(0.0, "Lỗi hệ thống chấm điểm", "");
        }

        // 2. MAP TỪNG ITEM TRONG GROUP RA KẾT QUẢ (Để không bị mất câu bồi)
        for (int i = 0; i < group.size(); i++) {
            var ex = group.get(i);

            // Tính index tuyến tính (global index) của câu hiện tại
            int currentGlobalIndex = startOrderIndex + i;

            // [LOGIC MỚI] MÓC LỖI TỪ RỔ RA: Lấy danh sách cảnh báo của đúng câu hỏi này.
            // Nếu câu này ứng viên ngoan, không có lỗi -> Trả về list rỗng
            List<String> warnings = behaviorMap.getOrDefault(currentGlobalIndex, new ArrayList<>());

            // Tạo builder cơ bản
            var qaBuilder = InterviewResultDetail.QAResult.builder()
                    .questionOrder(currentGlobalIndex) // Dùng luôn biến vừa tính
                    .questionText(ex.getQuestionText())
                    .answerText(ex.getAnswerText())
                    .behavioralWarnings(warnings);     // [GẮN VÀO ĐÂY] Mọi câu hỏi (neo hay bồi) đều có behavior

            // 3. GẮN ĐIỂM VÀ FEEDBACK
            // Logic: Chỉ gắn điểm vào câu đầu tiên (Anchor - Mỏ neo)
            // Các câu bồi (i > 0) sẽ để score = null
            if (i == 0) {
                qaBuilder.score(gradingRes.getScore())
                        .feedback(gradingRes.getFeedback())
                        .suggestion(gradingRes.getSuggestion());
            } else {
                // Câu bồi: Có thể để null hoặc copy feedback nếu muốn (thường là để null cho đỡ rối)
                qaBuilder.score(null)
                        .feedback(null)
                        .suggestion(null);
            }

            finalResults.add(qaBuilder.build());
        }
    }

    private InterviewSession.EvaluationResult determineEvaluationResult(double overallScore) {
        if (overallScore >= 9.0) {
            return InterviewSession.EvaluationResult.STRONG_HIRE;
        } else if (overallScore >= 7.0) {
            return InterviewSession.EvaluationResult.HIRE;
        } else if (overallScore >= 5.0) {
            return InterviewSession.EvaluationResult.CONSIDER;
        } else {
            return InterviewSession.EvaluationResult.REJECT;
        }
    }

    private String genOverviewFeedback(List<InterviewResultDetail.QAResult> gradedHistory) {

        return pythonApiClient.callApi(
                PythonService.LLM,
                ApiPath.OVERVIEW_FEEDBACK_API,
                HttpMethod.POST,
                gradedHistory,
                String.class
        );
    }

    @Data
    @AllArgsConstructor
    private static class AnchorInfo {
        int phaseIndex;
        int questionIndex;
        String questionText;
        String phaseName;
    }
}