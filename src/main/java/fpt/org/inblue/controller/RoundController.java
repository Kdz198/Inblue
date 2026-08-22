package fpt.org.inblue.controller;

import static fpt.org.inblue.enums.AnythingLlmWorkspace.CODING_GEN;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.WhiteboardQuestionDto;
import fpt.org.inblue.model.dto.request.SetupJdRoundsRequest;
import fpt.org.inblue.model.dto.request.UpdateJdRoundRequest;
import fpt.org.inblue.model.dto.response.RoundPlanGenerationResponse;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final RoundService roundService;
    private final ApiClient apiClient;

    @PutMapping("/jd/{jdId}")
    @Operation(
            summary = "Thiết lập các vòng phỏng vấn cho một Job Description",
            description = "Cho phép nhà tuyển dụng thiết lập các vòng phỏng vấn cho một JD cụ thể. ")
    public ResponseEntity<List<Round>> setUpRoundForJd(
            @PathVariable Long jdId, @RequestBody SetupJdRoundsRequest request) {
        List<Round> rounds = roundService.setUpRoundForJd(jdId, request);
        return ResponseEntity.ok(rounds);
    }

    @GetMapping
    public List<RoundType> getAllRoundTypes() {
        return roundService.getAllRoundTypes();
    }

    @PutMapping("/jd/{jdId}/update")
    @Operation(
            summary = "Cập nhật các vòng phỏng vấn cho một Job Description",
            description = "Cho phép nhà tuyển dụng cập nhật các vòng phỏng vấn đã thiết lập cho một JD cụ thể. ")
    public ResponseEntity<List<Round>> updateRoundForJd(
            @PathVariable Long jdId, @RequestBody UpdateJdRoundRequest request) {
        List<Round> rounds = roundService.updateRoundForJd(jdId, request);
        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/jd/{jdId}/generate-round-plan")
    @Operation(summary = "Generate a draft round plan from a Job Description via AnythingLLM")
    public ResponseEntity<RoundPlanGenerationResponse> generateRoundPlan(@PathVariable Long jdId) {
        return ResponseEntity.ok(roundService.generateRoundPlan(jdId));
    }

    @PostMapping("/generate-whiteboard-question")
    public WhiteboardQuestionDto generateQuestion(@RequestBody String hrIdea) {

        return apiClient.sendChatToAnythingLlm(
                CODING_GEN,
                "Tạo đề bài lập trình với yêu cầu sau: " + hrIdea,
                "Java - Backend",
                true,
                null,
                WhiteboardQuestionDto.class);
    }

    @GetMapping("/find-by-application-order/{applicationId}")
    public ResponseEntity<Round> findByApplicationOrder(@PathVariable Long applicationId) {
        Round rounds = roundService.getRoundByOrder(applicationId);
        return ResponseEntity.ok(rounds);
    }
}
