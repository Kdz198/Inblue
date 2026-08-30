package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.enums.CompilerLanguage;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.model.User;
import fpt.org.inblue.entrytest.model.EntryTest;
import fpt.org.inblue.entrytest.model.EntryTestAttempt;
import fpt.org.inblue.model.QuestionBank;
import fpt.org.inblue.entrytest.model.UserCareerPreference;
import fpt.org.inblue.model.dto.request.CompilerRequestDto;
import fpt.org.inblue.entrytest.dto.request.EntryTestRunCodeRequest;
import fpt.org.inblue.entrytest.dto.request.EntryTestSubmitRequest;
import fpt.org.inblue.model.dto.response.CompilerResponseDto;
import fpt.org.inblue.entrytest.dto.response.EntryTestStartResponse;
import fpt.org.inblue.mapper.EntryTestResponseMapper;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.entrytest.repository.EntryTestAttemptRepository;
import fpt.org.inblue.entrytest.repository.EntryTestRepository;
import fpt.org.inblue.repository.QuestionBankRepository;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.entrytest.repository.UserCareerPreferenceRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.entrytest.service.EntryTestService;
import fpt.org.inblue.entrytest.service.UserCompetencyService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EntryTestServiceImpl implements EntryTestService {
    private static final String COMMON_CATEGORY = "COMMON";

    private final EntryTestRepository entryTestRepository;
    private final EntryTestAttemptRepository attemptRepository;
    private final UserCareerPreferenceRepository preferenceRepository;
    private final QuestionBankRepository questionBankRepository;
    private final CodingProblemsRepository codingProblemsRepository;
    private final ApiClient apiClient;
    private final UserCompetencyService userCompetencyService;
    private final UserRepository userRepository;
    private final EntryTestResponseMapper responseMapper;

    @Override
    @Transactional
    public EntryTestStartResponse startEntryTest(Integer userId) {
        UserCareerPreference preference = preferenceRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new CustomException("Career preference not found", HttpStatus.BAD_REQUEST));
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        EntryTest entryTest = getOrCreateActiveEntryTest();
        List<String> languages = normalizeLanguages(preference.getLanguagesJson());

        List<EntryTestAttempt.QuestionItemSnapshot> commonItems = new ArrayList<>();
        List<EntryTestAttempt.QuestionItemSnapshot> specificItems = new ArrayList<>();
        List<EntryTestAttempt.CodingProblemItemSnapshot> codingItems = new ArrayList<>();

        for (EntryTest.EntryTestSectionConfig config : orderedConfigs(entryTest)) {
            switch (config.getSectionType()) {
                case COMMON_QUIZ -> commonItems.addAll(pickCommonQuestions(config));
                case SPECIFIC_QUIZ -> specificItems.addAll(pickSpecificQuestions(config, preference.getTargetRole(), languages));
                case SPECIFIC_CODING -> codingItems.addAll(pickSpecificCodingProblems(config, preference.getTargetRole(), languages));
            }
        }

        EntryTestAttempt attempt = EntryTestAttempt.builder()
                .user(user)
                .careerPreferenceId(preference.getUserId())
                .entryTestId(entryTest.getId())
                .selectedLanguagesJson(preference.getLanguagesJson())
                .commonQuizItemsJson(commonItems)
                .specificQuizItemsJson(specificItems)
                .specificCodingItemsJson(codingItems)
                .status(EntryTestAttempt.AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        user.addEntryTestAttempt(attempt);
        attempt = attemptRepository.save(attempt);

        return EntryTestStartResponse.builder()
                .attemptId(attempt.getId())
                .entryTestId(entryTest.getId())
                .timeLimitMinutes(entryTest.getTimeLimitMinutes())
                .selectedLanguagesJson(attempt.getSelectedLanguagesJson())
                .sectionConfigs(entryTest.getSectionConfigs())
                .commonQuizItemsJson(responseMapper.toQuestionResponses(commonItems))
                .specificQuizItemsJson(responseMapper.toQuestionResponses(specificItems))
                .specificCodingItemsJson(codingItems)
                .build();
    }

    @Override
    public CompilerResponseDto runCode(
            Integer userId, Long attemptId, EntryTestRunCodeRequest request) {
        EntryTestAttempt attempt = getAttempt(userId, attemptId);
        if (attempt.getStatus() != EntryTestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new CustomException("Entry test attempt is not in progress", HttpStatus.BAD_REQUEST);
        }

        EntryTestAttempt.CodingProblemItemSnapshot item = Optional.ofNullable(
                        indexCodingItems(attempt.getSpecificCodingItemsJson()).get(request.getItemId()))
                .orElseThrow(() -> new CustomException(
                        "Coding item does not belong to this attempt", HttpStatus.BAD_REQUEST));
        validateSourceCode(request.getSourceCode());

        CompilerRequestDto compilerRequest = CompilerRequestDto.builder()
                .language(resolveCompilerLanguage(request.getLanguage()))
                .sourceCode(request.getSourceCode())
                .timeLimitMs(Optional.ofNullable(item.getExecutionTimeLimitMs()).orElse(1000))
                .memoryLimitMb(Optional.ofNullable(item.getMemoryLimitMb()).orElse(256))
                .paramTypes(item.getParamTypes())
                .returnType(item.getReturnType())
                .testCases(Optional.ofNullable(item.getVisibleExamples()).orElse(List.of()).stream()
                        .map(example -> CompilerRequestDto.TestCase.builder()
                                .inputs(example.getInputs())
                                .expectedOutput(example.getOutput())
                                .build())
                        .toList())
                .build();

        CompilerResponseDto response = apiClient.executeCode(compilerRequest);
        if (response == null) {
            throw new CustomException("Compiler sandbox did not return a result", HttpStatus.BAD_GATEWAY);
        }
        return response;
    }

    @Override
    @Transactional
    public EntryTestAttempt submitEntryTest(Integer userId, Long attemptId, EntryTestSubmitRequest request) {
        EntryTestAttempt attempt = getAttempt(userId, attemptId);
        if (attempt.getStatus() != EntryTestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new CustomException("Entry test attempt is not in progress", HttpStatus.BAD_REQUEST);
        }

        Map<String, EntryTestAttempt.QuestionItemSnapshot> commonByItemId =
                indexQuestionItems(attempt.getCommonQuizItemsJson());
        Map<String, EntryTestAttempt.QuestionItemSnapshot> specificByItemId =
                indexQuestionItems(attempt.getSpecificQuizItemsJson());
        Map<String, EntryTestAttempt.CodingProblemItemSnapshot> codingByItemId =
                indexCodingItems(attempt.getSpecificCodingItemsJson());
        List<EntryTestSubmitRequest.Answer> submittedAnswers =
                request == null || request.getAnswers() == null ? List.of() : request.getAnswers();
        validateSubmittedAnswers(submittedAnswers, commonByItemId, specificByItemId, codingByItemId);

        double commonScore = 0.0;
        double specificScore = 0.0;
        double codingScore = 0.0;
        List<EntryTestAttempt.EntryTestAnswerSnapshot> answers = new ArrayList<>();

        for (EntryTestSubmitRequest.Answer submitted : submittedAnswers) {
            if (commonByItemId.containsKey(submitted.getItemId())) {
                EntryTestAttempt.EntryTestAnswerSnapshot answer = gradeQuestionAnswer(
                        submitted,
                        commonByItemId.get(submitted.getItemId()),
                        EntryTest.SectionType.COMMON_QUIZ);
                commonScore += value(answer.getScore());
                answers.add(answer);
            } else if (specificByItemId.containsKey(submitted.getItemId())) {
                EntryTestAttempt.EntryTestAnswerSnapshot answer = gradeQuestionAnswer(
                        submitted,
                        specificByItemId.get(submitted.getItemId()),
                        EntryTest.SectionType.SPECIFIC_QUIZ);
                specificScore += value(answer.getScore());
                answers.add(answer);
            } else if (codingByItemId.containsKey(submitted.getItemId())) {
                EntryTestAttempt.EntryTestAnswerSnapshot answer = gradeCodingAnswer(
                        submitted,
                        codingByItemId.get(submitted.getItemId()));
                codingScore += value(answer.getScore());
                answers.add(answer);
            }
        }

        commonScore = round(commonScore);
        specificScore = round(specificScore);
        codingScore = round(codingScore);
        double finalScore = round(commonScore + specificScore + codingScore);

        attempt.setCommonQuizScore(commonScore);
        attempt.setSpecificQuizScore(specificScore);
        attempt.setSpecificCodingScore(codingScore);
        attempt.setAnswersJson(answers);
        attempt.setFinalScore(finalScore);
        attempt.setStatus(EntryTestAttempt.AttemptStatus.GRADED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setResultSnapshotJson(buildResultSnapshot(commonScore, specificScore, codingScore, finalScore));

        attempt.setResultLevel(userCompetencyService.resolveLevelName(attempt));
        EntryTestAttempt saved = attemptRepository.save(attempt);
        userCompetencyService.updateAfterEntryTest(saved);
        return saved;
    }

    private void validateSubmittedAnswers(
            List<EntryTestSubmitRequest.Answer> submittedAnswers,
            Map<String, EntryTestAttempt.QuestionItemSnapshot> commonByItemId,
            Map<String, EntryTestAttempt.QuestionItemSnapshot> specificByItemId,
            Map<String, EntryTestAttempt.CodingProblemItemSnapshot> codingByItemId) {
        Set<String> validItemIds = new HashSet<>();
        validItemIds.addAll(commonByItemId.keySet());
        validItemIds.addAll(specificByItemId.keySet());
        validItemIds.addAll(codingByItemId.keySet());

        Set<String> submittedItemIds = new HashSet<>();
        for (EntryTestSubmitRequest.Answer answer : submittedAnswers) {
            if (answer == null || answer.getItemId() == null || answer.getItemId().isBlank()) {
                throw new CustomException("Each answer must contain itemId", HttpStatus.BAD_REQUEST);
            }
            if (!validItemIds.contains(answer.getItemId())) {
                throw new CustomException("Unknown entry test item: " + answer.getItemId(), HttpStatus.BAD_REQUEST);
            }
            if (!submittedItemIds.add(answer.getItemId())) {
                throw new CustomException("Duplicate entry test item: " + answer.getItemId(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    public EntryTestAttempt getAttempt(Integer userId, Long attemptId) {
        EntryTestAttempt attempt = attemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new CustomException("Entry test attempt not found", HttpStatus.NOT_FOUND));
        if (!Objects.equals(attempt.getUser().getId(), userId)) {
            throw new CustomException("Entry test attempt does not belong to current user", HttpStatus.FORBIDDEN);
        }
        return attempt;
    }

    private EntryTest getOrCreateActiveEntryTest() {
        return entryTestRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc().orElseGet(() -> entryTestRepository.save(
                EntryTest.builder()
                        .name("Software Engineer Entry Test")
                        .totalScore(100.0)
                        .timeLimitMinutes(60)
                        .isActive(true)
                        .build()));
    }

    private List<EntryTest.EntryTestSectionConfig> orderedConfigs(EntryTest entryTest) {
        return Optional.ofNullable(entryTest.getSectionConfigs()).orElse(List.of()).stream()
                .sorted(Comparator.comparing(config -> Optional.ofNullable(config.getDisplayOrder()).orElse(0)))
                .toList();
    }

    private List<EntryTestAttempt.QuestionItemSnapshot> pickCommonQuestions(EntryTest.EntryTestSectionConfig config) {
        List<QuestionBank> questions = questionBankRepository.findRandomByCategoryName(
                COMMON_CATEGORY, PageRequest.of(0, count(config)));
        ensureEnoughItems(questions.size(), count(config), EntryTest.SectionType.COMMON_QUIZ.name());
        return snapshotQuestionItems("COMMON", questions, scorePerItem(config));
    }

    private List<EntryTestAttempt.QuestionItemSnapshot> pickSpecificQuestions(
            EntryTest.EntryTestSectionConfig config, TargetRole role, List<String> languages) {
        Set<String> categoryNames = buildSpecificCategoryNames(role, languages);
        List<QuestionBank> questions = questionBankRepository.findRandomByCategoryNames(
                categoryNames, PageRequest.of(0, count(config)));
        ensureEnoughItems(questions.size(), count(config), EntryTest.SectionType.SPECIFIC_QUIZ.name());
        return snapshotQuestionItems("SPECIFIC", questions, scorePerItem(config));
    }

    private List<EntryTestAttempt.CodingProblemItemSnapshot> pickSpecificCodingProblems(
            EntryTest.EntryTestSectionConfig config, TargetRole role, List<String> languages) {
        List<CodingProblem> matching = codingProblemsRepository.findAllByIsDeletedFalse().stream()
                .filter(problem -> problem.getDifficulty() == CodingProblem.Difficulty.EASY)
                .collect(Collectors.toList());
        Collections.shuffle(matching);
        List<CodingProblem> picked = matching.stream().limit(count(config)).toList();
        ensureEnoughItems(picked.size(), count(config), EntryTest.SectionType.SPECIFIC_CODING.name());
        return snapshotCodingItems(picked, scorePerItem(config));
    }

    private EntryTestAttempt.EntryTestAnswerSnapshot gradeQuestionAnswer(
            EntryTestSubmitRequest.Answer submitted,
            EntryTestAttempt.QuestionItemSnapshot item,
            EntryTest.SectionType sectionType) {
        String selectedOption = normalizeAnswer(getAnswerValue(submitted.getAnswerJson(), "selectedOption"));
        String correctAnswer = normalizeAnswer(item.getCorrectAnswer());
        boolean correct = !selectedOption.isBlank() && selectedOption.equals(correctAnswer);

        return EntryTestAttempt.EntryTestAnswerSnapshot.builder()
                .itemId(submitted.getItemId())
                .sectionType(sectionType)
                .answerType(EntryTest.ItemType.QUESTION_BANK)
                .answerJson(submitted.getAnswerJson())
                .score(correct ? item.getMaxScore() : 0.0)
                .isCorrect(correct)
                .gradedAt(LocalDateTime.now())
                .build();
    }

    private EntryTestAttempt.EntryTestAnswerSnapshot gradeCodingAnswer(
            EntryTestSubmitRequest.Answer submitted,
            EntryTestAttempt.CodingProblemItemSnapshot item) {
        double score = scoreSubmittedCode(submitted.getAnswerJson(), item);
        return EntryTestAttempt.EntryTestAnswerSnapshot.builder()
                .itemId(submitted.getItemId())
                .sectionType(EntryTest.SectionType.SPECIFIC_CODING)
                .answerType(EntryTest.ItemType.CODING_PROBLEM)
                .answerJson(submitted.getAnswerJson())
                .score(score)
                .isCorrect(score >= value(item.getMaxScore()))
                .gradedAt(LocalDateTime.now())
                .build();
    }

    private double scoreSubmittedCode(
            Map<String, Object> answerJson, EntryTestAttempt.CodingProblemItemSnapshot item) {
        if (answerJson == null) {
            throw new CustomException("Coding answer is required", HttpStatus.BAD_REQUEST);
        }
        if (!answerJson.containsKey("language") || !answerJson.containsKey("sourceCode")) {
            throw new CustomException(
                    "Coding answer must contain language and sourceCode", HttpStatus.BAD_REQUEST);
        }
        return scoreByCompiler(answerJson, item);
    }

    private double scoreByCompiler(Map<String, Object> answerJson, EntryTestAttempt.CodingProblemItemSnapshot item) {
        CodingProblem problem = codingProblemsRepository
                .findById(item.getCodingProblemId())
                .orElseThrow(() -> new CustomException("Coding problem not found", HttpStatus.NOT_FOUND));

        List<String> sourceCode = resolveSourceCode(answerJson.get("sourceCode"));
        validateSourceCode(sourceCode);

        CompilerRequestDto compilerRequest = CompilerRequestDto.builder()
                .language(resolveCompilerLanguage(String.valueOf(answerJson.get("language"))))
                .sourceCode(sourceCode)
                .timeLimitMs(Optional.ofNullable(problem.getExecutionTimeLimitMs()).orElse(1000))
                .memoryLimitMb(Optional.ofNullable(problem.getMemoryLimitMb()).orElse(256))
                .paramTypes(problem.getParamTypes())
                .returnType(problem.getReturnType())
                .testCases(Optional.ofNullable(problem.getHiddenTestCases()).orElse(List.of()).stream()
                        .map(testCase -> CompilerRequestDto.TestCase.builder()
                                .inputs(testCase.getInputs())
                                .expectedOutput(testCase.getExpectedOutput())
                                .build())
                        .toList())
                .build();

        CompilerResponseDto response = apiClient.executeCode(compilerRequest);
        if (response == null || response.getTotalTestCases() <= 0) {
            return 0.0;
        }
        int passedTestCases = Math.max(0, Math.min(response.getPassedTestCases(), response.getTotalTestCases()));
        return clampScore(
                (passedTestCases * value(item.getMaxScore())) / response.getTotalTestCases(), item.getMaxScore());
    }

    private List<EntryTestAttempt.QuestionItemSnapshot> snapshotQuestionItems(
            String prefix, List<QuestionBank> questions, Double maxScore) {
        List<EntryTestAttempt.QuestionItemSnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            QuestionBank question = questions.get(i);
            snapshots.add(EntryTestAttempt.QuestionItemSnapshot.builder()
                    .itemId(prefix + "-" + (i + 1))
                    .questionBankId(question.getId())
                    .questionText(question.getQuestionText())
                    .options(question.getOptions())
                    .correctAnswer(question.getCorrectAnswer())
                    .categoryName(question.getQuestionCategory() != null
                            ? question.getQuestionCategory().getName()
                            : null)
                    .difficulty(question.getQuestionLevel() != null ? question.getQuestionLevel().name() : null)
                    .maxScore(maxScore)
                    .displayOrder(i + 1)
                    .build());
        }
        return snapshots;
    }

    private List<EntryTestAttempt.CodingProblemItemSnapshot> snapshotCodingItems(
            List<CodingProblem> problems, Double maxScore) {
        List<EntryTestAttempt.CodingProblemItemSnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < problems.size(); i++) {
            CodingProblem problem = problems.get(i);
            snapshots.add(EntryTestAttempt.CodingProblemItemSnapshot.builder()
                    .itemId("CODING-" + (i + 1))
                    .codingProblemId(problem.getId())
                    .title(problem.getTitle())
                    .difficulty(problem.getDifficulty() != null ? problem.getDifficulty().name() : null)
                    .problemStatement(problem.getProblemStatement())
                    .rulesAndConstraints(problem.getRulesAndConstraints())
                    .visibleExamples(problem.getVisibleExamples())
                    .codeStubs(problem.getCodeStubs())
                    .paramTypes(problem.getParamTypes())
                    .returnType(problem.getReturnType())
                    .executionTimeLimitMs(problem.getExecutionTimeLimitMs())
                    .memoryLimitMb(problem.getMemoryLimitMb())
                    .maxScore(maxScore)
                    .displayOrder(i + 1)
                    .build());
        }
        return snapshots;
    }

    private Set<String> buildSpecificCategoryNames(TargetRole role, List<String> languages) {
        Set<String> names = new LinkedHashSet<>();
        names.add(role.name());
        for (String language : languages) {
            names.add(normalizeCategory(language));
            names.add(role.name() + "_" + normalizeCategory(language));
        }
        return names;
    }

    private Map<String, Object> buildResultSnapshot(
            double commonScore, double specificScore, double codingScore, double finalScore) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("commonQuizScore", commonScore);
        snapshot.put("specificQuizScore", specificScore);
        snapshot.put("specificCodingScore", codingScore);
        snapshot.put("finalScore", finalScore);
        return snapshot;
    }

    private Map<String, EntryTestAttempt.QuestionItemSnapshot> indexQuestionItems(
            List<EntryTestAttempt.QuestionItemSnapshot> items) {
        return Optional.ofNullable(items).orElse(List.of()).stream()
                .collect(Collectors.toMap(EntryTestAttempt.QuestionItemSnapshot::getItemId, item -> item));
    }

    private Map<String, EntryTestAttempt.CodingProblemItemSnapshot> indexCodingItems(
            List<EntryTestAttempt.CodingProblemItemSnapshot> items) {
        return Optional.ofNullable(items).orElse(List.of()).stream()
                .collect(Collectors.toMap(EntryTestAttempt.CodingProblemItemSnapshot::getItemId, item -> item));
    }

    private String getAnswerValue(Map<String, Object> answerJson, String key) {
        if (answerJson == null) {
            return "";
        }
        Object value = answerJson.getOrDefault(key, answerJson.get("answer"));
        return value == null ? "" : String.valueOf(value);
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String normalized = answer.trim();
        int dotIndex = normalized.indexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }
        return normalized.trim().toUpperCase(Locale.ROOT);
    }

    private List<String> normalizeLanguages(List<String> languages) {
        return Optional.ofNullable(languages).orElse(List.of()).stream()
                .filter(language -> language != null && !language.isBlank())
                .toList();
    }

    private String normalizeCategory(String value) {
        return value == null ? "" : value.trim()
                .replace("#", "SHARP")
                .replace(".", "")
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);
    }

    private CompilerLanguage resolveCompilerLanguage(String language) {
        String normalized = normalizeCategory(language);
        if ("JAVASCRIPT".equals(normalized)) {
            normalized = "JS";
        }
        if ("CSHARP".equals(normalized) || "C_SHARP".equals(normalized)) {
            normalized = "CSHARP";
        }
        try {
            return CompilerLanguage.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new CustomException("Unsupported compiler language: " + language, HttpStatus.BAD_REQUEST);
        }
    }

    private List<String> resolveSourceCode(Object sourceCode) {
        if (sourceCode instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (sourceCode instanceof String string) {
            return List.of(string);
        }
        return List.of();
    }

    private void validateSourceCode(List<String> sourceCode) {
        if (sourceCode == null || sourceCode.isEmpty() || sourceCode.stream().allMatch(
                line -> line == null || line.isBlank())) {
            throw new CustomException("sourceCode cannot be empty", HttpStatus.BAD_REQUEST);
        }
    }

    private int count(EntryTest.EntryTestSectionConfig config) {
        return Optional.ofNullable(config.getItemCount()).orElse(0);
    }

    private Double scorePerItem(EntryTest.EntryTestSectionConfig config) {
        return Optional.ofNullable(config.getScorePerItem()).orElse(0.0);
    }

    private void ensureEnoughItems(int actual, int expected, String section) {
        if (actual < expected) {
            throw new CustomException(
                    "Not enough items for " + section + ". Required " + expected + ", found " + actual,
                    HttpStatus.BAD_REQUEST);
        }
    }

    private double clampScore(double score, Double maxScore) {
        return Math.max(0.0, Math.min(round(score), value(maxScore)));
    }

    private double value(Double number) {
        return number == null ? 0.0 : number;
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
