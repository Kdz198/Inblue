package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.QuestionBankMapper;
import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.model.dto.request.CreateQuestionBankRequest;
import fpt.org.inblue.model.dto.request.QuestionGenerateRequest;
import fpt.org.inblue.model.dto.request.UpdateQuestionBankRequest;
import fpt.org.inblue.model.dto.response.QuestionGenerateResponse;
import fpt.org.inblue.repository.QuestionBankRepository;
import fpt.org.inblue.repository.QuestionCategoryRepository;
import fpt.org.inblue.service.ApiClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionBankServiceImplActiveFlowTest {
    @Mock
    QuestionBankRepository questionRepository;

    @Mock
    QuestionCategoryRepository categoryRepository;

    @Mock
    QuestionBankMapper mapper;

    @Mock
    ApiClient apiClient;

    private QuestionBankServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QuestionBankServiceImpl(questionRepository, categoryRepository, mapper, apiClient);
    }

    @Test
    void createQuestionBankAttachesExistingCategory() {
        CreateQuestionBankRequest request =
                CreateQuestionBankRequest.builder().questionCategoryId(3).build();
        QuestionCategory category = QuestionCategory.builder().id(3).build();
        QuestionBank question = QuestionBank.builder().build();
        when(categoryRepository.findById(3)).thenReturn(Optional.of(category));
        when(mapper.toEntity(request)).thenReturn(question);
        when(questionRepository.save(question)).thenReturn(question);
        assertSame(question, service.createQuestionBank(request));
        assertSame(category, question.getQuestionCategory());
    }

    @Test
    void createQuestionBankRejectsUnknownCategory() {
        CreateQuestionBankRequest request =
                CreateQuestionBankRequest.builder().questionCategoryId(3).build();
        when(categoryRepository.findById(3)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.createQuestionBank(request))
                        .getStatus()
                        .value());
    }

    @Test
    void updateQuestionBankRejectsUnknownQuestion() {
        when(questionRepository.findById(9)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(
                                CustomException.class,
                                () -> service.updateQuestionBank(9, new UpdateQuestionBankRequest()))
                        .getStatus()
                        .value());
    }

    @Test
    void updateQuestionBankChangesCategoryWhenProvided() {
        QuestionBank question = QuestionBank.builder().id(1).build();
        QuestionCategory category = QuestionCategory.builder().id(2).build();
        UpdateQuestionBankRequest request =
                UpdateQuestionBankRequest.builder().questionCategoryId(2).build();
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(category));
        when(questionRepository.save(question)).thenReturn(question);
        assertSame(question, service.updateQuestionBank(1, request));
        assertSame(category, question.getQuestionCategory());
    }

    @Test
    void deleteQuestionBankRejectsUnknownQuestion() {
        when(questionRepository.existsById(9)).thenReturn(false);
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.deleteQuestionBank(9))
                        .getStatus()
                        .value());
    }

    @Test
    void deleteQuestionBankSoftDeletesExistingQuestion() {
        QuestionBank question = QuestionBank.builder().id(1).isDeleted(false).build();
        when(questionRepository.existsById(1)).thenReturn(true);
        when(questionRepository.getQuestionBankById(1)).thenReturn(question);
        service.deleteQuestionBank(1);
        assertTrue(question.getIsDeleted());
        verify(questionRepository).save(question);
    }

    @Test
    void getAllQuestionBanksPreservesEmptyList() {
        when(questionRepository.findAll()).thenReturn(List.of());
        assertEquals(List.of(), service.getAllQuestionBanks());
    }

    @Test
    void generateQuestionUsesQuizWorkspace() {
        QuestionGenerateRequest request = new QuestionGenerateRequest();
        QuestionGenerateResponse response = new QuestionGenerateResponse();
        when(apiClient.sendChatToAnythingLlm(
                        AnythingLlmWorkspace.QUIZ_GEN, request, "java", true, null, QuestionGenerateResponse.class))
                .thenReturn(response);
        assertSame(response, service.generateQuestion(request));
    }
}
