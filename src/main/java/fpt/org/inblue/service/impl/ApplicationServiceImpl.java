package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.ApplicationStatus;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.repository.ApplicationRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
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
    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Override
    public Application applyForJob(Long jdId) {
        String token = HelperUtil.getToke();
        int userId = jwtUtils.getUserIdFromToken(token);
        Application application = new Application();
        application.setUserId(userId);
        application.setJdId(jdId);
        JobDescription jd = jobDescriptionRepository.findById(jdId).orElseThrow(() -> new CustomException("Job Description not found", HttpStatus.NOT_FOUND));
        jd.setAppliedCount(jd.getAppliedCount() + 1);
        jobDescriptionRepository.save(jd);
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

    @Override
    public void moveToNextRound(Application currentApplication) {
        JobDescription jd = jobDescriptionRepository.findById(currentApplication.getJdId()).orElse(null);
        System.out.println("Moving application " + currentApplication.getId() + " to next round. Current round order: " + currentApplication.getCurrentRoundOrder());
        if(jd != null) {
            List<Round> rounds = jd.getRounds();
            int currentRoundOrder = currentApplication.getCurrentRoundOrder();
            if (currentRoundOrder < rounds.size()) {

                currentApplication.setCurrentRoundOrder(currentRoundOrder + 1);
                applicationRepository.save(currentApplication);
                System.out.println("Application " + currentApplication.getId() + " moved to round order " + currentApplication.getCurrentRoundOrder());
            }
            else{
                //Đã qua tất cả các vòng, có thể set trạng thái ứng viên ở đây nếu muốn
                //xử lí sau ( sẽ tính điểm các vòng set vô overall score và trạng thái tương ứng )
//                currentApplication.setStatus();
//                applicationRepository.save(currentApplication);
            }
        }
    }
}
