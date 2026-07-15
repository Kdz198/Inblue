package fpt.org.inblue.service;

import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.dailyco.DailyWebHookPayload;
import fpt.org.inblue.model.dto.dailyco.SessionCreationRequest;
import fpt.org.inblue.model.dto.dailyco.SessionResponse;
import fpt.org.inblue.model.dto.request.JoinSessionDtoRequest;
import fpt.org.inblue.model.dto.response.SessionDetailResponse;
import java.util.List;

public interface SessionService {
    List<SessionDetailResponse> getSessions();

    SessionDetailResponse getSession(int id);

    List<SessionDetailResponse> getSessionsByUserId(int userId);

    Session updateSession(Session session);

    // cho tạo session để chat
    SessionResponse createSession(SessionCreationRequest request);

    void saveJoinRecord(JoinSessionDtoRequest request);

    void updateLeaveRecord(DailyWebHookPayload payload);

    void updateSessionStatus(int sessionId, boolean isApproved);

    void deleteSession(String roomName);

    String makePayment(int sessionId);
}
