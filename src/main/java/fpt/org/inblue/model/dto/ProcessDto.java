package fpt.org.inblue.model.dto;

import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.SubmitRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDto {
     Application application;
     Round round;
    // Dành cho vòng tự luận, Email, SQL Script (Frontend gửi text lên)
    private String textContent;
    private MultipartFile file;
    private List<String> quizAnswers;

}
