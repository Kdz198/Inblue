package fpt.org.inblue.service;

import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.SessionCreationRequest;
import fpt.org.inblue.model.dto.SessionResponse;

import java.util.List;

public interface SessionService {
    public List<Session> getSessions();
    public Session getSession(int id);
    public List<Session> getSessionsByUserId(int userId);
    public Session createSession(Session session);
    public Session updateSession(Session session);

    //cho tạo session để chat
    public SessionResponse createSession(SessionCreationRequest request);

}
