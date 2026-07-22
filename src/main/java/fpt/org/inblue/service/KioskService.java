package fpt.org.inblue.service;

import fpt.org.inblue.model.Kiosk;
import fpt.org.inblue.model.KioskSchedule;
import fpt.org.inblue.model.dto.SlotDto;
import fpt.org.inblue.model.dto.response.KioskHistoryResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface KioskService {
    List<Kiosk> getAllKiosk();

    Kiosk createKiosk(Kiosk kiosk);

    KioskSchedule createSchedule(KioskSchedule schedule);

    List<KioskSchedule> getSchedulesByKiosk(Long kioskId);

    List<SlotDto> getAvailableSlots(Long kioskId, LocalDate date);

    Kiosk updateKiosk(Long id, Kiosk kiosk);

    KioskSchedule updateSchedule(Long id, KioskSchedule schedule);

    List<KioskHistoryResponseDto> getKioskHistory(Long kioskId);
}
