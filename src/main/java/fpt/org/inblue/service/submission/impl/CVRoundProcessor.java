package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import org.springframework.stereotype.Component;

import static fpt.org.inblue.enums.RoundType.CV_SCREENING;

@Component
public class CVRoundProcessor implements RoundSubmissionProcessor {

    @Override
    public RoundType getSupportedType() {
        return CV_SCREENING;
    }

    @Override
    public void process(ApplicationDetail detail) {

    }
}
