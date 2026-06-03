package fpt.org.inblue.controller;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.PythonService;
import fpt.org.inblue.model.dto.request.CvEvaluationRequest;
import fpt.org.inblue.model.dto.response.CVParserResponse;
import fpt.org.inblue.model.dto.response.CvEvaluationResponse;
import fpt.org.inblue.service.LLMApiClient;
import fpt.org.inblue.service.RedisTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/test")
public class TestController {

    private final RedisTestService redisTestService;
    private final LLMApiClient LLMApiClient;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Inblue!";
    }

    @GetMapping("/test")
    public String test() {
        log.info("test trace Id");
        log.info("MDC traceId={}", MDC.get("traceId"));
        log.info("MDC spanId={}", MDC.get("spanId"));
        return "This is a test endpoint for CI/CD.";
    }

    @GetMapping("/error")
    public String triggerError() {
        // Tạo một lỗi Generic Exception để test
        redisTestService.testLog();
        throw new RuntimeException("Đây là lỗi có chủ đích để test TraceID trên Dozzle!");
    }

    @GetMapping("/status")
    public String status() {
        return "Application is running smoothly.";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping("/food-test-hash")
    public Object testFoodHash(@RequestParam String id, @RequestParam String name, @RequestParam String cate, @RequestParam String country, @RequestParam String fieldToUpdate, @RequestParam String newValue) {
        RedisTestService.Food food = new RedisTestService.Food(name, cate, country);

        redisTestService.saveFoodAsHash(id, food);

        if (newValue != null && !newValue.isEmpty() && fieldToUpdate != null && !fieldToUpdate.isEmpty()) {
            redisTestService.updateSingleField(id, fieldToUpdate, newValue);
        }

        return redisTestService.getFoodHash(id);
    }

    @PostMapping(value = "/python-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CVParserResponse testPythonApi(@RequestParam("file") MultipartFile file) {

        CVParserResponse response = LLMApiClient.callApi(PythonService.LLM, ApiPath.CV_API, HttpMethod.POST, file, CVParserResponse.class);


        return response;
    }

    @PostMapping(value = "/cv-evaluation-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Đánh giá CV dựa trên tiêu chí và mô tả công việc",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "evaluationCriteria", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "jobDescription", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            )
    )
    public CvEvaluationResponse testCvEvaluation(
            @RequestPart("cvFile") MultipartFile cv,
            @RequestPart("evaluationCriteria") CvEvaluationRequest.EvaluationCriteria criteria,
            @RequestPart("jobDescription") CvEvaluationRequest.JD jd
    ) {
        CvEvaluationRequest cvEvaluationRequest = new CvEvaluationRequest();
        cvEvaluationRequest.setCvFile(cv);
        cvEvaluationRequest.setEvaluationCriteria(criteria);
        cvEvaluationRequest.setJobDescription(jd);

        List<MultipartFile> fileList = new ArrayList<>();
        if (cv != null && !cv.isEmpty()) {
            fileList.add(cv);
        }

        return LLMApiClient.sendChatToAnythingLlm(
                AnythingLlmWorkspace.CV_ANALYSIS,
                cvEvaluationRequest,
                "test-session-id",
                false,
                fileList,
                CvEvaluationResponse.class
        );
    }
}











