package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.InterviewTemplate;
import fpt.org.inblue.model.TemplateRound;
import fpt.org.inblue.model.dto.request.UpsertTemplateRequest;
import fpt.org.inblue.repository.InterviewTemplateRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterviewTemplateServiceImplActiveFlowTest {
    @Mock
    InterviewTemplateRepository repository;

    private InterviewTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewTemplateServiceImpl(repository);
    }

    @Test
    void getAllTemplatesMapsRoundCount() {
        InterviewTemplate template = InterviewTemplate.builder()
                .id(1L)
                .name("Backend")
                .category("BE")
                .rounds(new ArrayList<>(
                        List.of(TemplateRound.builder().name("HR").build())))
                .build();
        when(repository.findAll()).thenReturn(List.of(template));
        assertEquals(1, service.getAllTemplates().getFirst().getTotalRounds());
    }

    @Test
    void getAllTemplatesPreservesEmptyList() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.getAllTemplates().isEmpty());
    }

    @Test
    void getTemplateByIdMapsRoundDetails() {
        InterviewTemplate template = InterviewTemplate.builder()
                .id(1L)
                .name("Backend")
                .rounds(new ArrayList<>(List.of(TemplateRound.builder()
                        .name("Technical")
                        .roundOrder(2)
                        .roundType(RoundType.AI_INTERVIEW)
                        .build())))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        assertEquals(
                "Technical", service.getTemplateById(1L).getRounds().getFirst().getName());
    }

    @Test
    void getTemplateByIdRejectsUnknownTemplate() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getTemplateById(9L));
    }

    @Test
    void createTemplateMapsRoundsAndReturnsSavedId() {
        UpsertTemplateRequest request = request("Created");
        when(repository.save(org.mockito.ArgumentMatchers.any(InterviewTemplate.class)))
                .thenAnswer(invocation -> {
                    InterviewTemplate value = invocation.getArgument(0);
                    value.setId(8L);
                    return value;
                });
        assertEquals(8L, service.createTemplate(request));
    }

    @Test
    void updateTemplateClearsAndReplacesRounds() {
        InterviewTemplate template = InterviewTemplate.builder()
                .id(1L)
                .rounds(new ArrayList<>(
                        List.of(TemplateRound.builder().name("Old").build())))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        service.updateTemplate(1L, request("Updated"));
        assertEquals("Updated", template.getName());
        assertEquals("Technical", template.getRounds().getFirst().getName());
        verify(repository).saveAndFlush(template);
        verify(repository).save(template);
    }

    @Test
    void deleteTemplateRejectsMissingTemplate() {
        when(repository.existsById(9L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.deleteTemplate(9L));
    }

    @Test
    void deleteTemplateDeletesExistingTemplate() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deleteTemplate(1L);
        verify(repository).deleteById(1L);
    }

    private UpsertTemplateRequest request(String name) {
        UpsertTemplateRequest.TemplateRoundItem round = new UpsertTemplateRequest.TemplateRoundItem();
        round.setName("Technical");
        round.setRoundOrder(1);
        round.setRoundType(RoundType.AI_INTERVIEW);
        UpsertTemplateRequest request = new UpsertTemplateRequest();
        request.setName(name);
        request.setCategory("BE");
        request.setRounds(List.of(round));
        return request;
    }
}
