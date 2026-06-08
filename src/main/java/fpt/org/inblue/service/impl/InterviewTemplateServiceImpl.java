package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.InterviewTemplate;
import fpt.org.inblue.model.TemplateRound;
import fpt.org.inblue.model.dto.TemplateDto;
import fpt.org.inblue.model.dto.request.UpsertTemplateRequest;
import fpt.org.inblue.repository.InterviewTemplateRepository;
import fpt.org.inblue.service.InterviewTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewTemplateServiceImpl implements InterviewTemplateService {



    private final InterviewTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public List<TemplateDto.SummaryResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(template -> TemplateDto.SummaryResponse.builder()
                        .id(template.getId())
                        .name(template.getName())
                        .category(template.getCategory())
                        .description(template.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TemplateDto.DetailResponse getTemplateById(Long id) {
        InterviewTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Template với ID: " + id));

        List<TemplateDto.RoundItem> roundItems = template.getRounds().stream()
                .map(round -> TemplateDto.RoundItem.builder()
                        .name(round.getName())
                        .roundOrder(round.getRoundOrder())
                        .roundType(round.getRoundType())
                        .passThreshold(round.getPassThreshold())
                        .configData(round.getConfigData())
                        .build())
                .collect(Collectors.toList());

        return TemplateDto.DetailResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .category(template.getCategory())
                .description(template.getDescription())
                .rounds(roundItems)
                .build();
    }

    @Transactional
    public Long createTemplate(UpsertTemplateRequest request) {
        InterviewTemplate template = InterviewTemplate.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .build();

        // Map danh sách các vòng
        List<TemplateRound> rounds = request.getRounds().stream()
                .map(this::mapToTemplateRound)
                .collect(Collectors.toList());

        template.setRounds(rounds);
        return templateRepository.save(template).getId();
    }

    @Transactional
    public void updateTemplate(Long id, UpsertTemplateRequest request) {
        InterviewTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Template"));

        // 1. Cập nhật thông tin cơ bản
        template.setName(request.getName());
        template.setCategory(request.getCategory());
        template.setDescription(request.getDescription());

        // 2. Clear danh sách vòng cũ (Hibernate sẽ tự động DELETE các record cũ nhờ orphanRemoval=true)
        template.getRounds().clear();

        // 3. Thêm danh sách vòng mới vào
        List<TemplateRound> newRounds = request.getRounds().stream()
                .map(this::mapToTemplateRound)
                .toList();
        template.getRounds().addAll(newRounds);

        templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Template");
        }
        templateRepository.deleteById(id);
    }

    // Hàm helper để map DTO sang Entity
    private TemplateRound mapToTemplateRound(UpsertTemplateRequest.TemplateRoundItem dto) {
        return TemplateRound.builder()
                .name(dto.getName())
                .roundOrder(dto.getRoundOrder())
                .roundType(dto.getRoundType())
                .passThreshold(dto.getPassThreshold())
                .configData(dto.getConfigData())
                .build();
    }
}
