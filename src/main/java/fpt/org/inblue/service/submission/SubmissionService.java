package fpt.org.inblue.service.submission;

import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.ApplicationDetail.*;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import fpt.org.inblue.service.ApplicationService;
import fpt.org.inblue.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final RoundProcessorFactory roundProcessorFactory;
    private final ApplicationService applicationService;
    private final JobDescriptionService jobDescriptionService;

    @Transactional
    public SubmissionResult submitRound(SubmitRequest detail) throws IOException {
        Application currentApplication = applicationService.getApplicationById(detail.getApplicationId());
        Round currentRound = jobDescriptionService.getRoundByOrder( currentApplication.getJdId(), currentApplication.getCurrentRoundOrder());
        RoundSubmissionProcessor processor = roundProcessorFactory.getProcessor(currentRound.getRoundType());
        ProcessDto processDto = new ProcessDto();
        processDto.setApplication(currentApplication);
        processDto.setRound(currentRound);
        processDto.setFile(detail.getFile());
        processDto.setQuizAnswers(detail.getQuizAnswers());
        processDto.setTextContent(detail.getTextContent());
        processDto.setRoundType(currentRound.getRoundType());
        processDto.setCompileRequest(detail.getCompileRequest());
        SubmissionResult submissionResult = processor.process(processDto);
        if(submissionResult.getStatus().equals(SubmissionResult.Status.COMPLETED)&&submissionResult.getRoundResult().equals(RoundResult.PASSED)){
            applicationService.moveToNextRound(currentApplication);
        }
        return submissionResult;
    }
}
