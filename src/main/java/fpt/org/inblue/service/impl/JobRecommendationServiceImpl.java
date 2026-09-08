package fpt.org.inblue.service.impl;

import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.mapper.JobRecommendationMapper;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.JobRecommendationConfig;
import fpt.org.inblue.model.dto.response.JobRecommendationResponse;
import fpt.org.inblue.model.dto.response.JobRecommendationThresholdResponse;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.repository.JobRecommendationConfigRepository;
import fpt.org.inblue.service.JobRecommendationService;
import fpt.org.inblue.utils.VectorUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecommendationServiceImpl implements JobRecommendationService {
    private final UserCareerPreferenceRepository preferenceRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobRecommendationConfigRepository configRepository;
    private final JobRecommendationMapper jobRecommendationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobRecommendationResponse> getRecommendations(int userId) {
        Optional<UserCareerPreference> preferenceOptional = preferenceRepository.findByUserIdAndIsActiveTrue(userId);
        Optional<JobRecommendationConfig> configOptional =
                configRepository.findById(JobRecommendationConfig.SINGLETON_ID);

        if (preferenceOptional.isEmpty() || configOptional.isEmpty()) {
            log.info(
                    "Job recommendations unavailable: userId={}, hasPreference={}, hasThresholdConfig={}",
                    userId,
                    preferenceOptional.isPresent(),
                    configOptional.isPresent());
            return List.of();
        }

        float[] preferenceVector = preferenceOptional.get().getSkillEmbedding();
        if (preferenceVector == null || preferenceVector.length == 0) {
            log.info("Job recommendations unavailable: userId={} has no skill embedding", userId);
            return List.of();
        }

        double threshold = configOptional.get().getMatchThresholdPercent().doubleValue();
        LocalDateTime now = LocalDateTime.now();
        List<JobDescription> openJobs =
                jobDescriptionRepository.findByStatusAndIsDeletedFalse(JobDescriptionStatus.OPEN);
        List<ScoredJob> scoredJobs = new ArrayList<>();
        int eligibleJobCount = 0;

        for (JobDescription job : openJobs) {
            boolean eligible = isEligible(job, now);
            if (eligible) {
                eligibleJobCount++;
            }

            float[] jobVector = job.getSkillEmbedding();
            if (jobVector == null || jobVector.length == 0) {
                log.info(
                        "Bỏ qua so sánh JD vì chưa có vector: jdId={}, title='{}', status={}, deadlineAt={}, eligible={}",
                        job.getId(),
                        job.getTitle(),
                        job.getStatus(),
                        job.getDeadlineAt(),
                        eligible);
                continue;
            }

            scoredJobs.add(scoreJob(userId, job, preferenceVector, threshold, eligible));
        }

        log.info(
                "Job recommendation candidates: userId={}, openJobCount={}, eligibleJobCount={}, scoredJobCount={}, thresholdPercent={}",
                userId,
                openJobs.size(),
                eligibleJobCount,
                scoredJobs.size(),
                threshold);

        List<JobRecommendationResponse> recommendations = scoredJobs.stream()
                .filter(scoredJob -> scoredJob.eligible() && scoredJob.score() >= threshold)
                .sorted(Comparator.comparingDouble(ScoredJob::score).reversed())
                .map(ScoredJob::job)
                .map(jobRecommendationMapper::toResponse)
                .toList();
        log.info(
                "Job recommendations completed: userId={}, matchedJobCount={}", userId, recommendations.size());
        return recommendations;
    }

    @Override
    @Transactional
    public JobRecommendationThresholdResponse updateThreshold(BigDecimal thresholdPercent) {
        JobRecommendationConfig config = configRepository
                .findById(JobRecommendationConfig.SINGLETON_ID)
                .orElseGet(() -> JobRecommendationConfig.builder()
                        .id(JobRecommendationConfig.SINGLETON_ID)
                        .build());
        config.setMatchThresholdPercent(thresholdPercent);
        JobRecommendationConfig saved = configRepository.save(config);
        return JobRecommendationThresholdResponse.builder()
                .thresholdPercent(saved.getMatchThresholdPercent())
                .build();
    }

    private boolean isEligible(JobDescription job, LocalDateTime now) {
        return job.getStatus() == JobDescriptionStatus.OPEN
                && Boolean.FALSE.equals(job.getIsDeleted())
                && (job.getDeadlineAt() == null || !job.getDeadlineAt().isBefore(now));
    }

    private ScoredJob scoreJob(
            int userId, JobDescription job, float[] preferenceVector, double threshold, boolean eligible) {
        log.info(
                "Bắt đầu so sánh vector: userId={}, jdId={}, userVectorDimension={}, jdVectorDimension={}",
                userId,
                job.getId(),
                preferenceVector.length,
                job.getSkillEmbedding().length);
        double score = VectorUtils.cosineSimilarity(preferenceVector, job.getSkillEmbedding());
        log.info(
                "Kết quả matching JD: userId={}, jdId={}, title='{}', status={}, deadlineAt={}, matchPercent={}%, thresholdPercent={}%, eligible={}, matched={}",
                userId,
                job.getId(),
                job.getTitle(),
                job.getStatus(),
                job.getDeadlineAt(),
                score,
                threshold,
                eligible,
                eligible && score >= threshold);
        return new ScoredJob(job, score, eligible);
    }

    private record ScoredJob(JobDescription job, double score, boolean eligible) {}
}
