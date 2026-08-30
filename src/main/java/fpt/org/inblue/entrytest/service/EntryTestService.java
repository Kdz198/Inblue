package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.entrytest.dto.request.EntryTestRunCodeRequest;
import fpt.org.inblue.entrytest.dto.request.EntryTestSubmitRequest;
import fpt.org.inblue.entrytest.dto.response.EntryTestStartResponse;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;

public interface EntryTestService {
    EntryTestStartResponse startEntryTest(Integer userId);

    CompilerResponseDto runCode(Integer userId, Long attemptId, EntryTestRunCodeRequest request);

    EntryTestAttempt submitEntryTest(Integer userId, Long attemptId, EntryTestSubmitRequest request);

    EntryTestAttempt getAttempt(Integer userId, Long attemptId);
}
