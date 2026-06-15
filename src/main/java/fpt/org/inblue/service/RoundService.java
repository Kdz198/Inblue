package fpt.org.inblue.service;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.model.dto.request.UpdateJdRoundRequest;

import java.util.List;

public interface RoundService {
    List<Round> setUpRoundForJd(Long jdId, SetupJdRoundsRequest request);
    List<Round> updateRoundForJd(Long jdId, UpdateJdRoundRequest request);
    Round getRoundById(Long roundId);
    List<RoundType> getAllRoundTypes();
}
