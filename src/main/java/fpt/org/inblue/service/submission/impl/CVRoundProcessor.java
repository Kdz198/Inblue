package fpt.org.inblue.service.submission.impl;

import static fpt.org.inblue.enums.RoundType.CV_SCREENING;

import fpt.org.inblue.enums.RoundType;
import fpt.org.inblue.event.SubmissionEventHandle;
import fpt.org.inblue.model.dto.ProcessDto;
import fpt.org.inblue.service.submission.RoundSubmissionProcessor;
import fpt.org.inblue.service.submission.SubmissionResult;
import fpt.org.inblue.utils.FileUtil;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class CVRoundProcessor implements RoundSubmissionProcessor {

    private final SubmissionEventHandle submissionEventHandle;

    @Override
    public RoundType getSupportedType() {
        return CV_SCREENING;
    }

    @Override
    public SubmissionResult process(ProcessDto dto) throws IOException {
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            String absolutePath = FileUtil.saveFile(dto.getFile());
            File file = FileUtil.getFileByPath(absolutePath);
            try {
                MultipartFile multipartFile = FileUtil.convertFileToMultipart(file);
                dto.setFile(multipartFile);
            } finally {
                file.delete();
            }
        }

        return SubmissionResult.completed(submissionEventHandle.processCvSubmission(dto));
    }
}
