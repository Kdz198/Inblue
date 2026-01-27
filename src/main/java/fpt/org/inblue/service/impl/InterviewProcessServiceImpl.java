package fpt.org.inblue.service.impl;


import fpt.org.inblue.model.InterviewResultDetail;
import fpt.org.inblue.model.InterviewSession;
import fpt.org.inblue.model.caching.InterviewSessionRedis;
import fpt.org.inblue.model.dto.request.SubmitAnswerRequest;
import fpt.org.inblue.model.dto.response.InterviewBlueprintResponse.*;
import fpt.org.inblue.model.dto.response.QuestionResponse;
import fpt.org.inblue.repository.InterviewSessionRepository;
import fpt.org.inblue.repository.caching.InterviewSessionRedisRepository;
import fpt.org.inblue.service.InterviewProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewProcessServiceImpl implements InterviewProcessService {

    private final InterviewSessionRedisRepository redisRepository;
    private final InterviewSessionRepository sessionRepository;

    @Override
    // 1. START / GET CURRENT QUESTION
    public QuestionResponse getCurrentQuestion(String sessionKey) {
        InterviewSessionRedis session = redisRepository.findById(sessionKey)
                .orElseThrow(() -> new RuntimeException("Session not found or expired"));

        return buildQuestionResponse(session);
    }


    @Override
    // 2. SUBMIT ANSWER & NEXT
    public QuestionResponse submitAnswer(SubmitAnswerRequest request) {
        InterviewSessionRedis session = redisRepository.findById(request.getSessionKey())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Lấy data hiện tại để lưu câu trả lời cũ
        var currentPhase = session.getBlueprint().getBlueprint().get(session.getCurrentPhaseIndex());
        var currentQuestion = currentPhase.getQuestions().get(session.getCurrentQuestionIndex());

        // A. Lưu câu trả lời vào Redis History
        InterviewSessionRedis.InterviewExchange exchange = InterviewSessionRedis.InterviewExchange.builder()
                .phaseName(currentPhase.getPhaseName())
                .questionId(currentQuestion.getOrder())
                .questionText(currentQuestion.getQuestionText())
                .answerText(request.getAnswer())
                .submittedAt(LocalDateTime.now().toString())
                .build();

        session.getChatHistory().add(exchange);

        // B. Tính toán Index tiếp theo (Logic chuyển Phase)
        int nextQIndex = session.getCurrentQuestionIndex() + 1;
        int currentPhaseTotalQ = currentPhase.getQuestions().size();

        if (nextQIndex < currentPhaseTotalQ) {
            // Vẫn còn câu hỏi trong Phase này
            session.setCurrentQuestionIndex(nextQIndex);
        } else {
            // Hết câu hỏi trong Phase này -> Sang Phase kế tiếp
            int nextPhaseIndex = session.getCurrentPhaseIndex() + 1;
            int totalPhases = session.getBlueprint().getBlueprint().size();

            if (nextPhaseIndex < totalPhases) {
                // Sang Phase mới, reset câu hỏi về 0
                session.setCurrentPhaseIndex(nextPhaseIndex);
                session.setCurrentQuestionIndex(0);
            } else {
                finishSession(session);
                return QuestionResponse.builder()
                        .isFinished(true)
                        .sessionKey(session.getId())
                        .questionContent("Cảm ơn bạn đã hoàn thành buổi phỏng vấn!")
                        .build();
                // (Lúc này có thể trigger lưu từ Redis về Postgres)
            }
        }

        // C. Update Redis
        redisRepository.save(session);

        // D. Trả về câu hỏi MỚI
        return buildQuestionResponse(session);
    }


    private QuestionResponse buildQuestionResponse(InterviewSessionRedis session) {
        List<InterviewPhase> phases = session.getBlueprint().getBlueprint();
        InterviewPhase currentPhase = phases.get(session.getCurrentPhaseIndex());
        var currentQuestion = currentPhase.getQuestions().get(session.getCurrentQuestionIndex());

        return QuestionResponse.builder()
                .sessionKey(session.getId())
                .isFinished(false)
                .phaseName(currentPhase.getPhaseName())
                .currentQuestionIndex(session.getCurrentQuestionIndex() + 1) // +1 cho thân thiện người dùng
                .totalQuestionsInPhase(currentPhase.getQuestions().size())
                .questionContent(currentQuestion.getQuestionText())
                .build();
    }


    private void finishSession(InterviewSessionRedis redisSession) {
        // 1. Tìm bản ghi trong DB
        InterviewSession dbSession = sessionRepository.findById(redisSession.getDbId())
                .orElseThrow(() -> new RuntimeException("DB Session not found"));

        // 2. Map data từ Redis sang cấu trúc lưu DB
        List<InterviewResultDetail.QAResult> qaResults = redisSession.getChatHistory().stream()
                .map(h -> InterviewResultDetail.QAResult.builder()
                        .questionOrder(h.getQuestionOrder())
                        .questionText(h.getQuestionText())
                        .answerText(h.getAnswerText())
                        // Score và Feedback tạm thời để null hoặc tính sau
                        .build())
                .toList();

        InterviewResultDetail resultDetail = InterviewResultDetail.builder()
                .history(qaResults)
                .aiOverviewFeedback("Đang chờ AI chấm điểm...")
                .build();

        // 3. Update DB
        dbSession.setResultDetail(resultDetail);
        dbSession.setStatus(InterviewSession.SessionStatus.COMPLETED);
        dbSession.setCompletedAt(LocalDateTime.now());

        sessionRepository.save(dbSession);

        // 4. Xóa Cache Redis
        redisRepository.delete(redisSession);

        // 5. TRIGGER ASYNC GRADING (Bước quan trọng)
        // gradingService.submitForGrading(dbSession.getId());
    }
}
