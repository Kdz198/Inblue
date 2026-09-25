package fpt.org.inblue.entrytest.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.entrytest.dto.request.UpsertEntryTestRequest;
import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.repository.EntryTestRepository;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.EntryTestMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminEntryTestServiceImplActiveFlowTest {
    @Mock
    EntryTestRepository repository;

    @Mock
    EntryTestMapper mapper;

    private AdminEntryTestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminEntryTestServiceImpl(repository, mapper);
    }

    @Test
    void getAllEntryTestsReturnsRepositoryData() {
        EntryTest test = EntryTest.builder().id(1L).build();
        when(repository.findAll()).thenReturn(List.of(test));
        assertEquals(List.of(test), service.getAllEntryTests());
    }

    @Test
    void getEntryTestRejectsUnknownId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, () -> service.getEntryTest(99L));
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void getActiveEntryTestRejectsMissingConfiguration() {
        when(repository.findFirstByIsActiveTrueOrderByUpdatedAtDesc()).thenReturn(Optional.empty());
        CustomException error = assertThrows(CustomException.class, service::getActiveEntryTest);
        assertEquals(404, error.getStatus().value());
    }

    @Test
    void createEntryTestMapsAndSavesRequest() {
        UpsertEntryTestRequest request =
                UpsertEntryTestRequest.builder().name("SE entry").build();
        EntryTest mapped = EntryTest.builder().name("SE entry").build();
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(mapped);
        assertSame(mapped, service.createEntryTest(request));
    }

    @Test
    void updateEntryTestMapsOntoExistingEntity() {
        UpsertEntryTestRequest request =
                UpsertEntryTestRequest.builder().name("Updated").build();
        EntryTest existing = EntryTest.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertSame(existing, service.updateEntryTest(1L, request));
        verify(mapper).updateFromRequest(request, existing);
    }

    @Test
    void deactivateEntryTestPersistsInactiveState() {
        EntryTest existing = EntryTest.builder().id(1L).isActive(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        EntryTest result = service.deactivateEntryTest(1L);

        assertFalse(result.getIsActive());
        verify(repository).save(existing);
    }
}
