package fpt.org.inblue.service.summary;

import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundSummaryService {
    private final RoundSummaryFactory roundSummaryFactory;

    public AISummaryRequest.RoundSummaryInfo buildRoundSummary(ApplicationDetail detail, Round roundConfig) {
        return roundSummaryFactory.build(detail, roundConfig);
    }
}
