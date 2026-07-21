package fpt.org.inblue.service;

import fpt.org.inblue.model.KioskBooking;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.model.dto.response.KioskEnterDtoResponse;

public interface KioskBookingService {
    KioskBooking pickSlot(PickSlotDtoRequest dto, int userId);

    void cancelBooking(Long bookingId, int userId);

    KioskEnterDtoResponse enterKiosk(String sessionKey, Long kioskId);
}
