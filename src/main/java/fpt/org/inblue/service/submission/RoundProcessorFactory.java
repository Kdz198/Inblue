package fpt.org.inblue.service.submission;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.service.submission.impl.CVRoundProcessor;
import fpt.org.inblue.service.submission.impl.EmailRoundProcessor;
import fpt.org.inblue.service.submission.impl.QuizRoundProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RoundProcessorFactory {

    private final QuizRoundProcessor quizProcessor;
    private final EmailRoundProcessor emailProcessor;
    private final CVRoundProcessor  cvProcessor;

    public RoundSubmissionProcessor getProcessor(RoundType type) {
        return switch (type) {
            case QUIZ -> quizProcessor;
            case EMAIL_SIMULATOR -> emailProcessor;
            case CV_SCREENING -> cvProcessor;
            default -> throw new IllegalArgumentException("Hệ thống chưa hỗ trợ xử lý cho loại vòng: " + type);
        };
    }
}
