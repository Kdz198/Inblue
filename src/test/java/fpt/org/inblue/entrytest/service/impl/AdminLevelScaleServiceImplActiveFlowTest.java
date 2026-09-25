package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleRequest;
import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleSetRequest;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.entrytest.model.LevelScale;
import fpt.org.inblue.entrytest.repository.LevelScaleRepository;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.LevelScaleMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminLevelScaleServiceImplActiveFlowTest {
    @Mock
    LevelScaleRepository repository;

    @Mock
    LevelScaleMapper mapper;

    private AdminLevelScaleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminLevelScaleServiceImpl(repository, mapper);
    }

    @Test
    void createLevelScaleRejectsMissingLevel() {
        UpsertLevelScaleRequest request = validRequest().level(null).build();
        assertBadRequest(() -> service.createLevelScale(request));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createLevelScaleRejectsMissingScoreBoundary() {
        assertBadRequest(
                () -> service.createLevelScale(validRequest().minScore(null).build()));
        assertBadRequest(
                () -> service.createLevelScale(validRequest().maxScore(null).build()));
    }

    @Test
    void createLevelScaleRejectsReversedRange() {
        assertBadRequest(() -> service.createLevelScale(
                validRequest().minScore(80.0).maxScore(79.99).build()));
    }

    @Test
    void createLevelScaleAcceptsEqualBoundaries() {
        UpsertLevelScaleRequest request =
                validRequest().minScore(50.0).maxScore(50.0).build();
        LevelScale mapped = LevelScale.builder()
                .level(TargetLevel.JUNIOR)
                .minScore(50.0)
                .maxScore(50.0)
                .build();
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(mapped);

        assertSame(mapped, service.createLevelScale(request));
    }

    @Test
    void getLevelScaleRejectsUnknownId() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.getLevelScale(9L));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void updateLevelScaleValidatesMappedEntityBeforeSave() {
        UpsertLevelScaleRequest request = validRequest().build();
        LevelScale existing = LevelScale.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        org.mockito.Mockito.doAnswer(invocation -> {
                    existing.setLevel(TargetLevel.JUNIOR);
                    existing.setMinScore(0.0);
                    existing.setMaxScore(49.99);
                    return null;
                })
                .when(mapper)
                .updateFromRequest(request, existing);
        when(repository.save(existing)).thenReturn(existing);

        assertSame(existing, service.updateLevelScale(1L, request));
    }

    @Test
    void deactivateLevelScalePersistsInactiveState() {
        LevelScale existing = LevelScale.builder().id(1L).isActive(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        assertFalse(service.deactivateLevelScale(1L).getIsActive());
    }

    @Test
    void upsertLevelScaleSetRejectsEmptyScales() {
        assertBadRequest(() -> service.upsertLevelScaleSet(UpsertLevelScaleSetRequest.builder()
                .targetRole(TargetRole.BE)
                .scales(List.of())
                .build()));
    }

    @Test
    void upsertLevelScaleSetUpdatesExistingAndCreatesMissingLevels() {
        UpsertLevelScaleRequest juniorRequest =
                validRequest().level(TargetLevel.JUNIOR).build();
        UpsertLevelScaleRequest seniorRequest = validRequest()
                .level(TargetLevel.MIDDLE)
                .minScore(80.0)
                .maxScore(100.0)
                .build();
        UpsertLevelScaleSetRequest request = UpsertLevelScaleSetRequest.builder()
                .targetRole(TargetRole.BE)
                .scales(List.of(juniorRequest, seniorRequest))
                .build();
        LevelScale junior = LevelScale.builder().level(TargetLevel.JUNIOR).build();
        LevelScale senior = LevelScale.builder().level(TargetLevel.MIDDLE).build();
        when(repository.findAllByTargetRole(TargetRole.BE)).thenReturn(List.of(junior));
        when(mapper.toEntityForSet(seniorRequest, TargetRole.BE)).thenReturn(senior);
        when(repository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<LevelScale> result = service.upsertLevelScaleSet(request);

        assertEquals(List.of(junior, senior), result);
        verify(mapper).updateFromSetRequest(juniorRequest, TargetRole.BE, junior);
    }

    private UpsertLevelScaleRequest.UpsertLevelScaleRequestBuilder validRequest() {
        return UpsertLevelScaleRequest.builder()
                .targetRole(TargetRole.BE)
                .level(TargetLevel.JUNIOR)
                .minScore(0.0)
                .maxScore(79.99)
                .minCodingScore(0.0)
                .isActive(true);
    }

    private void assertBadRequest(org.junit.jupiter.api.function.Executable executable) {
        CustomException error = assertThrows(CustomException.class, executable);
        assertEquals(400, error.getStatus().value());
    }
}
