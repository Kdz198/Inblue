package fpt.org.inblue.controller;


import fpt.org.inblue.model.dto.response.FaceAnalysisResponse;
import fpt.org.inblue.service.FaceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/interview-analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InterviewAnalysisController {

    private final FaceAnalysisService faceAnalysisService;


    @PostMapping(value = "/face-behavior",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceAnalysisResponse analyzeFaceBehavior(@RequestPart("image") MultipartFile file) {
        if (file.isEmpty()) return  null;

        // Todo implement logic call Python service
        FaceAnalysisResponse response = faceAnalysisService.analyzeFace(file);
        System.out.println("Name file received: " + file.getOriginalFilename());

        return null;
    }

}