package fpt.org.inblue.service;

import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;

import java.util.List;

public interface RoundService {
    List<Round> setUpRoundForJd(Long jdId, SetupJdRoundsRequest request);

}
