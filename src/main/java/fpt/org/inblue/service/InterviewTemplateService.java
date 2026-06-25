package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.TemplateDto;
import fpt.org.inblue.model.dto.request.UpsertTemplateRequest;
import java.util.List;

public interface InterviewTemplateService {

    List<TemplateDto.SummaryResponse> getAllTemplates();

    TemplateDto.DetailResponse getTemplateById(Long id);

    Long createTemplate(UpsertTemplateRequest request);

    void updateTemplate(Long id, UpsertTemplateRequest request);

    void deleteTemplate(Long id);
}
