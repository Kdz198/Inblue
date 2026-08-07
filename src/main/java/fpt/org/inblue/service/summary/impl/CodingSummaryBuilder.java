package fpt.org.inblue.service.summary.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.service.CodingProblemService;
import fpt.org.inblue.service.summary.RoundSummaryBuilder;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CodingSummaryBuilder implements RoundSummaryBuilder {
    private final CodingProblemService codingProblemService;

    public CodingSummaryBuilder(CodingProblemService codingProblemService) {
        this.codingProblemService = codingProblemService;
    }

    @Override
    public AISummaryRequest.RoundSummaryInfo buildSummary(ApplicationDetail detail, Round roundConfig) {
        List<ApplicationDetail.CodeSubmission> submissions =
                detail.getSubmissionData() != null && detail.getSubmissionData().getCodeSubmissions() != null
                        ? detail.getSubmissionData().getCodeSubmissions()
                        : Collections.emptyList();
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < submissions.size(); i++) {
            ApplicationDetail.CodeSubmission submission = submissions.get(i);
            CompilerResponseDto testCases = submission.getTestCases();

            String problemTitle = resolveProblemTitle(submission, roundConfig, i);
            int passed = testCases != null ? testCases.getPassedTestCases() : 0;
            int total = testCases != null ? testCases.getTotalTestCases() : 0;

            summary.append(String.format("%s: pass %d/%d test.", problemTitle, passed, total));

            if (testCases != null && passed < total) {
                String failReason = extractFirstFailReason(testCases);
                if (failReason != null) {
                    summary.append(" Error: ").append(failReason).append(".");
                }
            }
            summary.append(" ");
        }

        return AISummaryRequest.RoundSummaryInfo.builder()
                .roundName(roundConfig.getName())
                .roundType(RoundType.CODING)
                .roundOrder(roundConfig.getRoundOrder())
                .score(detail.getFinalScore())
                .maxScore(resolveMaxScore(roundConfig))
                .finalResult(
                        detail.getFinalResult() != null
                                ? detail.getFinalResult().name()
                                : null)
                .summary(summary.toString().trim())
                .hrNote(detail.getHrNote())
                .hrScore(detail.getHrScore())
                .build();
    }

    private String resolveProblemTitle(ApplicationDetail.CodeSubmission submission, Round roundConfig, int index) {
        String titleFromRoundConfig = resolveProblemTitleFromRoundConfig(submission.getProblemId(), roundConfig);
        if (titleFromRoundConfig != null) {
            return titleFromRoundConfig;
        }
        if (submission.getProblemId() <= 0) {
            return "Problem " + (index + 1);
        }
        return codingProblemService
                .findCodingProblemById(submission.getProblemId())
                .map(problem ->
                        problem.getTitle() != null ? problem.getTitle() : "Problem " + submission.getProblemId())
                .orElse("Problem " + submission.getProblemId());
    }

    private String resolveProblemTitleFromRoundConfig(long problemId, Round roundConfig) {
        if (problemId <= 0
                || roundConfig.getConfigData() == null
                || roundConfig.getConfigData().getCodingProblems() == null) {
            return null;
        }
        return roundConfig.getConfigData().getCodingProblems().stream()
                .filter(problem -> problem.getProblemId() != null && problem.getProblemId() == problemId)
                .findFirst()
                .map(Round.CodingProblemSnapshot::getTitle)
                .orElse(null);
    }

    private Double resolveMaxScore(Round roundConfig) {
        if (roundConfig.getConfigData() == null || roundConfig.getConfigData().getMaxScore() == null) {
            return null;
        }
        return roundConfig.getConfigData().getMaxScore().doubleValue();
    }

    private String extractFirstFailReason(CompilerResponseDto testCases) {
        if (testCases.getTestCases() == null) {
            return null;
        }

        return testCases.getTestCases().stream()
                .filter(testCase -> !"PASSED".equalsIgnoreCase(testCase.getStatus()))
                .findFirst()
                .map(testCase -> {
                    String error = testCase.getErrorMessage();
                    if (error == null) {
                        return testCase.getStatus();
                    }
                    String firstLine = error.split("\n")[0];
                    return firstLine.length() > 120 ? firstLine.substring(0, 120) + "..." : firstLine;
                })
                .orElse(null);
    }
}
