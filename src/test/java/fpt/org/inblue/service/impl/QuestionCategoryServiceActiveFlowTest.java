package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.QuestionCategory;
import fpt.org.inblue.repository.QuestionCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class QuestionCategoryServiceActiveFlowTest {
    @Mock
    QuestionCategoryRepository repository;

    private QuestionCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QuestionCategoryServiceImpl(repository);
    }

    @Test
    void updateExistingCategoryPersistsChanges() {
        QuestionCategory category = new QuestionCategory();
        category.setId(2);
        when(repository.existsById(2)).thenReturn(true);
        when(repository.save(category)).thenReturn(category);
        assertEquals(category, service.updateQuestionCategory(category));
        verify(repository).save(category);
    }

    @Test
    void updateMissingCategoryReturnsNotFound() {
        QuestionCategory category = new QuestionCategory();
        category.setId(404);
        when(repository.existsById(404)).thenReturn(false);
        CustomException error = assertThrows(CustomException.class, () -> service.updateQuestionCategory(category));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void deleteMissingCategoryReturnsNotFound() {
        when(repository.existsById(404)).thenReturn(false);
        CustomException error = assertThrows(CustomException.class, () -> service.deleteQuestionCategory(404));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }
}
