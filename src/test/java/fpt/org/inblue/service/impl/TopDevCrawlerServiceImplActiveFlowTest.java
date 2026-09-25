package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.request.TopDevJobImportRequest;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.EmbeddingService;
import fpt.org.inblue.service.JobDescriptionService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TopDevCrawlerServiceImplActiveFlowTest {
    @Mock
    CompanyRepository companyRepository;

    @Mock
    JobDescriptionRepository jobRepository;

    @Mock
    JobDescriptionService jobService;

    @Mock
    ApiClient apiClient;

    @Mock
    EmbeddingService embeddingService;

    private TopDevCrawlerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TopDevCrawlerServiceImpl(
                new ObjectMapper(), companyRepository, jobRepository, jobService, apiClient, embeddingService);
    }

    @Test
    void importJobRejectsBlankTitle() {
        TopDevJobImportRequest request = new TopDevJobImportRequest();
        request.setCompanyName("Acme");
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.importJob(request))
                        .getStatus()
                        .value());
    }

    @Test
    void importJobRejectsBlankCompanyName() {
        TopDevJobImportRequest request = new TopDevJobImportRequest();
        request.setTitle("Backend");
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.importJob(request))
                        .getStatus()
                        .value());
    }

    @Test
    void importJobRejectsDuplicateSourceJobId() {
        TopDevJobImportRequest request = new TopDevJobImportRequest();
        request.setTitle("Backend");
        request.setCompanyName("Acme");
        request.setSourceJobId("td-1");
        when(jobRepository.findFirstBySourceJobIdAndIsDeletedFalse("td-1"))
                .thenReturn(Optional.of(JobDescription.builder().build()));
        assertEquals(
                409,
                assertThrows(CustomException.class, () -> service.importJob(request))
                        .getStatus()
                        .value());
    }
}
