package fpt.org.inblue.service.submission;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.ApplicationDetail.*;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.repository.ApplicationDetailRepository;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final RoundProcessorFactory roundProcessorFactory;
    private final ApplicationService applicationService;
    private final JobDescriptionService jobDescriptionService;
    private final ApplicationDetailRepository applicationDetailRepository;

    @Transactional
    public ApplicationDetail submitRound(SubmitRequest detail) {
        Application currentApplication = applicationService.getApplicationById(detail.getApplicationId());
        Round currentRound = jobDescriptionService.getRoundByOrder( currentApplication.getJdId(), currentApplication.getCurrentRoundOrder());
        RoundSubmissionProcessor processor = roundProcessorFactory.getProcessor(currentRound.getRoundType());
        ApplicationDetail app = processor.process(detail);
        return applicationDetailRepository.save(app);
    }
}
