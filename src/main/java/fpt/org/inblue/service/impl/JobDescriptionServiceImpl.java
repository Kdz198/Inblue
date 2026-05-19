package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.JobDescriptionMapper;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.request.CreateJobDescriptionRequest;
import fpt.org.inblue.model.dto.request.UpdateJobDescriptionRequest;
import fpt.org.inblue.model.enums.JobDescriptionStatus;
import fpt.org.inblue.model.enums.TargetLevel;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.JobDescriptionSpecification;
import fpt.org.inblue.service.JobDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobDescriptionServiceImpl implements JobDescriptionService {

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobDescriptionMapper jobDescriptionMapper;

    @Override
    public JobDescription getById(Long id) {
        if (id == null || id <= 0) {
            throw new CustomException("ID mô tả công việc không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        return jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy mô tả công việc với ID: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public List<JobDescription> getAll() {
        return jobDescriptionRepository.findAll();
    }

    @Override
    public List<JobDescription> getByCompanyId(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException("Không tìm thấy công ty với ID: " + companyId, HttpStatus.NOT_FOUND));
        return company.getJobDescriptions();
    }

    @Override
    @Transactional
    public JobDescription create(CreateJobDescriptionRequest request) throws IOException {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new CustomException("Không tìm thấy công ty với ID: " + request.getCompanyId(), HttpStatus.NOT_FOUND));

        JobDescription jobDescription = jobDescriptionMapper.toEntity(request);
        if (jobDescription.getAppliedCount() == null) {
            jobDescription.setAppliedCount(0);
        }
        jobDescription.setIsDeleted(false);
        company.getJobDescriptions().add(jobDescription);
        companyRepository.save(company);
        return jobDescription;
    }

    @Override
    @Transactional
    public JobDescription update(UpdateJobDescriptionRequest request) throws IOException {
        Long jobDescriptionId = request.getId();
        JobDescription jobDescription = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new CustomException("Không tìm thấy mô tả công việc với ID: " + jobDescriptionId, HttpStatus.NOT_FOUND));
        jobDescriptionMapper.updateJobDescriptionFromRequest(request, jobDescription);
        JobDescription updated = jobDescriptionRepository.save(jobDescription);
        return updated;
    }

    @Override
    public void delete(Long id) {
        JobDescription jobDescription = jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy mô tả công việc với ID: " + id, HttpStatus.NOT_FOUND));

        jobDescriptionRepository.delete(jobDescription);
    }

    @Override
    public void softDelete(Long id) {
        JobDescription jobDescription = jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy mô tả công việc với ID: " + id, HttpStatus.NOT_FOUND));

        jobDescription.setIsDeleted(true);
        jobDescription.setDeletedAt(LocalDateTime.now());
        jobDescriptionRepository.save(jobDescription);
    }

    @Override
    public List<JobDescription> searchJobs(String keyword, JobDescriptionStatus status, TargetLevel level, Double salaryMin, Double salaryMax) {

        Specification<JobDescription> spec = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
        spec= spec.and(JobDescriptionSpecification.isNotDeleted());
        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(JobDescriptionSpecification.titleContains(keyword));
        }
        if (status != null) {
            spec = spec.and(JobDescriptionSpecification.hasStatus(status));
        }
        if(level != null){
            spec = spec.and(JobDescriptionSpecification.levelEquals(level));
        }
        if(salaryMin != null){
            spec = spec.and(JobDescriptionSpecification.hasSalaryGreaterThanOrEqual(salaryMin));
        }
        if(salaryMax != null){
            spec = spec.and(JobDescriptionSpecification.hasSalaryLessThanOrEqual(salaryMax));
        }


        return jobDescriptionRepository.findAll(spec);
    }
}






