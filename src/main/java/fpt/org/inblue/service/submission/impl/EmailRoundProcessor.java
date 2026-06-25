package fpt.org.inblue.service.submission.impl;

import static fpt.org.inblue.enums.RoundType.EMAIL_SIMULATOR;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import fpt.org.inblue.service.submission.SubmissionResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EmailRoundProcessor implements RoundSubmissionProcessor {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EmailRoundProcessor(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public RoundType getSupportedType() {
        return EMAIL_SIMULATOR;
    }

    @Override
    public SubmissionResult process(ProcessDto dto) {
        applicationEventPublisher.publishEvent(dto);
        return SubmissionResult.pending(dto.getApplication().getId());
    }
}
