package fpt.org.inblue.service.summary;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.ApplicationDetail;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.service.summary.impl.AiInterviewSummaryBuilder;
import fpt.org.inblue.service.summary.impl.CodeReviewSummaryBuilder;
import fpt.org.inblue.service.summary.impl.CodingSummaryBuilder;
import fpt.org.inblue.service.summary.impl.CvSummaryBuilder;
import fpt.org.inblue.service.summary.impl.EmailSummaryBuilder;
import fpt.org.inblue.service.summary.impl.MentorReviewSummaryBuilder;
import fpt.org.inblue.service.summary.impl.QuizSummaryBuilder;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RoundSummaryFactory {
    private final Map<RoundType, RoundSummaryBuilder> summaryBuilders;

    public RoundSummaryFactory(
            QuizSummaryBuilder quiz,
            CvSummaryBuilder cv,
            CodingSummaryBuilder coding,
            EmailSummaryBuilder email,
            CodeReviewSummaryBuilder codeReview,
            MentorReviewSummaryBuilder mentorReview,
            AiInterviewSummaryBuilder aiInterview) {
        this.summaryBuilders = Map.of(
                RoundType.QUIZ,
                quiz,
                RoundType.CV_SCREENING,
                cv,
                RoundType.CODING,
                coding,
                RoundType.EMAIL_SIMULATOR,
                email,
                RoundType.CODE_REVIEW,
                codeReview,
                RoundType.MENTROR_REVIEW,
                mentorReview,
                RoundType.AI_INTERVIEW,
                aiInterview);
    }

    public RoundSummaryBuilder getBuilder(RoundType roundType) {
        RoundSummaryBuilder builder = summaryBuilders.get(roundType);
        if (builder == null) {
            throw new CustomException("Unsupported round summary type: " + roundType, HttpStatus.BAD_REQUEST);
        }
        return builder;
    }

    public AISummaryRequest.RoundSummaryInfo build(ApplicationDetail detail, Round roundConfig) {
        if (roundConfig == null || roundConfig.getRoundType() == null) {
            throw new CustomException("Round config is required to build summary", HttpStatus.BAD_REQUEST);
        }
        return getBuilder(roundConfig.getRoundType()).buildSummary(detail, roundConfig);
    }

    public AISummaryRequest.RoundSummaryInfo buildSummary(RoundType roundType, ApplicationDetail detail) {
        Round roundConfig = Round.builder().roundType(roundType).build();
        return getBuilder(roundType).buildSummary(detail, roundConfig);
    }
}
