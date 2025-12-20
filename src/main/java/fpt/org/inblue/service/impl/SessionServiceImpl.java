package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Session;
import fpt.org.inblue.model.dto.JoinSessionDtoRequest;
import fpt.org.inblue.model.dto.dailyco.SessionCreationRequest;
import fpt.org.inblue.model.dto.dailyco.DailyCoCreationRequest;
import fpt.org.inblue.model.dto.dailyco.SessionResponse;
import fpt.org.inblue.model.enums.SessionStatus;
import fpt.org.inblue.repository.SessionRepository;
import fpt.org.inblue.service.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {
    public final RestTemplate restTemplate;
    public final String dailyApiUrl;
    public final String dailyApiKey;
    public final SessionRepository sessionRepository;

    public SessionServiceImpl(@Value("${daily.api.url}") String dailyApiUrl,
                              @Value("${daily.api.key}") String dailyApiKey,
                              SessionRepository sessionRepository,
                              RestTemplate restTemplate) {
        this.dailyApiUrl = dailyApiUrl;
        this.dailyApiKey = dailyApiKey;
        this.sessionRepository = sessionRepository;
        this.restTemplate = restTemplate;
    }



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
        return sessionRepository.findAllByUserId(userId);
    }

    @Override
    public Session updateSession(Session session) {
        if(!sessionRepository.existsById(session.getId())) {
            throw new CustomException("Session not found", HttpStatus.NOT_FOUND);
        }
        return sessionRepository.save(session);
    }

    public String helperCreateName() {
        long timestamp = System.currentTimeMillis();
        return "session-" + timestamp;
    }

    /**
     * Gửi yêu cầu POST tới Daily.co API để tạo một phòng họp mới.
     * @param request DTO chứa thông tin cấu hình Room
     * @return RoomResponse DTO chứa thông tin Room đã tạo (bao gồm URL)
     */
    @Override
    public SessionResponse createSession(SessionCreationRequest request) {
        //thiết lập header ( bắt buộc phải có autho để xác thực )
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dailyApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Tính toán Timestamp hết hạn (Expiration Time)
        long currentTime = System.currentTimeMillis() / 1000;
        long exp = currentTime + request.getDailyCoCreationRequest().getProperties().getExp();
        request.getDailyCoCreationRequest().getProperties().setExp((int) exp);
        request.getDailyCoCreationRequest().setName(helperCreateName());
        HttpEntity<DailyCoCreationRequest> entity = new HttpEntity<>(request.getDailyCoCreationRequest(), headers);
        // Gọi API Daily.co
        String apiUrl = dailyApiUrl + "/rooms"; // Endpoint để tạo Room
        ResponseEntity<SessionResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                SessionResponse.class // DTO mà mong muốn nhận về
        );
        //Kiểm tra và trả về kết quả
        if (response.getStatusCode() == HttpStatus.OK
                || response.getStatusCode() == HttpStatus.CREATED) {
            Session session = new Session();
            session.setRoomName(response.getBody().getName());
            session.setRoomUrl(response.getBody().getUrl());
            session.setUserId(request.getUserId());
            session.setUserId2(request.getMentorId());
            session.setStatus(SessionStatus.SCHEDULED);
            sessionRepository.save(session);
            return response.getBody();
        }
        else{
            throw new RuntimeException("Lỗi khi tạo Session trên Daily.co: " + response.getStatusCode());
        }
    }

    /**
     * Endpoint để logging khi có participant join vào phòng
     * Hàm này sẽ nhận về một JoinSessionDtoRequest từ webhook của Daily.co
     * trong đó có participantId và sessionName (tên phòng)
     * khi có người join thì fe sẽ lắng nghe sự kiện join metting ở dailt.co và sau đó đã có participant từ sự kiện đó rồi mới gửi về endpoint này để ghi nhận tracking người dùng tham gia vào db
     */
    @Override
    public void saveJoinRecord(JoinSessionDtoRequest request) {
        Session session = sessionRepository.findByRoomName(request.getSessionName());
        if(session == null) {
            throw new CustomException("Session not found", HttpStatus.NOT_FOUND);}
        if(session != null && session.getUserId() == request.getUserId()) {
            session.setParticipantId1(request.getParticipantId());
            session.setStatus(SessionStatus.ONGOING);
            session.setStartTime1(helperConvertToVietNamTime());
        }
        else if(session != null && session.getUserId2() == request.getUserId()) {
            session.setParticipantId2(request.getParticipantId());
            session.setStartTime2(helperConvertToVietNamTime());
        }
        else{
            throw new CustomException("User not in session", HttpStatus.FORBIDDEN);
        }
        sessionRepository.save(session);
    }

    public Timestamp helperConvertToVietNamTime() {
        long now = System.currentTimeMillis();
        // Chuyển đổi từ giây sang milliseconds
        long milliseconds = now + (7 * 60 * 60 * 1000); // Giờ Việt Nam là UTC+7
        // Tạo đối tượng Timestamp
        return new Timestamp(milliseconds);
    }

}
