package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.response.CompetencyChartResponse;

public interface CompetencyChartService {
    CompetencyChartResponse getCompetencyChart(Long applicationId);
}
