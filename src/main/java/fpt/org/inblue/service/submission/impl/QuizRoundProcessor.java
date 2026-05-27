package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import org.springframework.stereotype.Component;

import static fpt.org.inblue.enums.RoundType.QUIZ;

@Component
public class QuizRoundProcessor implements RoundSubmissionProcessor {
    @Override
    public RoundType getSupportedType() {
        return QUIZ;
    }

    @Override
    public void process(ApplicationDetail detail) {

    }
}
