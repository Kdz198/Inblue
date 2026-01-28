package fpt.org.inblue.service.impl;


import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.model.InterviewResultDetail;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.caching.InterviewSessionRedis;
import fpt.org.inblue.model.dto.request.OrchestratorConductRequest;
import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse;
import fpt.org.inblue.model.dto.response.OrchestratorAnalysisResponse;
import fpt.org.inblue.model.dto.response.QuestionResponse;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.caching.InterviewSessionRedisRepository;
import fpt.org.inblue.service.InterviewProcessService;
import fpt.org.inblue.service.PythonApiClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewProcessServiceImpl implements InterviewProcessService {

    private final InterviewSessionRedisRepository redisRepository;
    private final InterviewSessionRepository sessionRepository;
    private final PythonApiClient pythonApiClient;

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

        OrchestratorAnalysisResponse aiResponse;
        try {
            aiResponse = pythonApiClient.callApi(
                    ApiPath.ANALYZER_API,
                    HttpMethod.POST,
                    pythonRequest,
                    OrchestratorAnalysisResponse.class
            );
        } catch (Exception e) {
            System.err.println("AI Error: " + e.getMessage());
            aiResponse = OrchestratorAnalysisResponse.builder()
                    .action(OrchestratorAnalysisResponse.AnalysisAction.MOVE_NEXT)
                    .responseText("Chúng ta chuyển sang câu tiếp theo nhé.")
                    .build();
        }

        // --- BƯỚC 3: XỬ LÝ RESPONSE AI ---
        if (aiResponse.getAction() == OrchestratorAnalysisResponse.AnalysisAction.DRILL_DOWN) {
            session.setCurrentQuestionType(InterviewSessionRedis.QuestionType.FOLLOW_UP);
            session.setCurrentQuestionText(aiResponse.getResponseText());
        } else {
            if (nextAnchorInfo == null) {
                finishSession(session);
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

    private void finishSession(InterviewSessionRedis redisSession) {

        InterviewSession dbSession = sessionRepository.findById(redisSession.getDbId())
                .orElseThrow(() -> new RuntimeException("DB Session not found"));

        List<InterviewResultDetail.QAResult> qaResults = redisSession.getChatHistory().stream()
                .map(h -> InterviewResultDetail.QAResult.builder()
                        .questionOrder(h.getQuestionOrder())
                        .questionText(h.getQuestionText())
                        .answerText(h.getAnswerText())
                        .build())
                .toList();

        InterviewResultDetail resultDetail = InterviewResultDetail.builder()
                .history(qaResults)
                .aiOverviewFeedback("Đang chờ AI chấm điểm...")
                .build();

        dbSession.setResultDetail(resultDetail);
        dbSession.setStatus(InterviewSession.SessionStatus.COMPLETED);
        dbSession.setCompletedAt(LocalDateTime.now());

        sessionRepository.save(dbSession);
        redisRepository.delete(redisSession);
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