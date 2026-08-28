package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.entrytest.dto.request.UpsertEntryTestRequest;
import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.repository.EntryTestRepository;
import fpt.org.inblue.entrytest.service.AdminEntryTestService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.EntryTestMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEntryTestServiceImpl implements AdminEntryTestService {
    private final EntryTestRepository entryTestRepository;
    private final EntryTestMapper entryTestMapper;

    @Override
    public List<EntryTest> getAllEntryTests() {
        return entryTestRepository.findAll();
    }

    @Override
    public EntryTest getEntryTest(Long id) {
        return entryTestRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Entry test not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public EntryTest getActiveEntryTest() {
        return entryTestRepository
                .findFirstByIsActiveTrueOrderByUpdatedAtDesc()
                .orElseThrow(() -> new CustomException("Active entry test not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public EntryTest createEntryTest(UpsertEntryTestRequest request) {
        EntryTest entryTest = entryTestMapper.toEntity(request);
        return entryTestRepository.save(entryTest);
    }

    @Override
    @Transactional
    public EntryTest updateEntryTest(Long id, UpsertEntryTestRequest request) {
        EntryTest entryTest = getEntryTest(id);
        entryTestMapper.updateFromRequest(request, entryTest);
        return entryTestRepository.save(entryTest);
    }

    @Override
    @Transactional
    public EntryTest deactivateEntryTest(Long id) {
        EntryTest entryTest = getEntryTest(id);
        entryTest.setIsActive(false);
        return entryTestRepository.save(entryTest);
    }
}
