package fpt.org.inblue.service.submission.impl;

import static fpt.org.inblue.enums.RoundType.CV_SCREENING;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.event.SubmissionEventHandle;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import fpt.org.inblue.service.submission.SubmissionResult;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CVRoundProcessor implements RoundSubmissionProcessor {

    private final SubmissionEventHandle submissionEventHandle;

    @Override
    public RoundType getSupportedType() {
        return CV_SCREENING;
    }

    @Override
    public SubmissionResult process(ProcessDto dto) throws IOException {
        return SubmissionResult.completed(submissionEventHandle.processCvSubmission(dto));
    }
}
