package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.JobDescriptionMapper;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.service.EmbeddingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class JobDescriptionServiceActiveFlowTest {
    @Mock
    JobDescriptionRepository repository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    JobDescriptionMapper mapper;

    @Mock
    EmbeddingService embeddingService;

    private JobDescriptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JobDescriptionServiceImpl(repository, companyRepository, mapper, embeddingService);
    }

    @Test
    void getByIdRejectsNonPositiveId() {
        CustomException e = assertThrows(CustomException.class, () -> service.getById(0L));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void getByIdReturnsExistingJob() {
        JobDescription jd = JobDescription.builder().id(4L).build();
        when(repository.findById(4L)).thenReturn(Optional.of(jd));
        assertEquals(jd, service.getById(4L));
    }

    @Test
    void getByIdReportsMissingJob() {
        when(repository.findById(404L)).thenReturn(Optional.empty());
        CustomException e = assertThrows(CustomException.class, () -> service.getById(404L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void softDeleteMarksJobDeletedAndPersists() {
        JobDescription jd = JobDescription.builder().id(4L).build();
        when(repository.findById(4L)).thenReturn(Optional.of(jd));
        service.softDelete(4L);
        assertEquals(Boolean.TRUE, jd.getIsDeleted());
        verify(repository).save(jd);
    }

    @Test
    void toggleActiveClosesOpenJob() {
        JobDescription jd = JobDescription.builder()
                .id(4L)
                .status(JobDescriptionStatus.OPEN)
                .build();
        when(repository.findById(4L)).thenReturn(Optional.of(jd));
        service.toggleActive(4L);
        assertEquals(JobDescriptionStatus.CLOSED, jd.getStatus());
    }

    @Test
    void toggleActiveOpensClosedJob() {
        JobDescription jd = JobDescription.builder()
                .id(4L)
                .status(JobDescriptionStatus.CLOSED)
                .build();
        when(repository.findById(4L)).thenReturn(Optional.of(jd));
        service.toggleActive(4L);
        assertEquals(JobDescriptionStatus.OPEN, jd.getStatus());
    }

    @Test
    void getRoundByOrderReturnsMatchingRound() {
        Round round = Round.builder().id(2L).roundOrder(2).build();
        JobDescription jd = JobDescription.builder()
                .id(4L)
                .rounds(new ArrayList<>(List.of(round)))
                .build();
        when(repository.findById(4L)).thenReturn(Optional.of(jd));
        assertEquals(round, service.getRoundByOrder(4L, 2));
    }

    @Test
    void getRoundByOrderReportsMissingRound() {
        JobDescription jd =
                JobDescription.builder().id(4L).rounds(new ArrayList<>()).build();
        when(repository.findById(4L)).thenReturn(Optional.of(jd));
        CustomException e = assertThrows(CustomException.class, () -> service.getRoundByOrder(4L, 3));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
}
