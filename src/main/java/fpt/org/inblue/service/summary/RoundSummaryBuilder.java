package fpt.org.inblue.service.summary;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;

public interface RoundSummaryBuilder {
    AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig);
}
