package fpt.org.inblue.entrytest.service;

import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleRequest;
import fpt.org.inblue.entrytest.dto.request.UpsertLevelScaleSetRequest;
import fpt.org.inblue.entrytest.model.LevelScale;
import java.util.List;

public interface AdminLevelScaleService {
    List<LevelScale> getAllLevelScales();

    LevelScale getLevelScale(Long id);

    LevelScale createLevelScale(UpsertLevelScaleRequest request);

    LevelScale updateLevelScale(Long id, UpsertLevelScaleRequest request);

    LevelScale deactivateLevelScale(Long id);

    List<LevelScale> upsertLevelScaleSet(UpsertLevelScaleSetRequest request);
}
