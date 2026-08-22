package fpt.org.inblue.service;

import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.model.dto.request.TopDevJobImportRequest;
import fpt.org.inblue.model.dto.response.TopDevJobImportResponse;
import fpt.org.inblue.model.dto.response.TopDevJobPreviewResponse;
import java.util.List;

public interface TopDevCrawlerService {

    List<TopDevJobPreviewResponse> searchJobs(
            String keyword, TargetLevel level, List<Integer> jobCategoryIds, int page, int limit);

    TopDevJobImportResponse importJob(TopDevJobImportRequest request);

}
