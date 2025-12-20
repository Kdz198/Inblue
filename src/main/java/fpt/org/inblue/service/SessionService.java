package fpt.org.inblue.service;

import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.JoinSessionDtoRequest;
import fpt.org.inblue.model.dto.dailyco.DailyWebHookPayload;
import fpt.org.inblue.model.dto.dailyco.SessionCreationRequest;
import fpt.org.inblue.model.dto.dailyco.SessionResponse;

import java.util.List;

public interface SessionService {
    public List<Session> getSessions();
    public Session getSession(int id);
    public List<Session> getSessionsByUserId(int userId);
    public Session updateSession(Session session);

    //cho tạo session để chat
    public SessionResponse createSession(SessionCreationRequest request);
    public void saveJoinRecord(JoinSessionDtoRequest request);
    void updateLeaveRecord(DailyWebHookPayload payload);

    void deleteSession(String roomName);
}
