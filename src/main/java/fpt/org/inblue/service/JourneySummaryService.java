package fpt.org.inblue.service;

import fpt.org.inblue.model.JourneySummary;
import fpt.org.inblue.model.dto.request.AISummaryRequest;
import fpt.org.inblue.model.dto.request.JourneySummaryAIRequest;
import fpt.org.inblue.model.dto.response.CompetencyChartResponse;

import java.util.List;

public interface JourneySummaryService {
    JourneySummaryAIRequest generate(Long applicationId);

    JourneySummaryAIRequest buildAIRequest(Long applicationId);

    AISummaryRequest buildSummaryRequest(Long applicationId);

    JourneySummary saveNarrative(Long applicationId, String narrative);

    JourneySummary getSavedSummary(Long applicationId);

    CompetencyChartResponse getSavedCompetencyChart(Long applicationId);

    void generateMissingScripts();

    List<JourneySummary> getAllJourneyByUser(String email);

}
