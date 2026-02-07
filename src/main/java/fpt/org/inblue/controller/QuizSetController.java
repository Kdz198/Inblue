package fpt.org.inblue.controller;

import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.QuizSet;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;
import fpt.org.inblue.service.QuizSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz-sets")
public class QuizSetController {
    @Autowired
    private QuizSetService quizSetService;

    @PostMapping("create-full")
    public ResponseEntity<List<QuizItem>> createFullQuizSet(@RequestParam int practiceSetId, @RequestParam String QuizName, @RequestBody List<QuizItemCreateRequest> items) {
        return ResponseEntity.ok(quizSetService.createFullQuizSet(practiceSetId, QuizName, items));
    }

    @GetMapping
    public ResponseEntity<List<QuizSet>> getAll() {
        return ResponseEntity.ok(quizSetService.getAllQuizSet());
    }
    @GetMapping("/{quizId}" )
    public ResponseEntity<QuizSet> getQuizById(@PathVariable int quizId){
        return ResponseEntity.ok(quizSetService.getQuizById(quizId));
    }
    @GetMapping("by-practice-set/{practiceSetId}")
    public ResponseEntity<List<QuizSet>> getHistoryByPracticeSet(@PathVariable int practiceSetId){
        return ResponseEntity.ok(quizSetService.getHistoryByPracticeSet(practiceSetId));
    }
    @PostMapping
    public ResponseEntity<QuizSet> createQuizSet(@RequestParam int quizId, @RequestParam String quizName){
        return ResponseEntity.ok(quizSetService.createQuizSet(quizId, quizName));
    }
    @PostMapping("/submit/{quizId}" )
    public ResponseEntity<QuizSet> submitAndCalculateScore(@PathVariable int quizId, @RequestBody Map<Integer, String> userAnswers){
        return ResponseEntity.ok(quizSetService.submitAndCalculateScore(quizId, userAnswers));
    }
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuizSet(@PathVariable int quizId){
        quizSetService.deleteQuizSet(quizId);
        return ResponseEntity.noContent().build();}
}
