package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.dto.request.UpsertEntryTestRequest;
import fpt.org.inblue.entrytest.entity.EntryTest;
import java.util.List;

public interface AdminEntryTestService {
    List<EntryTest> getAllEntryTests();

    EntryTest getEntryTest(Long id);

    EntryTest getActiveEntryTest();

    EntryTest createEntryTest(UpsertEntryTestRequest request);

    EntryTest updateEntryTest(Long id, UpsertEntryTestRequest request);

    EntryTest deactivateEntryTest(Long id);
}
