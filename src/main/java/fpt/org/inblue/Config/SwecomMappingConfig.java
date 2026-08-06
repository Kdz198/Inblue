package fpt.org.inblue.Config;

import fpt.org.inblue.enums.RoundType;
import java.util.List;
import java.util.Map;

public class SwecomMappingConfig {
    private SwecomMappingConfig() {}

    public static final Map<RoundType, List<String>> TECHNICAL_MAPPING = Map.of(
            RoundType.CODING,
            List.of("Software Construction"),
            RoundType.CODE_REVIEW,
            List.of("Software Quality", "Software Construction"),
            RoundType.QUIZ,
            List.of("Software Requirements", "Software Design"),
            RoundType.MENTROR_REVIEW,
            List.of("Software Process and Life Cycle"),
            RoundType.AI_INTERVIEW,
            List.of("Software Systems Engineering"));

    public static final Map<RoundType, String> BEHAVIORAL_MAPPING = Map.of(
            RoundType.CV_SCREENING,
            "Aptitude",
            RoundType.EMAIL_SIMULATOR,
            "Communication Skills",
            RoundType.MENTROR_REVIEW,
            "Team Participation Skills");
}
