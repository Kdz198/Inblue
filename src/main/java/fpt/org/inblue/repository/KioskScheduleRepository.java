package fpt.org.inblue.repository;

import fpt.org.inblue.model.KioskSchedule;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KioskScheduleRepository extends JpaRepository<KioskSchedule, Long> {
    List<KioskSchedule> findAllByKioskIdAndIsActiveTrue(Long kioskId);
    List<KioskSchedule> findAllByKioskIdAndDayOfWeekAndIsActiveTrue(Long kioskId, DayOfWeek dayOfWeek);
}
