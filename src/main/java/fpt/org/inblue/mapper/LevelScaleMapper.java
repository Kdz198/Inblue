package fpt.org.inblue.mapper;

import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleRequest;
import fpt.org.inblue.entrytest.enums.TargetRole;
import fpt.org.inblue.entrytest.model.LevelScale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LevelScaleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", source = "isActive", defaultValue = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LevelScale toEntity(UpsertLevelScaleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UpsertLevelScaleRequest request, @MappingTarget LevelScale levelScale);

    default LevelScale toEntityForSet(UpsertLevelScaleRequest request, TargetRole targetRole) {
        LevelScale levelScale = toEntity(request);
        applySetValues(request, targetRole, levelScale);
        return levelScale;
    }

    default void updateFromSetRequest(
            UpsertLevelScaleRequest request, TargetRole targetRole, @MappingTarget LevelScale levelScale) {
        updateFromRequest(request, levelScale);
        applySetValues(request, targetRole, levelScale);
    }

    private void applySetValues(UpsertLevelScaleRequest request, TargetRole targetRole, LevelScale levelScale) {
        levelScale.setTargetRole(targetRole);
        levelScale.setIsActive(request.getIsActive() == null || request.getIsActive());
    }
}
