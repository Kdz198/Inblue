package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.dto.request.EntryTestSubmitRequest;
import fpt.org.inblue.entrytest.dto.response.EntryTestStartResponse;

public interface EntryTestService {
    EntryTestStartResponse startEntryTest(Integer userId);

    EntryTestAttempt submitEntryTest(Integer userId, Long attemptId, EntryTestSubmitRequest request);

    EntryTestAttempt getAttempt(Integer userId, Long attemptId);
}
