package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.QuizSet;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;
import fpt.org.inblue.repository.PracticeSetRepository;
import fpt.org.inblue.repository.QuizSetRepository;
import fpt.org.inblue.service.QuizItemService;
import fpt.org.inblue.service.QuizSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class QuizSetServiceImpl implements QuizSetService {
    @Autowired
    private PracticeSetRepository practiceSetRepository;

    @Autowired
    private QuizSetRepository quizSetRepository;

    @Autowired
    private QuizItemService quizItemService;

    @Override
    public QuizSet createQuizSet(int practiceSetId, String QuizName) {
        PracticeSet practiceSet = practiceSetRepository.findById(practiceSetId).orElse(null);
        if(practiceSet != null){
            QuizSet quizSet = QuizSet.builder()
                    .quizName(QuizName)
                    .practiceSet(practiceSet)
                    .isSubmitted(false)
                    .build();
            return quizSetRepository.save(quizSet);
        }
        else{
            throw new CustomException("Practice Set not found", HttpStatus.NOT_FOUND);

        }
    }

    @Override
    public QuizSet getQuizById(int quizId) {
        return quizSetRepository.findById(quizId).orElse(null);
    }

    @Override
    public List<QuizSet> getHistoryByPracticeSet(int practiceSetId) {
        return quizSetRepository.findAllByPracticeSet_Id(practiceSetId);
    }

    /**
     * Nộp bài và tính điểm
     * @param quizId
     * @param userAnswers
     * @return
     */
    @Override
    public QuizSet submitAndCalculateScore(int quizId, Map<Integer, String> userAnswers) {
        QuizSet quizSet = quizSetRepository.findById(quizId)
                .orElseThrow(() -> new CustomException("Quiz không tồn tại", HttpStatus.NOT_FOUND));
        List<QuizItem> items = quizItemService.getItemsByQuizSetId(quizId);
        int correctCount = 0;
        for (QuizItem item : items) {
            String userAnswer = userAnswers.get(item.getId());
            item.setUserResponse(userAnswer);
            if(item.getCorrectAnswer().equals(userAnswer)) {
                correctCount++;
            }
        }
        double finalScore = (double) correctCount / items.size() * 10;
        quizSet.setScore(Math.round(finalScore * 100.0) / 100.0); // Làm tròn 2 chữ số
        quizSet.setSubmitted(true);
        return quizSetRepository.save(quizSet);
    }

    @Override
    public void deleteQuizSet(int quizId) {

    }

    /**
     * Tạo bộ quiz hoàn chỉnh gồm QuizSet và các QuizItem
     * @param practiceSetId
     * @param QuizName
     * @param items
     * @return
     */
    @Override
    @Transactional
    public List<QuizItem> createFullQuizSet(int practiceSetId, String QuizName, List<QuizItemCreateRequest> items) {
        QuizSet quizSet = createQuizSet(practiceSetId, QuizName);
        quizItemService.saveAllItems(items, quizSet.getQuizId());

        return quizItemService.getItemsByQuizSetId(quizSet.getQuizId());
    }

    @Override
    public List<QuizSet> getAllQuizSet() {
        return quizSetRepository.findAll();
    }
}
