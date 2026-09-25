package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.CompanyMapper;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.request.CreateCompanyRequest;
import fpt.org.inblue.model.dto.request.UpdateCompanyRequest;
import fpt.org.inblue.repository.CompanyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplActiveFlowTest {
    @Mock
    CompanyRepository repository;

    @Mock
    CompanyMapper mapper;

    @Mock
    CloudinaryService cloudinaryService;

    private CompanyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CompanyServiceImpl(repository, mapper, cloudinaryService);
    }

    @Test
    void getByIdRejectsUnknownCompany() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(
                404,
                assertThrows(CustomException.class, () -> service.getById(9L))
                        .getStatus()
                        .value());
    }

    @Test
    void createRejectsNullRequest() {
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.create(null, null, null))
                        .getStatus()
                        .value());
    }

    @Test
    void createWithoutImagesMapsAndSavesCompany() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest();
        Company company = Company.builder().name("Acme").build();
        when(mapper.toEntity(request)).thenReturn(company);
        when(repository.save(company)).thenReturn(company);
        assertSame(company, service.create(request, null, null));
    }

    @Test
    void updatePropagatesCompanyNameToJobs() throws Exception {
        UpdateCompanyRequest request = new UpdateCompanyRequest();
        request.setId(1L);
        request.setName("Renamed");
        JobDescription job = JobDescription.builder().companyName("Old").build();
        Company company = Company.builder()
                .id(1L)
                .name("Old")
                .jobDescriptions(new ArrayList<>(List.of(job)))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(company));
        org.mockito.Mockito.doAnswer(invocation -> {
                    company.setName("Renamed");
                    return null;
                })
                .when(mapper)
                .updateCompanyFromRequest(request, company);
        when(repository.save(company)).thenReturn(company);
        service.update(request, null, null);
        assertEquals("Renamed", job.getCompanyName());
    }

    @Test
    void getAllReturnsNewestRepositoryItemFirst() {
        Company first = Company.builder().id(1L).build();
        Company second = Company.builder().id(2L).build();
        when(repository.findAll()).thenReturn(new ArrayList<>(List.of(first, second)));
        assertEquals(List.of(second, first), service.getAll());
    }

    @Test
    void deleteByIdSoftDeletesExistingCompany() {
        Company company = Company.builder().id(1L).isDeleted(false).build();
        when(repository.findById(1L)).thenReturn(Optional.of(company));
        service.deleteById(1L);
        assertTrue(company.getIsDeleted());
        verify(repository).save(company);
    }

    @Test
    void toggleActiveSwitchesBothDirections() {
        Company company = Company.builder().id(1L).status("ACTIVE").build();
        when(repository.findById(1L)).thenReturn(Optional.of(company));
        service.toggleActive(1L);
        assertEquals("INACTIVE", company.getStatus());
        service.toggleActive(1L);
        assertEquals("ACTIVE", company.getStatus());
    }
}
