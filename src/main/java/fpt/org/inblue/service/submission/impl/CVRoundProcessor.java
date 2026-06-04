package fpt.org.inblue.service.submission.impl;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.model.dto.SubmissionResult;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import fpt.org.inblue.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static fpt.org.inblue.enums.RoundType.CV_SCREENING;

@Component
@RequiredArgsConstructor
public class CVRoundProcessor implements RoundSubmissionProcessor {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public RoundType getSupportedType() {
        return CV_SCREENING;
    }

    @Override
    public SubmissionResult process(ProcessDto dto) throws IOException {
        //bỏ vào cvEvaluationRequest rồi gọi qua python service sau đó nhận response và lưu vào application detail
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            String absolutePath = FileUtil.saveFile(dto.getFile());
            File file = FileUtil.getFileByPath(absolutePath);
            MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
            file.delete();
            dto.setFile(multipartFile);
            applicationEventPublisher.publishEvent(dto);
        }
        return SubmissionResult.pending(dto.getApplication().getId());
    }
}
