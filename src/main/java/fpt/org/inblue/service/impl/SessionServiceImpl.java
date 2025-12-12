package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {
    @Autowired
    private SessionRepository sessionRepository;

    @Override
    public List<Session> getSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session getSession(int id) {
        if(!sessionRepository.existsById(id)) {
            throw new CustomException("Session not found", HttpStatus.NOT_FOUND);
        }
        return sessionRepository.findById(id).get();
    }

    @Override
    public List<Session> getSessionsByUserId(int userId) {
        return sessionRepository.findAllByUserIdOrMentorId(userId, userId);
    }

    @Override
    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Session updateSession(Session session) {
        if(!sessionRepository.existsById(session.getId())) {
            throw new CustomException("Session not found", HttpStatus.NOT_FOUND);
        }
        return sessionRepository.save(session);
    }
}
