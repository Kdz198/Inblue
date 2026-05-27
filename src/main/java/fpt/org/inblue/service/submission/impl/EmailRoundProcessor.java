package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import org.springframework.stereotype.Component;

import static fpt.org.inblue.enums.RoundType.EMAIL_SIMULATOR;

@Component
public class EmailRoundProcessor implements RoundSubmissionProcessor {

    @Override
    public RoundType getSupportedType() {
        return EMAIL_SIMULATOR;
    }

    @Override
    public void process(ApplicationDetail detail) {

    }
}
