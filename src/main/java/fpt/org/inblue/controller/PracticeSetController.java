package fpt.org.inblue.controller;

import fpt.org.inblue.model.PracticeSet;
import fpt.org.inblue.model.dto.request.PracticeAIRequest;
import fpt.org.inblue.model.dto.request.PracticeGenerateRequest;
import fpt.org.inblue.model.dto.request.PracticeRequest;
import fpt.org.inblue.model.dto.response.PracticeSetAIResponse;
import fpt.org.inblue.model.dto.response.PracticeSetResponse;
import fpt.org.inblue.service.PracticeSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice-sets")
@CrossOrigin(origins = "*")
public class PracticeSetController {
    @Autowired
    private PracticeSetService practiceSetService;

    @GetMapping
    public ResponseEntity<List<PracticeSet>> getQuestionSets() {
        List<PracticeSet> practiceSets = practiceSetService.getAllQuestionSets();
        return ResponseEntity.ok(practiceSets);
    }
    @GetMapping("{id}")
    public ResponseEntity<PracticeSet> getQuestionSet(@PathVariable int id) {
        PracticeSet practiceSet = practiceSetService.getQuestionSet(id);
        return ResponseEntity.ok(practiceSet);
    }
    @GetMapping("/level/{level} ")
    public ResponseEntity<List<PracticeSet>> getQuestionSetsByTargetLevel(@PathVariable String level) {
        List<PracticeSet> practiceSets = practiceSetService.getQuestionSetsByTargetLevel(level);
        return ResponseEntity.ok(practiceSets);}
    @PostMapping
    public ResponseEntity<PracticeSet> createQuestionSet(@RequestBody PracticeSet practiceSet) {
        PracticeSet createdPracticeSet = practiceSetService.createQuestionSet(practiceSet);
        return ResponseEntity.ok(createdPracticeSet);
    }
    @PutMapping
    public ResponseEntity<PracticeSet> updateQuestionSet(@RequestBody PracticeSet practiceSet) {
        PracticeSet updatedPracticeSet = practiceSetService.updateQuestionSet(practiceSet);
        return ResponseEntity.ok(updatedPracticeSet);}

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQuestionSet(@PathVariable int id) {
        practiceSetService.deleteQuestionSet(id);
        return ResponseEntity.noContent().build();}

    @PostMapping("create-full")
    public ResponseEntity<PracticeSet> createFullQuestionSet(@RequestBody PracticeRequest request) {
        return ResponseEntity.ok(practiceSetService.createFullSet(request));
    }
    @GetMapping("full-set/{id}")
    public ResponseEntity<PracticeSetResponse> getFullQuestionSet(@PathVariable int id) {
        return ResponseEntity.ok(practiceSetService.getFullSet(id));}

    @PostMapping("create-by-ai")
    public ResponseEntity<List<PracticeSetAIResponse> > createPracticeSetByAI(@RequestBody PracticeGenerateRequest request) {
        return ResponseEntity.ok(practiceSetService.creatPracticeSetByAI(request));
    }

    @GetMapping("interview-session/{interviewSessionId}")
    public ResponseEntity<List<PracticeSet>> getAllByInterviewSession(@PathVariable int interviewSessionId) {
        return ResponseEntity.ok(practiceSetService.getAllByInterviewSession(interviewSessionId));
    }
}
