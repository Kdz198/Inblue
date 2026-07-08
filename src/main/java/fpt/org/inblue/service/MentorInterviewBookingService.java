package fpt.org.inblue.service;

import fpt.org.inblue.enums.BookingStatus;
import fpt.org.inblue.model.MentorInterviewBooking;
import fpt.org.inblue.model.dto.request.PickSlotDtoRequest;
import fpt.org.inblue.model.dto.response.KioskEnterDtoResponse;
import java.util.List;

public interface MentorInterviewBookingService {
    MentorInterviewBooking pickSlot(PickSlotDtoRequest dto, int userId);
    void cancelBooking(Long bookingId, int userId);
    List<MentorInterviewBooking> getBookingsByStatus(BookingStatus status);
    MentorInterviewBooking assignMentor(Long bookingId, int mentorId, String notes);
    KioskEnterDtoResponse enterKiosk(String sessionKey, Long kioskId);
}
