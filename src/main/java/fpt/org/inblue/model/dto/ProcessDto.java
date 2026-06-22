package fpt.org.inblue.model.dto;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.Application;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.Round;
import fpt.org.inblue.model.dto.request.CompileRequest;
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
    //vong mail thi: FE render dạng email form như To:, Subject:, Body: để ứng viên điền vào, sau đó gửi lên backend dưới dạng text
    private String textContent;
    private MultipartFile file;
    private List<String> quizAnswers;
    private RoundType roundType;
    private List<CompileRequest> compileRequest;
}
