package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.service.ApplicationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ApplicationDetailServiceImpl implements ApplicationDetailService {
    private final ApplicationDetailRepository applicationDetailRepository;

    @Override
    public ApplicationDetail getApplicationById(long id) {
        return applicationDetailRepository.findById(id).orElseThrow(() -> new CustomException("Application Detail not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<ApplicationDetail> getByApplicationId(long applicationId) {
        return applicationDetailRepository.findAllByApplicationId(applicationId);
    }

    @Override
    public void hrScore(int applicationId, boolean isPass, String note) {

    }
}
