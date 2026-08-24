package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.entity.EntryTestAttempt;
import fpt.org.inblue.entrytest.entity.UserCompetency;

public interface UserCompetencyService {
    UserCompetency updateAfterEntryTest(EntryTestAttempt attempt);

    UserCompetency getCurrentCompetency(Integer userId);

    UserCompetency updateAfterJd(Integer userId, Double jdScore);

    String resolveLevelName(EntryTestAttempt attempt);
}
