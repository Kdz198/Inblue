package fpt.org.inblue.service;

import fpt.org.inblue.model.dto.request.ChangeMentorPasswordRequest;
import fpt.org.inblue.model.dto.request.CreateMentorRequest;
import fpt.org.inblue.model.dto.request.UpdateMentorRequest;
import fpt.org.inblue.model.dto.response.MentorResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface MentorService {
    MentorResponse createMentor(CreateMentorRequest data, MultipartFile avatar) throws IOException;

    MentorResponse updateMentor(int id, UpdateMentorRequest data, MultipartFile avatar) throws IOException;

    MentorResponse changePassword(int id, ChangeMentorPasswordRequest request);

    MentorResponse getMentorById(int id);

    List<MentorResponse> getAllMentors();

    void toggleActive(int id);
}
