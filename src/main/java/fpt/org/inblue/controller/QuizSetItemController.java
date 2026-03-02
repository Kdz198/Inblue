package fpt.org.inblue.controller;


import fpt.org.inblue.model.QuizItem;
import fpt.org.inblue.model.dto.request.QuizItemCreateRequest;
import fpt.org.inblue.service.QuizItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-set-items")
@CrossOrigin(origins = "*")
public class QuizSetItemController {
    @Autowired
    private QuizItemService quizItemService;


}
