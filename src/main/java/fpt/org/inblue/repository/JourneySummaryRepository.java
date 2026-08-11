package fpt.org.inblue.repository;

import fpt.org.inblue.model.JourneySummary;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneySummaryRepository extends JpaRepository<JourneySummary, Long> {
    Optional<JourneySummary> findTopByApplicationIdOrderByGeneratedAtDesc(Long applicationId);

    List<JourneySummary> findByScriptIsNull();

    List<JourneySummary> findByAudioUrlIsNull();

    List<JourneySummary> findAllByApplicationIdIn(Collection<Long> applicationIds);
}
