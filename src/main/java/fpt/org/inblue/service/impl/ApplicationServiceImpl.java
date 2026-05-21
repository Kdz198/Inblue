package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.security.JwtUtils;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.utils.HelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Override
    public Application applyForJob(Long jdId) {
        String token = HelperUtil.getToke();
        int userId = jwtUtils.getUserIdFromToken(token);
        Application application = new Application();
        application.setUserId(userId);
        application.setJdId(jdId);
        return applicationRepository.save(application);
    }

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new CustomException("Application not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Override
    public List<Application> getAllApplicationsByUserId() {
        String token = HelperUtil.getToke();
        int userId = jwtUtils.getUserIdFromToken(token);
        return applicationRepository.findAllByUserId(userId);
    }
}
