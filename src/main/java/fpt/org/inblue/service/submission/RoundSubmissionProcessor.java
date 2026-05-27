package fpt.org.inblue.service.submission;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.ApplicationDetail.*;
import fpt.org.inblue.model.Round.*;

public interface RoundSubmissionProcessor {

    RoundType getSupportedType();
    Object process(ApplicationDetail detail);
}