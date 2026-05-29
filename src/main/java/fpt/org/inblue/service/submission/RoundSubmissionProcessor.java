package fpt.org.inblue.service.submission;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.ApplicationDetail.*;
import fpt.org.inblue.model.Round.*;
import fpt.org.inblue.model.dto.SubmissionResult;
import fpt.org.inblue.model.dto.request.SubmitRequest;

public interface RoundSubmissionProcessor {

    RoundType getSupportedType();
    SubmissionResult process(SubmitRequest detail);
}