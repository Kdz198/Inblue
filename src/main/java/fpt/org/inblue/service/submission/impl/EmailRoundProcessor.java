package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.SubmissionResult;
import fpt.org.inblue.model.dto.request.SubmitRequest;
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
    public SubmissionResult process(ProcessDto detail) {

        return null;
    }
}
