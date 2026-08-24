package fpt.org.inblue.entrytest.service.impl;

import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleRequest;
import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleSetRequest;
import fpt.org.inblue.entrytest.entity.LevelScale;
import fpt.org.inblue.entrytest.repository.LevelScaleRepository;
import fpt.org.inblue.entrytest.service.AdminLevelScaleService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.enums.TargetLevel;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLevelScaleServiceImpl implements AdminLevelScaleService {
    private final LevelScaleRepository levelScaleRepository;

    @Override
    public List<LevelScale> getAllLevelScales() {
        return levelScaleRepository.findAll();
    }

    @Override
    public LevelScale getLevelScale(Long id) {
        return levelScaleRepository
                .findById(id)
                .orElseThrow(() -> new CustomException("Level scale not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public LevelScale createLevelScale(UpsertLevelScaleRequest request) {
        validate(request);
        return levelScaleRepository.save(LevelScale.builder()
                .targetRole(request.getTargetRole())
                .level(request.getLevel())
                .minScore(request.getMinScore())
                .maxScore(request.getMaxScore())
                .minCodingScore(request.getMinCodingScore())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build());
    }

    @Override
    @Transactional
    public LevelScale updateLevelScale(Long id, UpsertLevelScaleRequest request) {
        LevelScale levelScale = getLevelScale(id);
        if (request.getTargetRole() != null) {
            levelScale.setTargetRole(request.getTargetRole());
        }
        if (request.getLevel() != null) {
            levelScale.setLevel(request.getLevel());
        }
        if (request.getMinScore() != null) {
            levelScale.setMinScore(request.getMinScore());
        }
        if (request.getMaxScore() != null) {
            levelScale.setMaxScore(request.getMaxScore());
        }
        if (request.getMinCodingScore() != null) {
            levelScale.setMinCodingScore(request.getMinCodingScore());
        }
        if (request.getIsActive() != null) {
            levelScale.setIsActive(request.getIsActive());
        }
        validate(levelScale);
        return levelScaleRepository.save(levelScale);
    }

    @Override
    @Transactional
    public LevelScale deactivateLevelScale(Long id) {
        LevelScale levelScale = getLevelScale(id);
        levelScale.setIsActive(false);
        return levelScaleRepository.save(levelScale);
    }

    @Override
    @Transactional
    public List<LevelScale> upsertLevelScaleSet(UpsertLevelScaleSetRequest request) {
        if (request.getScales() == null || request.getScales().isEmpty()) {
            throw new CustomException("scales cannot be empty", HttpStatus.BAD_REQUEST);
        }
        List<LevelScale> existingScales = request.getTargetRole() == null
                ? levelScaleRepository.findAllByTargetRoleIsNull()
                : levelScaleRepository.findAllByTargetRole(request.getTargetRole());
        Map<TargetLevel, LevelScale> existingByLevel = existingScales.stream()
                .filter(scale -> scale.getLevel() != null)
                .collect(Collectors.toMap(LevelScale::getLevel, Function.identity(), (left, right) -> left));

        List<LevelScale> upserted = request.getScales().stream()
                .map(scaleRequest -> {
                    validate(scaleRequest);
                    LevelScale scale = existingByLevel.getOrDefault(
                            scaleRequest.getLevel(),
                            LevelScale.builder()
                                    .targetRole(request.getTargetRole())
                                    .level(scaleRequest.getLevel())
                                    .build());
                    scale.setTargetRole(request.getTargetRole());
                    scale.setLevel(scaleRequest.getLevel());
                    scale.setMinScore(scaleRequest.getMinScore());
                    scale.setMaxScore(scaleRequest.getMaxScore());
                    scale.setMinCodingScore(scaleRequest.getMinCodingScore());
                    scale.setIsActive(scaleRequest.getIsActive() == null || scaleRequest.getIsActive());
                    return scale;
                })
                .toList();

        return levelScaleRepository.saveAll(upserted);
    }

    private void validate(UpsertLevelScaleRequest request) {
        if (request.getLevel() == null) {
            throw new CustomException("level is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getMinScore() == null || request.getMaxScore() == null) {
            throw new CustomException("minScore and maxScore are required", HttpStatus.BAD_REQUEST);
        }
        if (request.getMinScore() > request.getMaxScore()) {
            throw new CustomException("minScore cannot be greater than maxScore", HttpStatus.BAD_REQUEST);
        }
    }

    private void validate(LevelScale levelScale) {
        if (levelScale.getLevel() == null) {
            throw new CustomException("level is required", HttpStatus.BAD_REQUEST);
        }
        if (levelScale.getMinScore() == null || levelScale.getMaxScore() == null) {
            throw new CustomException("minScore and maxScore are required", HttpStatus.BAD_REQUEST);
        }
        if (levelScale.getMinScore() > levelScale.getMaxScore()) {
            throw new CustomException("minScore cannot be greater than maxScore", HttpStatus.BAD_REQUEST);
        }
    }
}
