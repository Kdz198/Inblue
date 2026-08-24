package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.entrytest.dto.request.UpsertEntryTestRequest;
import fpt.org.inblue.entrytest.entity.EntryTest;
import fpt.org.inblue.entrytest.repository.EntryTestRepository;
import fpt.org.inblue.entrytest.service.AdminEntryTestService;
import fpt.org.inblue.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEntryTestServiceImpl implements AdminEntryTestService {
    private final EntryTestRepository entryTestRepository;

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
                .findFirstByIsActiveTrueOrderByVersionDesc()
                .orElseThrow(() -> new CustomException("Active entry test not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public EntryTest createEntryTest(UpsertEntryTestRequest request) {
        EntryTest entryTest = EntryTest.builder()
                .name(request.getName())
                .version(request.getVersion())
                .totalScore(request.getTotalScore())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .sectionConfigs(request.getSectionConfigs())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
        return entryTestRepository.save(entryTest);
    }

    @Override
    @Transactional
    public EntryTest updateEntryTest(Long id, UpsertEntryTestRequest request) {
        EntryTest entryTest = getEntryTest(id);
        if (request.getName() != null) {
            entryTest.setName(request.getName());
        }
        if (request.getVersion() != null) {
            entryTest.setVersion(request.getVersion());
        }
        if (request.getTotalScore() != null) {
            entryTest.setTotalScore(request.getTotalScore());
        }
        if (request.getTimeLimitMinutes() != null) {
            entryTest.setTimeLimitMinutes(request.getTimeLimitMinutes());
        }
        if (request.getSectionConfigs() != null) {
            entryTest.setSectionConfigs(request.getSectionConfigs());
        }
        if (request.getIsActive() != null) {
            entryTest.setIsActive(request.getIsActive());
        }
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
