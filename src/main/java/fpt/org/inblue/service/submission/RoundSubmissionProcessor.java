package fpt.org.inblue.service.submission;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.dto.ProcessDto;

import java.io.IOException;

public interface RoundSubmissionProcessor {

    RoundType getSupportedType();
    SubmissionResult process(ProcessDto detail) throws IOException;
}